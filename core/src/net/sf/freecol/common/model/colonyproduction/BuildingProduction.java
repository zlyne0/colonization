package net.sf.freecol.common.model.colonyproduction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import net.sf.freecol.common.model.Building;
import net.sf.freecol.common.model.MapIdEntitiesReadOnly;
import net.sf.freecol.common.model.Production;
import net.sf.freecol.common.model.ProductionConsumption;
import net.sf.freecol.common.model.ProductionSummary;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.BuildingType;
import net.sf.freecol.common.model.specification.GoodsType;
import net.sf.freecol.common.model.specification.Modifier;

class BuildingProduction {
	Building building;
	private BuildingType buildingType;
	private List<Worker> workers = new ArrayList<Worker>(3);
	
	void init(Building building, BuildingType buildingType, MapIdEntitiesReadOnly<Unit> units, List<Worker> workers) {
		this.building = building;
		this.buildingType = buildingType;
		
		for (Unit unit : units.entities()) {
			this.workers.add(new Worker(unit, unit.unitType));
		}
		workers.addAll(this.workers);
	}

	public ProductionConsumption determineProductionConsumption(
		Warehouse warehouse,
		ProductionSummary globalProdCons, 
		int colonyProductionBonus
	) {
	    List<Production> productions = buildingInitProduction();
	    
		ProductionConsumption prodCons = new ProductionConsumption();
        for (Production production : productions) {
        	productionConsumption(prodCons, production, warehouse, globalProdCons, colonyProductionBonus);
        }
		return prodCons;
	}

	private List<Production> buildingInitProduction() {
		if (workers.isEmpty()) {
	        return buildingType.productionInfo.getUnattendedProductions();
	    } else {
	        return buildingType.productionInfo.getAttendedProductions();
	    }
	}
	
	private void productionConsumption(
	    ProductionConsumption prodCons, Production production, 
	    Warehouse warehouse, 
	    ProductionSummary globalProdCons,
	    int colonyProductionBonus
	) {
		final boolean avoidExcessProduction = buildingType.hasAbility(Ability.AVOID_EXCESS_PRODUCTION);
		final boolean consumeOnlySurplusProduction = buildingType.hasModifier(Modifier.CONSUME_ONLY_SURPLUS_PRODUCTION);
		final boolean canAutoProduce = buildingType.hasAbility(Ability.AUTO_PRODUCTION);
		
        HashSet<GoodsType> consumptionGoods = new HashSet<GoodsType>();
        for (java.util.Map.Entry<GoodsType, Integer> inputEntry : production.inputEntries()) {
            consumptionGoods.add(inputEntry.getKey());
        }
        
        for (java.util.Map.Entry<GoodsType, Integer> outputEntry : production.outputEntries()) {
        	GoodsType goodsType = outputEntry.getKey();
            int goodInitValue = outputEntry.getValue();
            if (0 == goodInitValue) {
                continue;
            }
            int goodQuantity = 0;
            
            if (workers.isEmpty()) {
                goodQuantity += goodInitValue;
                goodQuantity += colonyProductionBonus;
            } else {
                for (Worker worker : workers) {
                    goodQuantity += (int)worker.unitType.applyModifier(goodsType.getId(), goodInitValue);
                    goodQuantity += colonyProductionBonus;
                }
            }
            if (canAutoProduce) {
            	int available = warehouse.amount(goodsType);
            	if (available <= outputEntry.getKey().getBreedingNumber()) {
            		goodQuantity = 0;
            	} else {
            		int divisor = (int)buildingType.applyModifier(Modifier.BREEDING_DIVISOR, 0);
            		int factor = (int)buildingType.applyModifier(Modifier.BREEDING_FACTOR, 0);
            		goodQuantity = ((available - 1) / divisor + 1) * factor;
            	}
            }    
            prodCons.baseProduction.addGoods(goodsType.getId(), goodQuantity);
            
            if (avoidExcessProduction) {
            	int warehouseGoods = warehouse.amount(goodsType);
            	if (goodQuantity + warehouseGoods > warehouse.capacity()) {
            		goodQuantity = warehouse.capacity() - warehouseGoods;
            		if (goodQuantity < 0) {
            			goodQuantity = 0;
            		}
            	}
            }
            
            // its big simplicity because for one production product create all consume product, 
            // is it will produce another product it will not be narrowed to warehouse state
            // this simplicity is enough because every building consume only one good
            for (GoodsType cg : consumptionGoods) {
                prodCons.baseConsumption.addGoods(cg.getId(), -goodQuantity);
                
                if (consumeOnlySurplusProduction) {
                	int realCgProd = globalProdCons.getQuantity(cg.getId());
                	int maxCGConsumption = (int)buildingType.applyModifier(Modifier.CONSUME_ONLY_SURPLUS_PRODUCTION, realCgProd);
                	if (goodQuantity > maxCGConsumption) {
                		goodQuantity = maxCGConsumption;
                		if (goodQuantity < 0) {
                			goodQuantity = 0;
                		}
                	}
                } else {
                    int available = globalProdCons.getQuantity(cg.getId()) + warehouse.amount(cg);
	                if (available < goodQuantity) {
	                    goodQuantity = available;
	                }
                }
                prodCons.realConsumption.addGoods(cg.getId(), -goodQuantity);
            }
            consumptionGoods.clear();
            prodCons.realProduction.addGoods(goodsType.getId(), goodQuantity);
        }
	}

}