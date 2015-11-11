package net.sf.freecol.common.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import net.sf.freecol.common.model.specification.GameOptions;
import promitech.colonization.Validation;

class ColonyProduction {

	private boolean needUpdate = true;
	private final Colony colony;
	private final java.util.Map<String,ProductionConsumption> prodConsByProducer = new HashMap<String, ProductionConsumption>();
	private final ProductionConsumption globalProductionConsumption = new ProductionConsumption();
	
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
        globalProductionConsumption.realProduction.addGoods("model.goods.bells", amount);
    	
        for (ColonyTile ct : colony.colonyTiles.entities()) {
        	ProductionConsumption ps = productionSummaryForTerrain(ct.tile, ct, abstractWarehouse);
        	prodConsByProducer.put(ct.getId(), ps);
        	
        	globalProductionConsumption.add(ps);
            abstractWarehouse.addGoods(ps.realProduction);
        }
        
        for (Unit worker : colony.colonyWorkers) {
        	for (UnitConsumption uc : worker.unitType.unitConsumption.entities()) {
        		globalProductionConsumption.baseConsumption.addGoods(uc.getId(), uc.getQuantity());
        		globalProductionConsumption.realProduction.addGoods(uc.getId(), -uc.getQuantity());
        	}
        }

        // TODO: przemienienie wszystkich goods na ich odpowiedniki storedAs 
        
        for (Building building : colony.buildings.entities()) {
        	ProductionConsumption pc = building.determineProductionConsumption(abstractWarehouse, warehouseCapacity, globalProductionConsumption);
            pc.realProduction.applyModifiers(colony.colonyBuildingsFeatures);
            pc.baseProduction.applyModifiers(colony.colonyBuildingsFeatures);
        	
        	prodConsByProducer.put(building.getId(), pc);
        	
        	globalProductionConsumption.add(pc);
            abstractWarehouse.addGoods(pc.realProduction);
        }
        
        System.out.println("warehouse = " + colony.goodsContainer.cloneGoods());
        System.out.println("warehouse = " + abstractWarehouse);
        System.out.println("productionConsumption ##################");
        System.out.println("productionConsumption = " + globalProductionConsumption);
        System.out.println("productionConsumption ##################");
        needUpdate = false;
    }
	
	public ProductionConsumption productionSummaryForTerrain(Tile tile, ColonyTile colonyTile, ProductionSummary abstractWarehouse) {
		ProductionConsumption prodCons = new ProductionConsumption();
		
		List<Unit> workers = null;
		if (colonyTile.getWorker() != null) {
			workers = Arrays.asList(colonyTile.getWorker());
		} else {
			workers = Collections.emptyList();
		}
		colonyTile.productionInfo.determineProductionConsumption(prodCons, workers, abstractWarehouse);
		
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

	public ProductionConsumption globalProductionConsumption() {
		update();
		return globalProductionConsumption;
	}
    
}
