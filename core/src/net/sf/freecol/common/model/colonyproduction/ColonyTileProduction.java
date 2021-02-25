package net.sf.freecol.common.model.colonyproduction;

import java.util.ArrayList;
import java.util.List;

import net.sf.freecol.common.model.Identifiable;
import net.sf.freecol.common.model.ObjectWithFeatures;
import net.sf.freecol.common.model.Production;
import net.sf.freecol.common.model.ProductionConsumption;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.specification.GoodsType;
import net.sf.freecol.common.model.specification.Modifier;

class ColonyTileProduction implements Identifiable {
	
	Tile tile;
	private Worker worker;
	private final List<Production> tileProduction = new ArrayList<Production>(1);

	ColonyTileProduction() {
	}

	ColonyTileProduction(Tile tile) {
		this.tile = tile;
	}

	@Override
	public String getId() {
		return tile.getId();
	}

	void init(Tile tile, List<Production> production, UnitType workerType) {
		this.tile = tile;
		this.init(production, workerType);
	}

	void init(List<Production> tileProduction, Unit unit, List<Worker> workers) {
		this.tileProduction.clear();
		this.tileProduction.addAll(tileProduction);
		
		if (unit != null) {
			worker = new Worker(unit, unit.unitType);
			workers.add(worker);
		} else {
			worker = null;
		}
	}

	void init(List<Production> tileProduction, UnitType unitType) {
		this.tileProduction.clear();
		this.tileProduction.addAll(tileProduction);
		
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

	void addWorker(List<Worker> workers) {
		if (worker != null) {
			workers.add(worker);
		}
	}
	
	void init(Production tileProduction, UnitType unitType) {
		this.tileProduction.clear();
		this.tileProduction.add(tileProduction);
		this.worker = new Worker(unitType);
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