package net.sf.freecol.common.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.badlogic.gdx.utils.ObjectIntMap.Entry;

import net.sf.freecol.common.model.player.FoundingFather;
import net.sf.freecol.common.model.specification.GameOptions;
import net.sf.freecol.common.model.specification.GoodsType;
import net.sf.freecol.common.util.Validation;

class ColonyProduction {
	private boolean needUpdate = true;
	private final Colony colony;
	private final java.util.Map<String,ProductionConsumption> prodConsByProducer = new HashMap<String, ProductionConsumption>();
	private final ProductionSummary globalProductionConsumption = new ProductionSummary();
	
	ColonyProduction(Colony colony) {
		this.colony = colony;
	}
	
	public void setAsNeedUpdate() {
		needUpdate = true;
	}
	
    private void update() {
    	if (!needUpdate) {
    		return;
    	}
    	prodConsByProducer.clear();
    	globalProductionConsumption.makeEmpty();
    	
    	ProductionSummary abstractWarehouse = colony.goodsContainer.cloneGoods();
    	int warehouseCapacity = colony.warehouseCapacity();

    	
        int unitsThatUseNoBells = Specification.options.getIntValue(GameOptions.UNITS_THAT_USE_NO_BELLS);
        int amount = Math.min(unitsThatUseNoBells, colony.getColonyUnitsCount());
        globalProductionConsumption.addGoods("model.goods.bells", amount);
    	
        for (ColonyTile ct : colony.colonyTiles.entities()) {
        	ProductionConsumption ps = productionSummaryForTerrain(ct.tile, ct);
        	prodConsByProducer.put(ct.getId(), ps);
        	
        	globalProductionConsumption.addGoods(ps.realProduction);
        }
        
        for (Unit worker : colony.settlementWorkers()) {
        	for (UnitConsumption uc : worker.unitType.unitConsumption.entities()) {
        		if (uc.getId().equals(GoodsType.FOOD)) {
        			if (globalProductionConsumption.decreaseIfHas(GoodsType.FISH, uc.getQuantity())) {
        				// can decrease
        			} else {
        				// can not decrease
        				globalProductionConsumption.decrease(GoodsType.GRAIN, uc.getQuantity());
        			}
        		} else {
	        		globalProductionConsumption.decrease(uc.getId(), uc.getQuantity());
        		}
        	}
        }

        for (Building building : colony.buildings.sortedEntities()) {
        	ProductionConsumption pc = building.determineProductionConsumption(abstractWarehouse, warehouseCapacity, globalProductionConsumption, colony.productionBonus());
            pc.baseProduction.applyModifiers(colony.colonyUpdatableFeatures);
            pc.realProduction.applyModifiers(colony.colonyUpdatableFeatures);
        	
        	prodConsByProducer.put(building.getId(), pc);
        	
        	globalProductionConsumption.addGoods(pc.realConsumption);
        	globalProductionConsumption.addGoods(pc.realProduction);
        }
        
        consolidateFoods();
        
        //System.out.println("global production consumption " + globalProductionConsumption);
        needUpdate = false;
    }
	
    private void consolidateFoods() {
        for (Entry<String> entry : globalProductionConsumption.entries()) {
            if (GoodsType.isFoodGoodsType(entry.key)) {
                int q = entry.value;
                globalProductionConsumption.decrease(entry.key, q);
                globalProductionConsumption.addGoods(GoodsType.FOOD, q);
            }
        }
    }
    
	public ProductionConsumption productionSummaryForTerrain(Tile tile, ColonyTile colonyTile) {
		ProductionConsumption prodCons = new ProductionConsumption();
		
		List<Production> productions; 
		if (colonyTile.hasWorker()) {
		    productions = colonyTile.productionInfo.getAttendedProductions();
		} else {
            productions = colonyTile.productionInfo.getUnattendedProductions();
		}
		
		for (Production production : productions) {
		    for (java.util.Map.Entry<GoodsType, Integer> outputEntry : production.outputEntries()) {
	            String goodsId = outputEntry.getKey().getId();
	            Integer goodInitValue = outputEntry.getValue();
	            if (0 == goodInitValue) {
	                continue;
	            }
	            int goodQuantity = 0;
		        if (colonyTile.hasWorker()) {
		            goodQuantity += (int)colonyTile.getWorker().unitType.applyModifier(goodsId, goodInitValue);
		            for (FoundingFather ff : colony.owner.foundingFathers.entities()) {
		            	goodQuantity = (int)ff.applyModifier(goodsId, goodQuantity);
		            }
		        } else {
		            goodQuantity += goodInitValue;
		        }
                goodQuantity += colony.productionBonus();
		        
		        prodCons.realProduction.addGoods(goodsId, goodQuantity);
                prodCons.baseProduction.addGoods(goodsId, goodQuantity);
		    }
		}
		
		if (prodCons.baseProduction.isNotEmpty()) {
			prodCons.baseProduction.applyTileImprovementsModifiers(tile);
			prodCons.baseProduction.applyModifier(colony.productionBonus());
		}
		if (prodCons.realProduction.isNotEmpty()) {
			prodCons.realProduction.applyTileImprovementsModifiers(tile);
			prodCons.realProduction.applyModifier(colony.productionBonus());
		}
		return prodCons; 
	}
	
	public void dispose() {
		prodConsByProducer.clear();
	}

	List<GoodMaxProductionLocation> determinePotentialMaxGoodsProduction(
		Collection<GoodsType> goodsTypes, 
		Unit worker, 
		boolean ignoreIndianOwner
	) {
        List<GoodMaxProductionLocation> goodsProduction = new ArrayList<GoodMaxProductionLocation>();
        
        ProductionSummary prodCons = globalProductionConsumption();
        
        for (GoodsType gt : goodsTypes) {
            GoodMaxProductionLocation maxProd = null;
            if (gt.isFarmed()) {
                maxProd = maxProductionFromTile(gt, worker, ignoreIndianOwner);
            } else {
                maxProd = maxProductionFromBuilding(gt, worker, prodCons, colony.goodsContainer);
            }
            if (maxProd != null) {
                goodsProduction.add(maxProd);
            }
        }
        return goodsProduction;
    }

	void determinePotentialColonyTilesProduction(Unit worker, List<GoodMaxProductionLocation> potentialMaxProduction) {
        for (GoodsType gt : Specification.instance.goodsTypes.entities()) {
            if (gt.isFarmed()) {
            	GoodMaxProductionLocation maxProd = maxProductionFromTile(gt, worker, false);
                if (maxProd != null) {
                    potentialMaxProduction.add(maxProd);
                }
            }
        }
	}
	
	private GoodMaxProductionLocation maxProductionFromBuilding(
			final GoodsType goodsType, Unit worker, 
			ProductionSummary prodCons, GoodsContainer warehouseGoods
	) {
	    GoodMaxProductionLocation maxProd = null;
	    
	    for (Building building : colony.buildings.sortedEntities()) {
	        if (!building.canAddWorker(worker)) {
	            continue;
	        }
	        
	        List<Production> productions = building.buildingType.productionInfo.getAttendedProductions();
	        for (Production production : productions) {
	            for (java.util.Map.Entry<GoodsType, Integer> outputEntry : production.outputEntries()) {
	                String goodsId = outputEntry.getKey().getId();
	                if (!goodsType.equalsId(goodsId) || 0 == outputEntry.getValue().intValue()) {
	                    continue;
	                }
	                
	                int goodQuantity = building.workerProductionAmount(colony, worker.unitType, outputEntry);
	         
	                for (java.util.Map.Entry<GoodsType, Integer> inputEntry : production.inputEntries()) {
						String cg = inputEntry.getKey().getId();
                        int available = prodCons.getQuantity(cg) + warehouseGoods.goodsAmount(cg);
    	                if (available < goodQuantity) {
    	                    goodQuantity = available;
    	                }
                    }
	                maxProd = GoodMaxProductionLocation.updateFromBuilding(goodsType, goodQuantity, maxProd, building);
	            }
	        }
	    }
        return maxProd;
    }

    /**
     * Determine max potential production of goodsTypeId, the best option.
     * Do not take into account input requirments 
     */
    void determineMaxPotentialProduction(String goodsTypeId, Unit worker, ProductionSummary prod, ProductionSummary cons) {
    	if (!worker.isPerson()) {
    		throw new IllegalArgumentException("worker[" + worker + "] is not a person ");
    	}
    	
    	for (Building building : colony.buildings.sortedEntities()) {
	        if (!building.canAddWorker(worker)) {
	            continue;
	        }
	        building.determineMaxPotentialProduction(colony, worker, prod, cons, goodsTypeId);
    	}
    }
	
    protected GoodMaxProductionLocation maxProductionFromTile(final GoodsType goodsType, final Unit worker, boolean ignoreIndianOwner) {
	    GoodMaxProductionLocation maxProd = null;
	    
	    for (ColonyTile colonyTile : colony.colonyTiles.entities()) {
	        if (colonyTile.hasWorker()) {
	            continue;
	        }
	        if (colonyTile.tile.getId().equals(colony.tile.getId())) {
	            continue;
	        }
	        if (colony.isTileLocked(colonyTile.tile, ignoreIndianOwner)) {
	        	continue;
	        }
	        
	        List<Production> productions = colonyTile.tile.getType().productionInfo.getAttendedProductions();
	        for (Production production : productions) {
	            for (java.util.Map.Entry<GoodsType, Integer> outputEntry : production.outputEntries()) {
	                if (goodsType.equalsId(outputEntry.getKey())) {
	                	maxProd = maxGoodProduction(outputEntry, worker, colonyTile, maxProd);
	                }
	            }
	        }
	    }
	    return maxProd;
	}

    private GoodMaxProductionLocation maxGoodProduction(
		final java.util.Map.Entry<GoodsType, Integer> outputEntry, 
		final Unit worker, final ColonyTile colonyTile, 
		GoodMaxProductionLocation maxProd
    ) {
    	int goodsQuantity = colonyWorkerProductionAmount(colony, worker, colonyTile, outputEntry);
    	
    	return GoodMaxProductionLocation.updateFromColonyTile(
			outputEntry.getKey(), 
			goodsQuantity, 
			maxProd, 
			colonyTile
		);
    }
    
    private int colonyWorkerProductionAmount(
		Colony colony,
		Unit worker, 
		ColonyTile colonyTile, 
		java.util.Map.Entry<GoodsType, Integer> goodsTypeProdAmount) 
    {
    	Integer goodInitValue = goodsTypeProdAmount.getValue();
    	GoodsType prodGoodsType = goodsTypeProdAmount.getKey();
    	
    	int goodsQuantity = (int)worker.unitType.applyModifier(prodGoodsType.getId(), goodInitValue);
    	goodsQuantity = (int)colony.colonyUpdatableFeatures.applyModifier(prodGoodsType.getId(), goodsQuantity);
    	goodsQuantity = colonyTile.tile.applyTileProductionModifier(prodGoodsType.getId(), goodsQuantity);
    	goodsQuantity += colony.productionBonus();
    	return goodsQuantity;
    }
    
	List<GoodMaxProductionLocation> determinePotentialTerrainProductions(ColonyTile colonyTile, Unit worker) {
		List<GoodMaxProductionLocation> goodsProduction = new ArrayList<GoodMaxProductionLocation>();
		
        List<Production> productions = colonyTile.tile.getType().productionInfo.getAttendedProductions();
        for (Production production : productions) {
            for (java.util.Map.Entry<GoodsType, Integer> outputEntry : production.outputEntries()) {
            	GoodMaxProductionLocation prod = maxGoodProduction(outputEntry, worker, colonyTile, null);
            	if (prod != null) {
	            	prod.tileTypeInitProduction = production;
	            	goodsProduction.add(prod);
            	}
            }
        }
		
		return goodsProduction;
	}
    
	public ProductionConsumption productionConsumptionForObject(String id) {
    	update();
    	ProductionConsumption productionConsumption = prodConsByProducer.get(id);
    	Validation.notNull(productionConsumption, "can not find producer/consumer by id: " + id);
		return productionConsumption;
	}

	public ProductionSummary globalProductionConsumption() {
		update();
		return globalProductionConsumption;
	}
}
