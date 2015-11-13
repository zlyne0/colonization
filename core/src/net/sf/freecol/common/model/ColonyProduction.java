package net.sf.freecol.common.model;

import java.util.HashMap;
import java.util.List;

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
        		globalProductionConsumption.addGoods(uc.getId(), -uc.getQuantity());
        	}
        }

        // TODO: przemienienie wszystkich goods na ich odpowiedniki storedAs 
        
        for (Building building : colony.buildings.entities()) {
        	ProductionConsumption pc = building.determineProductionConsumption(abstractWarehouse, warehouseCapacity, globalProductionConsumption);
            pc.baseProduction.applyModifiers(colony.colonyBuildingsFeatures);
            pc.realProduction.applyModifiers(colony.colonyBuildingsFeatures);
        	
        	prodConsByProducer.put(building.getId(), pc);
        	
        	globalProductionConsumption.addGoods(pc.realProduction);
        }
        
        System.out.println("warehouse = " + abstractWarehouse);
        System.out.println("productionConsumption ##################");
        System.out.println("productionConsumption = " + globalProductionConsumption);
        System.out.println("productionConsumption ##################");
        needUpdate = false;
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
