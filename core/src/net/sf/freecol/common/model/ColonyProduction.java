package net.sf.freecol.common.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.utils.ObjectIntMap.Entry;

import net.sf.freecol.common.model.specification.GameOptions;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.Validation;

class ColonyProduction {
    private static final Set<String> FOOD_GOODS = new HashSet<String>();
    static {
        FOOD_GOODS.add(GoodsType.FISH);
        FOOD_GOODS.add(GoodsType.GRAIN);
    }
    
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
    	int warehouseCapacity = colony.getWarehouseCapacity();

    	
        int unitsThatUseNoBells = Specification.options.getIntValue(GameOptions.UNITS_THAT_USE_NO_BELLS);
        int amount = Math.min(unitsThatUseNoBells, colony.getColonyUnitsCount());
        globalProductionConsumption.addGoods("model.goods.bells", amount);
    	
        for (ColonyTile ct : colony.colonyTiles.entities()) {
        	ProductionConsumption ps = productionSummaryForTerrain(ct.tile, ct);
        	prodConsByProducer.put(ct.getId(), ps);
        	
        	globalProductionConsumption.addGoods(ps.realProduction);
        }
        
        for (Unit worker : colony.colonyWorkers) {
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

        for (Building building : colony.buildings.entities()) {
        	ProductionConsumption pc = building.determineProductionConsumption(abstractWarehouse, warehouseCapacity, globalProductionConsumption, colony.productionBonus());
            pc.baseProduction.applyModifiers(colony.colonyBuildingsFeatures);
            pc.realProduction.applyModifiers(colony.colonyBuildingsFeatures);
        	
        	prodConsByProducer.put(building.getId(), pc);
        	
        	globalProductionConsumption.addGoods(pc.realConsumption);
        	globalProductionConsumption.addGoods(pc.realProduction);
        }
        
        consolidateFoods();
        
        System.out.println("global production consumption " + globalProductionConsumption);
        needUpdate = false;
    }
	
    private void consolidateFoods() {
        for (Entry<String> entry : globalProductionConsumption.entries()) {
            if (FOOD_GOODS.contains(entry.key)) {
                int q = entry.value;
                globalProductionConsumption.decrease(entry.key, q);
                globalProductionConsumption.addGoods(GoodsType.FOOD, q);
            }
        }
    }
    
	public ProductionConsumption productionSummaryForTerrain(Tile tile, ColonyTile colonyTile) {
		ProductionConsumption prodCons = new ProductionConsumption();
		
		List<Production> productions; 
		if (colonyTile.getWorker() != null) {
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
		        if (colonyTile.getWorker() != null) {
		            goodQuantity += (int)colonyTile.getWorker().unitType.applyModifier(goodsId, goodInitValue);
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

	List<GoodMaxProductionLocation> determinePotentialMaxGoodsProduction(Unit worker) {
        List<GoodMaxProductionLocation> goodsProduction = new ArrayList<GoodMaxProductionLocation>();
        
        ProductionSummary prodCons = globalProductionConsumption();
        colony.goodsContainer.cloneGoods();
        
        for (GoodsType gt : Specification.instance.goodsTypes.entities()) {
            if (gt.isFarmed()) {
                GoodMaxProductionLocation maxProd = maxProductionFromTile(gt, worker, colony.tileId);
                if (maxProd != null) {
                    goodsProduction.add(maxProd);
                }
            } else {
                GoodMaxProductionLocation maxProd = maxProductionFromBuilding(gt, worker);
                if (maxProd != null) {
                    goodsProduction.add(maxProd);
                }
            }
        }
        return goodsProduction;
    }
	
	private GoodMaxProductionLocation maxProductionFromBuilding(final GoodsType goodsType, Unit worker) {
	    GoodMaxProductionLocation maxProd = null;
	    
	    for (Building building : colony.buildings.entities()) {
	        if (!building.canAddWorker(worker)) {
	            continue;
	        }
	        
	        List<Production> productions = building.buildingType.productionInfo.getAttendedProductions();
	        for (Production production : productions) {
	            
	            HashSet<String> consumptionGoods = new HashSet<String>();
	            for (java.util.Map.Entry<GoodsType, Integer> inputEntry : production.inputEntries()) {
	                consumptionGoods.add(inputEntry.getKey().getId());
	            }
	            
	            for (java.util.Map.Entry<GoodsType, Integer> outputEntry : production.outputEntries()) {
	                String goodsId = outputEntry.getKey().getId();
	                if (!goodsType.equalsId(goodsId)) {
	                    continue;
	                }
	                
	                Integer goodInitValue = outputEntry.getValue();
	                if (0 == goodInitValue) {
	                    continue;
	                }
	                int goodQuantity = 0;
	                
                    goodQuantity += (int)worker.unitType.applyModifier(goodsId, goodInitValue);
                    goodQuantity += colony.productionBonus();
	         
                    globalProductionConsumption.
                    
                    // TODO:
//	                sprawdzenie czy w warehouse sa materialy na produkcjie
//	                moze by jakos skorzystac z warehouse i aktualnej produkcji
	            }
	            
	        }
	    }
        return null;
    }

    private GoodMaxProductionLocation maxProductionFromTile(final GoodsType goodsType, final Unit worker, final String tileForColonyId) {
	    GoodMaxProductionLocation maxProd = null;
	    
	    for (ColonyTile colonyTile : colony.colonyTiles.entities()) {
	        if (colonyTile.getWorker() != null) {
	            continue;
	        }
	        if (colonyTile.tile.getId().equals(tileForColonyId)) {
	            continue;
	        }
	        
	        
	        List<Production> productions = colonyTile.tile.type.productionInfo.getAttendedProductions();
	        for (Production production : productions) {
	            for (java.util.Map.Entry<GoodsType, Integer> outputEntry : production.outputEntries()) {
	                if (goodsType.equalsId(outputEntry.getKey())) {
	                    Integer goodInitValue = outputEntry.getValue();
	                    
	                    int goodQuantity = (int)worker.unitType.applyModifier(goodsType.getId(), goodInitValue);
	                    goodQuantity += colony.productionBonus();
	                    // TODO: tile improvment modifier
	                    // TODO: chyba production bonus powinien byc uwzlednianiny tylko wtedy gdy jest jakakolwiek produkcja 
	                    
	                    if (goodQuantity > 0) {
    	                    if (maxProd == null) {
    	                        maxProd = new GoodMaxProductionLocation(goodsType, goodQuantity, colonyTile);
    	                    } else {
    	                        if (maxProd.hasLessProduction(goodQuantity)) {
    	                            maxProd.setProduction(goodQuantity, colonyTile);
    	                        }
    	                    }
	                    }
	                }
	            }
	        }
	    }
	    return maxProd;
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
