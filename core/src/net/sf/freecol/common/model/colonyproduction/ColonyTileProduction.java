package net.sf.freecol.common.model.colonyproduction;

import net.sf.freecol.common.model.Identifiable;
import net.sf.freecol.common.model.ObjectWithFeatures;
import net.sf.freecol.common.model.Production;
import net.sf.freecol.common.model.ProductionConsumption;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.specification.GoodsType;
import net.sf.freecol.common.model.specification.Modifier;

import java.util.List;

class ColonyTileProduction implements Identifiable {
	
	Tile tile;
	private Worker worker;
	private Production tileProduction = Production.EMPTY_READONLY;

	ColonyTileProduction() {
	}

	ColonyTileProduction(Tile tile) {
		this.tile = tile;
	}

	@Override
	public String getId() {
		return tile.getId();
	}

	void init(Tile tile, Production production, UnitType workerType) {
		this.tile = tile;
		this.init(production, workerType);
	}

	void init(Production tileProduction, Unit unit) {
		this.tileProduction = tileProduction;
		this.worker = new Worker(unit, unit.unitType);
	}

	void init(Production tileProduction) {
		this.tileProduction = tileProduction;
		this.worker = null;
	}

	void init(Production tileProduction, UnitType unitType) {
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

	public void init(ColonyTileProduction atw) {
		this.tileProduction = atw.tileProduction;
		if (this.worker == null) {
			this.worker = atw.worker;
		} else {
			this.worker.unitId = atw.worker.unitId;
			this.worker.unitType = atw.worker.unitType;
		}
	}

	void sumWorkers(List<Worker> workers) {
		if (worker != null) {
			workers.add(worker);
		}
	}

	public boolean hasWorker() {
		return worker != null;
	}
	
	public ProductionConsumption productionSummaryForTile(ObjectWithFeatures colonyFeatures) {
		ProductionConsumption prodCons = new ProductionConsumption();
		
		for (java.util.Map.Entry<GoodsType, Integer> outputEntry : tileProduction.outputEntries()) {
			String goodsId = outputEntry.getKey().getId();
			int goodQuantity = workerTileProduction(outputEntry, colonyFeatures);

			prodCons.realProduction.addGoods(goodsId, goodQuantity);
			prodCons.baseProduction.addGoods(goodsId, goodQuantity);
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