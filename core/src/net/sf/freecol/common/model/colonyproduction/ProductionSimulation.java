package net.sf.freecol.common.model.colonyproduction;

import java.util.List;

import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.Production;
import net.sf.freecol.common.model.ProductionSummary;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.specification.GoodsType;

class ProductionSimulation {

	private final ColonyTileProduction simTileProduction = new ColonyTileProduction();
	private final ColonySettingProvider colonyProvider;
	private final ProductionValueComparator productionValueComparator;

	public ProductionSimulation(ColonySettingProvider colonySettingProvider) {
		this.colonyProvider = colonySettingProvider;
		this.productionValueComparator = ProductionValueComparator.byQuantity;
	}

    protected MaxGoodsProductionLocation maxProductionFromTile(
		final GoodsType goodsType, 
		final UnitType workerType, 
		boolean ignoreIndianOwner,
		MapIdEntities<ColonyTileProduction> tiles
	) {
    	MaxGoodsProductionLocation maxProd = null;
	    
    	for (ColonyTileProduction colonyTile : tiles) {
	        if (colonyTile.hasWorker() 
        		|| colonyProvider.isCenterTile(colonyTile.tile) 
        		|| colonyProvider.isTileLocked(colonyTile.tile, ignoreIndianOwner)
    		) {
	            continue;
	        }
	        simTileProduction.init(colonyTile.tile, colonyTile.tile.getType().productionInfo.getAttendedProductions(), workerType);
	        
	        maxProd = maxGoodsProduction(goodsType, maxProd);
	    }
	    return maxProd;
	}
	
	public MaxGoodsProductionLocation maxGoodsProduction(
		GoodsType goodsType, 
		MaxGoodsProductionLocation maxProd
	) {
        List<Production> productions = simTileProduction.tile.getType().productionInfo.getAttendedProductions();
        for (Production production : productions) {
            for (java.util.Map.Entry<GoodsType, Integer> outputEntry : production.outputEntries()) {
                if (goodsType.equalsId(outputEntry.getKey())) {
                	int goodsQuantity = simTileProduction.workerTileProduction(outputEntry, colonyProvider.colonyUpdatableFeatures());
                	
                	if (goodsQuantity > 0 && productionValueComparator.more(maxProd, outputEntry.getKey(), goodsQuantity)) {
                		if (maxProd == null) {
                			maxProd = new MaxGoodsProductionLocation();
                		}
                		maxProd.goodsType = outputEntry.getKey();
                		maxProd.production = goodsQuantity;
                		maxProd.tileTypeInitProduction = production;
                		maxProd.colonyTile = simTileProduction.tile;
                	}
                }
            }
        }
		return maxProd;
	}
	
	/**
	 * Return increase in production. Not total production.
	 */
	MaxGoodsProductionLocation maxProductionFromBuilding(
		GoodsType goodsType, UnitType workerType, 
		ProductionSummary prodCons, 
		MapIdEntities<BuildingProduction> buildings,
		Warehouse warehouse
	) {
		MaxGoodsProductionLocation maxProd = null;
		
		for (BuildingProduction productionBuilding : buildings) {
			if (!productionBuilding.canAddWorker(workerType)) {
				continue;
			}
			int quantity = productionBuilding.singleWorkerProduction(workerType, goodsType, prodCons, warehouse, colonyProvider.colonyUpdatableFeatures());
			
			if (quantity > 0 && productionValueComparator.more(maxProd, goodsType, quantity)) {
				if (maxProd == null) {
					maxProd = new MaxGoodsProductionLocation();
				}
				maxProd.goodsType = goodsType;
				maxProd.production = quantity;
				maxProd.buildingType = productionBuilding.buildingType;
			}
		}
		return maxProd;
	}
	
	
}
