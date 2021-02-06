package net.sf.freecol.common.model.colonyproduction;

import java.util.List;

import net.sf.freecol.common.model.ObjectWithFeatures;
import net.sf.freecol.common.model.Production;
import net.sf.freecol.common.model.ProductionConsumption;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.specification.GoodsType;
import net.sf.freecol.common.model.specification.Modifier;

class ColonyTileProduction {
	
	Tile tile;
	private Worker worker;
	private List<Production> tileProduction;
	
	public void init(Tile tile, List<Production> tileProduction, Unit unit, List<Worker> workers) {
		this.tile = tile;
		this.tileProduction = tileProduction;
		
		if (unit != null) {
			worker = new Worker(unit, unit.unitType);
			workers.add(worker);
		} else {
			worker = null;
		}
	}

	void init(Tile tile, List<Production> tileProduction, UnitType unitType) {
		this.tile = tile;
		this.tileProduction = tileProduction;
		
		if (unitType == null) {
			worker = null;
		} else {
			if (worker != null) {
				worker.unitType = unitType;
				worker.unitId = null;
			} else {
				worker = new Worker(unitType);
			}
		}
	}
	
	public boolean hasWorker() {
		return worker != null;
	}
	
	public ProductionConsumption productionSummaryForTile(ObjectWithFeatures colonyFeatures) {
		ProductionConsumption prodCons = new ProductionConsumption();
		
		for (Production production : tileProduction) {
		    for (java.util.Map.Entry<GoodsType, Integer> outputEntry : production.outputEntries()) {
		    	if (outputEntry.getValue() == 0) {
		    		continue;
		    	}
		    	String goodsId = outputEntry.getKey().getId();
		    	int goodQuantity = workerTileProduction(outputEntry, colonyFeatures);
		    	
		        prodCons.realProduction.addGoods(goodsId, goodQuantity);
                prodCons.baseProduction.addGoods(goodsId, goodQuantity);
		    }
		}
		return prodCons; 
	}

	int workerTileProduction(java.util.Map.Entry<GoodsType, Integer> outputEntry, ObjectWithFeatures colonyFeatures) {
        String goodsId = outputEntry.getKey().getId();
        Integer goodInitValue = outputEntry.getValue();
        
        int goodQuantity = 0;
        if (worker != null) {
            goodQuantity = (int)worker.unitType.applyModifier(goodsId, goodInitValue);
            goodQuantity = (int)colonyFeatures.applyModifier(goodsId, goodQuantity);
        } else {
            goodQuantity = goodInitValue;
        }
        goodQuantity = tile.applyTileProductionModifier(goodsId, goodQuantity);
        goodQuantity = (int)colonyFeatures.applyModifier(Modifier.COLONY_PRODUCTION_BONUS, goodQuantity);
        return goodQuantity;
	}
}