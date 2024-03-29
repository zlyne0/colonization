package net.sf.freecol.common.model.colonyproduction;

import net.sf.freecol.common.model.Colony;
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
import java.util.Map;

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
				worker.unit = null;
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
			this.worker.unit = atw.worker.unit;
			this.worker.unitType = atw.worker.unitType;
		}
	}

	void sumWorkers(List<Worker> workers) {
		if (worker != null && worker.unitType != null) {
			workers.add(worker);
		}
	}

	public void clearWorkersAllocation() {
		if (worker != null) {
			// do not remove center tile production
			this.worker = null;
			this.tileProduction = Production.EMPTY_READONLY;
		}
	}

	public boolean hasWorker() {
		return worker != null && worker.unitType != null;
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

	MaxGoodsProductionLocation maxGoodsProduction(
		GoodsType goodsType,
		MaxGoodsProductionLocation maxProd,
		ObjectWithFeatures colonyUpdatableFeatures,
		ProductionValueComparator productionValueComparator
	) {
		List<Production> productions = tile.getType().productionInfo.getAttendedProductions();
		for (Production production : productions) {
			for (java.util.Map.Entry<GoodsType, Integer> outputEntry : production.outputEntries()) {
				if (goodsType.equalsId(outputEntry.getKey())) {
					int goodsQuantity = workerTileProduction(outputEntry, colonyUpdatableFeatures);

					if (goodsQuantity > 0 && productionValueComparator.more(maxProd, outputEntry.getKey(), goodsQuantity)) {
						if (maxProd == null) {
							maxProd = new MaxGoodsProductionLocation();
						}
						maxProd.goodsType = outputEntry.getKey();
						maxProd.production = goodsQuantity;
						maxProd.tileTypeInitProduction = production;
						maxProd.colonyTile = tile;
					}
				}
			}
		}
		return maxProd;
	}

	private int workerTileProduction(java.util.Map.Entry<GoodsType, Integer> outputEntry, ObjectWithFeatures colonyFeatures) {
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

	void potentialProductions(List<MaxGoodsProductionLocation> goodsProduction, ObjectWithFeatures colonyFeatures) {
		for (java.util.Map.Entry<GoodsType, Integer> outputEntry : tileProduction.outputEntries()) {
			int goodQuantity = workerTileProduction(outputEntry, colonyFeatures);

			MaxGoodsProductionLocation maxProd = new MaxGoodsProductionLocation();
			maxProd.goodsType = outputEntry.getKey();
			maxProd.production = goodQuantity;
			maxProd.tileTypeInitProduction = tileProduction;
			maxProd.colonyTile = tile;
			goodsProduction.add(maxProd);
		}
	}

	public void assignWorkerToColony(Colony colony) {
		if (worker != null) {
			colony.addWorkerToTerrain(tile, worker.unit, tileProduction);
		}
	}

	public Worker getWorker() {
		return worker;
	}

	public boolean isExpertAndWorkingInItsProfession() {
		if (worker == null || worker.unit == null || !worker.unit.isExpert()) {
			return false;
		}
		String workerExpertGoodsTypeId = worker.unit.unitType.getExpertProductionForGoodsId();
		if (workerExpertGoodsTypeId == null) {
			return false;
		}
		for (Map.Entry<GoodsType, Integer> outputEntry : tileProduction.outputEntries()) {
			if (outputEntry.getKey().equalsId(workerExpertGoodsTypeId)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "ColonyTileProduction{" +
			"tile=" + tile.getId() +
			", worker=" + worker +
			", tileProduction=" + tileProduction +
			'}';
	}
}