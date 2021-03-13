package net.sf.freecol.common.model.colonyproduction;

import net.sf.freecol.common.model.Identifiable;
import net.sf.freecol.common.model.MapIdEntitiesReadOnly;
import net.sf.freecol.common.model.ObjectWithFeatures;
import net.sf.freecol.common.model.Production;
import net.sf.freecol.common.model.ProductionConsumption;
import net.sf.freecol.common.model.ProductionSummary;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitContainer.NoAddReason;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.BuildingType;
import net.sf.freecol.common.model.specification.GoodsType;
import net.sf.freecol.common.model.specification.Modifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

class BuildingProduction implements Identifiable {
	final BuildingType buildingType;
	private List<Worker> workers = new ArrayList<Worker>(3);

	BuildingProduction(BuildingType buildingType) {
		this.buildingType = buildingType;
	}
	
	@Override
	public String getId() {
		return buildingType.getId();
	}
	
	void initWorkers(MapIdEntitiesReadOnly<Unit> units) {
		this.workers.clear();
		for (Unit unit : units.entities()) {
			this.workers.add(new Worker(unit, unit.unitType));
		}
	}

	public void addWorker(UnitType workerType) {
		this.workers.add(new Worker(workerType));
	}

	public void addWorker(List<UnitType> workerType) {
		for (UnitType unitType : workerType) {
			this.workers.add(new Worker(unitType));
		}
	}

	public void sumWorkers(List<Worker> globalWorkers) {
		globalWorkers.addAll(this.workers);
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

	public void determineMaxPotentialProduction(
		ObjectWithFeatures colonyFeatures,
		UnitType workerType,
		ProductionSummary prod,
		ProductionSummary cons,
		String goodsTypeId
	) {
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

				int goodQuantity = workerProductionAmount(workerType, outputEntry, colonyFeatures);
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
                	int realCgProd = globalProdCons.getQuantity(cg);
                	int maxCGConsumption = (int)buildingType.applyModifier(Modifier.CONSUME_ONLY_SURPLUS_PRODUCTION, realCgProd);
                	if (goodQuantity > maxCGConsumption) {
                		goodQuantity = maxCGConsumption;
                		if (goodQuantity < 0) {
                			goodQuantity = 0;
                		}
                	}
                } else {
                    int available = globalProdCons.getQuantity(cg) + warehouse.amount(cg);
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

	boolean canAddWorker(UnitType unitType) {
		int workersSpaceTaken = workersSpaceTaken();
		NoAddReason reason = buildingType.addWorkerToBuildingReason(unitType, workersSpaceTaken);
		return NoAddReason.NONE == reason;
	}

	private int workersSpaceTaken() {
		int sum = 0;
		for (Worker worker : workers) {
			sum += worker.unitType.getSpaceTaken();
		}
		return sum;
	}

	int singleWorkerProduction(
		UnitType workerType, GoodsType goodsType, 
		ProductionSummary prodCons, Warehouse warehouse,
		ObjectWithFeatures colonyFeatures
	) {
        List<Production> productions = buildingType.productionInfo.getAttendedProductions();
        for (Production production : productions) {
            for (java.util.Map.Entry<GoodsType, Integer> outputEntry : production.outputEntries()) {
                if (!goodsType.equalsId(outputEntry.getKey()) || 0 == outputEntry.getValue().intValue()) {
                    continue;
                }
                
                int goodQuantity = workerProductionAmount(workerType, outputEntry, colonyFeatures);
         
                for (java.util.Map.Entry<GoodsType, Integer> inputEntry : production.inputEntries()) {
                    GoodsType inputGoodsType = inputEntry.getKey();
					int available = prodCons.getQuantity(inputGoodsType) + warehouse.amount(inputGoodsType);
	                if (available < goodQuantity) {
	                    goodQuantity = available;
	                }
                }
                return goodQuantity;
            }
        }
		return 0;
	}
	
    private int workerProductionAmount(
		UnitType workerUnitType, 
		java.util.Map.Entry<GoodsType, Integer> goodsTypeProdAmount,
		ObjectWithFeatures colonyFeatures
	) {
    	String outputGoodsId = goodsTypeProdAmount.getKey().getId();
    	Integer outputGoodsInitValue = goodsTypeProdAmount.getValue();
    	
    	int goodQuantity = 0;
        goodQuantity += (int)workerUnitType.applyModifier(outputGoodsId, outputGoodsInitValue);
        goodQuantity = (int)colonyFeatures.applyModifier(outputGoodsId, goodQuantity);
        goodQuantity = (int)colonyFeatures.applyModifier(Modifier.COLONY_PRODUCTION_BONUS, goodQuantity);
        return goodQuantity;
    }
}