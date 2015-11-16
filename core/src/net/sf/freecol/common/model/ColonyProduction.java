package net.sf.freecol.common.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.utils.ObjectIntMap.Entries;
import com.badlogic.gdx.utils.ObjectIntMap.Entry;

import net.sf.freecol.common.model.specification.GameOptions;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.Validation;

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
        	ProductionConsumption pc = building.determineProductionConsumption(abstractWarehouse, warehouseCapacity, globalProductionConsumption);
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
	
    private static final Set<String> FOOD_GOODS = new HashSet<String>();
    static {
        FOOD_GOODS.add(GoodsType.FISH);
        FOOD_GOODS.add(GoodsType.GRAIN);
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
