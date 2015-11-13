package net.sf.freecol.common.model;

import java.util.HashSet;
import java.util.List;

import net.sf.freecol.common.model.UnitContainer.NoAddReason;
import net.sf.freecol.common.model.specification.BuildingType;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class Building extends ObjectWithId {

    public BuildingType buildingType;
    public final MapIdEntities<Unit> workers = new MapIdEntities<Unit>();
    
    public Building(String id) {
        super(id);
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
    
	public ProductionConsumption determineProductionConsumption(
			ProductionSummary warehouse, 
			int warehouseCapacity, 
			ProductionSummary globalProdCons
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
        	productionConsumption(prodCons, production, warehouse, warehouseCapacity, globalProdCons, unattendedProduction);
        }
		return prodCons;
	}
	
	private void productionConsumption(
	    ProductionConsumption prodCons, Production production, 
	    ProductionSummary warehouse, int warehouseCapacity, 
	    ProductionSummary globalProdCons,
	    boolean unattendedProduction
	) {
		final boolean avoidExcessProduction = buildingType.hasAbility(Ability.AVOID_EXCESS_PRODUCTION);
		final boolean consumeOnlySurplusProduction = buildingType.hasModifier(Modifier.CONSUME_ONLY_SURPLUS_PRODUCTION);
		final boolean canAutoProduce = canAutoProduce();
		
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
            } else {
                for (Unit worker : workers.entities()) {
                    goodQuantity += (int)worker.unitType.applyModifier(goodsId, goodInitValue);
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
            // it's simplicity is enough because every building consume only one good
            for (String cg : consumptionGoods) {
                prodCons.baseProduction.addGoods(cg, goodQuantity);
                
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
                prodCons.realProduction.addGoods(cg, -goodQuantity);
            }
            consumptionGoods.clear();
            prodCons.realProduction.addGoods(goodsId, goodQuantity);
        }
	}
	
	private boolean canAutoProduce() {
		return buildingType.hasAbility(Ability.AUTO_PRODUCTION);
	}
	
	public String toString() {
	    return "id = " + getId() + ", type = " + buildingType;
	}
	
    public static class Xml extends XmlNodeParser {

        public Xml() {
            addNodeForMapIdEntities("workers", Unit.class);
        }
        
        @Override
        public void startElement(XmlNodeAttributes attr) {
            String id = attr.getStrAttribute("id");
            Building b = new Building(id);
            
            String buildingTypeId = attr.getStrAttribute("buildingType");
            if (buildingTypeId == null) {
                throw new IllegalStateException("can not find buildingType for building " + id);
            }
            BuildingType buildingType = game.specification.buildingTypes.getById(buildingTypeId);
            b.buildingType = buildingType;
            
            nodeObject = b;
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
