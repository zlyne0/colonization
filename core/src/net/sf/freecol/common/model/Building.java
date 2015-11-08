package net.sf.freecol.common.model;

import java.util.HashSet;

import net.sf.freecol.common.model.UnitContainer.NoAddReason;
import net.sf.freecol.common.model.specification.BuildingType;
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
    
	public ProductionConsumption determineProductionConsumption(ProductionSummary warehouse, int warehouseCapacity, ProductionConsumption globalProdCons) {
		ProductionConsumption prodCons = new ProductionConsumption();
        for (Production production : buildingType.productionInfo.productions) {
        	productionConsumption(prodCons, production, warehouse, warehouseCapacity, globalProdCons);
        }
		return prodCons;
	}
	
	private void productionConsumption(ProductionConsumption prodCons, Production production, ProductionSummary warehouse, int warehouseCapacity, ProductionConsumption globalProdCons) {
		boolean avoidExcessProduction = buildingType.hasAbility(Ability.AVOID_EXCESS_PRODUCTION);
		boolean consumeOnlySurplusProduction = buildingType.hasModifier(Modifier.CONSUME_ONLY_SURPLUS_PRODUCTION);
//		consumeOnlySurplusProduction = false;
//		avoidExcessProduction = false;
		
        HashSet<String> consumptionGoods = new HashSet<String>();
        
        for (java.util.Map.Entry<String, Integer> inputEntry : production.inputEntries()) {
            String goodsId = inputEntry.getKey();
            consumptionGoods.add(goodsId);
        }
        for (java.util.Map.Entry<String, Integer> outputEntry : production.outputEntries()) {
            String goodsId = outputEntry.getKey();
            Integer goodInitValue = outputEntry.getValue();
            if (0 == goodInitValue) {
                continue;
            }
            int goodQuantity = 0;
            
            if (production.isUnattended() && workers.isEmpty()) {
                goodQuantity += goodInitValue;
            } 
            if (!production.isUnattended() && !workers.isEmpty()) {
                for (Unit worker : workers.entities()) {
                    goodQuantity += (int)worker.unitType.applyModifier(goodsId, goodInitValue);
                }
            }
            if (goodsId.equals("model.goods.horses")) {
            	System.out.println("model.goods.horses = " + goodQuantity);
            }
            // TODO: canAutoProduce do usuniecia oparcie sie w calosci na attended lub unattended
            if (canAutoProduce()) {
            	int available = warehouse.getQuantity(goodsId);
            	// TODO: goodsType.getBreedingNumber()
            	if (available <= 2) {
            		goodQuantity = 0;
            	} else {
            		int divisor = (int)buildingType.applyModifier(Modifier.BREEDING_DIVISOR, 0);
            		int factor = (int)buildingType.applyModifier(Modifier.BREEDING_FACTOR, 0);
            		goodQuantity = ((available - 1) / divisor + 1) * factor;
            	}
            } else {
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
                prodCons.baseConsumption.addGoods(cg, goodQuantity);
                
                if (consumeOnlySurplusProduction) {
                	int realCgProd = globalProdCons.realProduction.getQuantity(cg);
                	int maxCGConsumption = (int)buildingType.applyModifier(Modifier.CONSUME_ONLY_SURPLUS_PRODUCTION, realCgProd);
                	if (goodQuantity > maxCGConsumption) {
                		goodQuantity = maxCGConsumption;
                		if (goodQuantity < 0) {
                			goodQuantity = 0;
                		}
                	}
                } else {
	                if (warehouse.hasNotGood(cg, goodQuantity)) {
	                	int warehouseMax = warehouse.getQuantity(cg);
	                    goodQuantity = warehouseMax;
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
