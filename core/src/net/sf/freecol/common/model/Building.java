package net.sf.freecol.common.model;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import net.sf.freecol.common.model.UnitContainer.NoAddReason;
import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.BuildingType;
import net.sf.freecol.common.model.specification.GoodsType;
import net.sf.freecol.common.model.specification.Modifier;
import promitech.colonization.savegame.ObjectFromNodeSetter;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class Building extends ObjectWithId implements ProductionLocation, UnitLocation {

    public BuildingType buildingType;
    private final MapIdEntities<Unit> workers = new MapIdEntities<Unit>();

    public Building(IdGenerator idGenerator, BuildingType aBuildingType) {
    	this(idGenerator.nextId(Building.class), aBuildingType);
    }
    
    private Building(String id, BuildingType aBuildingType) {
        super(id);
        this.buildingType = aBuildingType;
    }

	public UnitContainer.NoAddReason getNoAddReason(Unit unit) {
		if (!unit.isPerson()) {
			return UnitContainer.NoAddReason.WRONG_TYPE;
		}
		UnitContainer.NoAddReason reason = buildingType.getNoAddReason(unit.unitType);
		if (reason == NoAddReason.NONE) {
			int workersSpaceTaken = 0;
			for (Unit u : workers.entities()) {
				workersSpaceTaken += u.unitType.getSpaceTaken();
			}
			if (unit.unitType.getSpaceTaken() + workersSpaceTaken > buildingType.getWorkplaces()) {
				return UnitContainer.NoAddReason.CAPACITY_EXCEEDED;
			}
		}
		return reason;
	}    
    
    public boolean canAddWorker(Unit unit) {
        NoAddReason reason = getNoAddReason(unit);
//        if (NoAddReason.NONE != reason) {
//            System.out.println("can not add unit to " + buildingType + " because " + reason);
//        }
        return NoAddReason.NONE == reason;
    }
	
    public void determineMaxPotentialProduction(Colony colony, Unit worker, ProductionSummary prod, ProductionSummary cons) {
    	determineMaxPotentialProduction(colony, worker, prod, cons, null);
    }
    
    public void determineMaxPotentialProduction(
		Colony colony, 
		Unit worker, 
		ProductionSummary prod, 
		ProductionSummary cons, 
		String goodsTypeId) 
    {
		List<Production> productions = buildingType.productionInfo.getAttendedProductions();
		for (Production production : productions) {
			for (java.util.Map.Entry<GoodsType, Integer> outputEntry : production.outputEntries()) {
				String outputGoodsId = outputEntry.getKey().getId();
				if (goodsTypeId != null && !goodsTypeId.equals(outputGoodsId)) {
					continue;
				}
                if (0 == outputEntry.getValue().intValue()) {
                    continue;
                }
                
            	int goodQuantity = workerProductionAmount(colony, worker.unitType, outputEntry);
            	if (goodQuantity > prod.getQuantity(outputGoodsId)) {
            		prod.addGoods(outputGoodsId, goodQuantity);
            	}
            	for (java.util.Map.Entry<GoodsType, Integer> inputEntry : production.inputEntries()) {
            		// assumption, production amount to consumption amount ratio is one to one 
            		cons.addGoods(inputEntry.getKey().getId(), goodQuantity);
            	}
			}
		}
    }
    
	public ProductionConsumption determineProductionConsumption(
			ProductionSummary warehouse, 
			int warehouseCapacity, 
			ProductionSummary globalProdCons, 
			int colonyProductionBonus
	) {
	    boolean unattendedProduction = false;
	    List<Production> productions;
	    if (workers.isEmpty()) {
	        productions = buildingType.productionInfo.getUnattendedProductions();
	        unattendedProduction = true;
	    } else {
	        productions = buildingType.productionInfo.getAttendedProductions();
	        unattendedProduction = false;
	    }
	    
		ProductionConsumption prodCons = new ProductionConsumption();
        for (Production production : productions) {
        	productionConsumption(prodCons, production, warehouse, warehouseCapacity, globalProdCons, unattendedProduction, colonyProductionBonus);
        }
		return prodCons;
	}
	
	private void productionConsumption(
	    ProductionConsumption prodCons, Production production, 
	    ProductionSummary warehouse, int warehouseCapacity, 
	    ProductionSummary globalProdCons,
	    boolean unattendedProduction, 
	    int colonyProductionBonus
	) {
		final boolean avoidExcessProduction = buildingType.hasAbility(Ability.AVOID_EXCESS_PRODUCTION);
		final boolean consumeOnlySurplusProduction = buildingType.hasModifier(Modifier.CONSUME_ONLY_SURPLUS_PRODUCTION);
		final boolean canAutoProduce = buildingType.hasAbility(Ability.AUTO_PRODUCTION);
		
        HashSet<String> consumptionGoods = new HashSet<String>();
        
        for (java.util.Map.Entry<GoodsType, Integer> inputEntry : production.inputEntries()) {
            String goodsId = inputEntry.getKey().getId();
            consumptionGoods.add(goodsId);
        }
        for (java.util.Map.Entry<GoodsType, Integer> outputEntry : production.outputEntries()) {
            String goodsId = outputEntry.getKey().getId();
            Integer goodInitValue = outputEntry.getValue();
            if (0 == goodInitValue) {
                continue;
            }
            int goodQuantity = 0;
            
            if (unattendedProduction) {
                goodQuantity += goodInitValue;
                goodQuantity += colonyProductionBonus;
            } else {
                for (Unit worker : workers.entities()) {
                    goodQuantity += (int)worker.unitType.applyModifier(goodsId, goodInitValue);
                    goodQuantity += colonyProductionBonus;
                }
            }
            if (canAutoProduce) {
            	int available = warehouse.getQuantity(goodsId);
            	if (available <= outputEntry.getKey().getBreedingNumber()) {
            		goodQuantity = 0;
            	} else {
            		int divisor = (int)buildingType.applyModifier(Modifier.BREEDING_DIVISOR, 0);
            		int factor = (int)buildingType.applyModifier(Modifier.BREEDING_FACTOR, 0);
            		goodQuantity = ((available - 1) / divisor + 1) * factor;
            	}
            }    
            prodCons.baseProduction.addGoods(goodsId, goodQuantity);
            
            if (avoidExcessProduction) {
            	int warehouseGoods = warehouse.getQuantity(goodsId);
            	if (goodQuantity + warehouseGoods > warehouseCapacity) {
            		goodQuantity = warehouseCapacity - warehouseGoods;
            		if (goodQuantity < 0) {
            			goodQuantity = 0;
            		}
            	}
            }
            
            // its big simplicity because for one production product create all consume product, 
            // is it will produce another product it will not be narrowed to warehouse state
            // this simplicity is enough because every building consume only one good
            for (String cg : consumptionGoods) {
                prodCons.baseConsumption.addGoods(cg, -goodQuantity);
                
                if (consumeOnlySurplusProduction) {
                	int realCgProd = globalProdCons.getQuantity(cg);
                	int maxCGConsumption = (int)buildingType.applyModifier(Modifier.CONSUME_ONLY_SURPLUS_PRODUCTION, realCgProd);
                	if (goodQuantity > maxCGConsumption) {
                		goodQuantity = maxCGConsumption;
                		if (goodQuantity < 0) {
                			goodQuantity = 0;
                		}
                	}
                } else {
                    int available = globalProdCons.getQuantity(cg) + warehouse.getQuantity(cg);
	                if (available < goodQuantity) {
	                    goodQuantity = available;
	                }
                }
                prodCons.realConsumption.addGoods(cg, -goodQuantity);
            }
            consumptionGoods.clear();
            prodCons.realProduction.addGoods(goodsId, goodQuantity);
        }
	}
	
    public int workerProductionAmount(
		Colony colony,
		UnitType workerUnitType, 
		java.util.Map.Entry<GoodsType, Integer> goodsTypeProdAmount) 
    {
    	String outputGoodsId = goodsTypeProdAmount.getKey().getId();
    	Integer outputGoodsInitValue = goodsTypeProdAmount.getValue();
    	
    	int goodQuantity = 0;
        goodQuantity += (int)workerUnitType.applyModifier(outputGoodsId, outputGoodsInitValue);
        goodQuantity = (int)colony.colonyUpdatableFeatures.applyModifier(outputGoodsId, goodQuantity);
        goodQuantity = (int)colony.colonyUpdatableFeatures.applyModifier(Modifier.COLONY_PRODUCTION_BONUS, goodQuantity);
        return goodQuantity;
    }
	
	public String toString() {
	    return "id = " + getId() + ", type = " + buildingType;
	}
	
	public void upgrade(BuildingType aBuildingType) {
		this.buildingType = aBuildingType;
	}
	
	public MapIdEntities<Unit> damageBuilding() {
		buildingType = buildingType.getUpgradesFrom();
		if (workers.isEmpty()) {
			return MapIdEntities.unmodifiableEmpty();
		}
		
		MapIdEntities<Unit> eject = new MapIdEntities<Unit>();
		getWorkersToEject(eject);
		return eject;
	}

	/**
	 * Method return workers which can not work in building because of type or workspace capacity
	 * @param ejectWorkers
	 */
	public void getWorkersToEject(MapIdEntities<Unit> ejectWorkers) {
		if (workers.isEmpty()) {
			return;
		}
		
		int workersSpaceTaken = 0;
		for (Unit worker : workers.entities()) {
			UnitContainer.NoAddReason reason = buildingType.getNoAddReason(worker.unitType);
			if (reason != NoAddReason.NONE) {
				ejectWorkers.add(worker);
				continue;
			}
			if (workersSpaceTaken + worker.unitType.getSpaceTaken() > buildingType.getWorkplaces()) {
				ejectWorkers.add(worker);
			} else {
				workersSpaceTaken += worker.unitType.getSpaceTaken();
			}
		}
	}
	
    @Override
    public MapIdEntitiesReadOnly<Unit> getUnits() {
        return workers;
    }

    @Override
    public void addUnit(Unit unit) {
        workers.add(unit);
    }

    @Override
    public void removeUnit(Unit unit) {
    	unit.reduceMovesLeftToZero();
        workers.removeId(unit);
    }
    
    public static class Xml extends XmlNodeParser<Building> {

        private static final String ATTR_BUILDING_TYPE = "buildingType";

        public Xml() {
            addNode(Unit.class, new ObjectFromNodeSetter<Building, Unit>() {
                @Override
                public void set(Building target, Unit entity) {
                    entity.changeUnitLocation(target);
                }

                @Override
                public void generateXml(Building source, ChildObject2XmlCustomeHandler<Unit> xmlGenerator) throws IOException {
                    xmlGenerator.generateXmlFromCollection(source.workers.entities());
                }
            });
        }
        
        @Override
        public void startElement(XmlNodeAttributes attr) {
            String id = attr.getStrAttribute(ATTR_ID);
            String buildingTypeId = attr.getStrAttributeNotNull(ATTR_BUILDING_TYPE);
            BuildingType buildingType = Specification.instance.buildingTypes.getById(buildingTypeId);
            Building b = new Building(id, buildingType);
            nodeObject = b;
        }

        @Override
        public void startWriteAttr(Building n, XmlNodeAttributesWriter attr) throws IOException {
        	attr.setId(n);
        	attr.set(ATTR_BUILDING_TYPE, n.buildingType);
        }
        
        @Override
        public String getTagName() {
            return tagName();
        }
        
        public static String tagName() {
            return "building";
        }
    }
}
