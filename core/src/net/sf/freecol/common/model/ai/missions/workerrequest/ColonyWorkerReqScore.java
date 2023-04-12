package net.sf.freecol.common.model.ai.missions.workerrequest;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.colonyproduction.ColonyProduction;
import net.sf.freecol.common.model.colonyproduction.ColonySimulationSettingProvider;
import net.sf.freecol.common.model.colonyproduction.MaxGoodsProductionLocation;
import net.sf.freecol.common.model.colonyproduction.ProductionSimulation;
import net.sf.freecol.common.model.player.Market;
import net.sf.freecol.common.model.specification.GoodsType;

import java.util.List;

import promitech.colonization.ai.score.ScoreableObjectsList;

class ColonyWorkerReqScore {

	private static final int MAX_UNITS_TYPES = 3;
	private static final boolean IGNORE_INDIAN_OWNER = true;
	
	private final Market market;
	private final MapIdEntities<GoodsType> goodsTypeToScore;
	private boolean consumeWarehouseResources = false;
	
	private Colony colony;
	private ColonyProduction colonyProduction;
	private ProductionSimulation productionSimulation;
	private ColonySimulationSettingProvider colonyProvider;

	public ColonyWorkerReqScore(Market market, MapIdEntities<GoodsType> goodsTypeToScore) {
		this.market = market;
		this.goodsTypeToScore = goodsTypeToScore;
	}

	public void simulate(Colony colony, ScoreableObjectsList<WorkerRequestScoreValue> tileScore) {
		this.colony = colony;
		ScoreableObjectsList<SingleWorkerRequestScoreValue> reqUnits;

		reqUnits = simulate(ProductionSimulation.expertForGoodsType);
		if (!reqUnits.isEmpty() && reqUnits.sumScore() > 0) {
			tileScore.add(new MultipleWorkerRequestScoreValue(reqUnits));
		}
		reqUnits = simulate(ProductionSimulation.freeColonistsForGoodsType);
		if (!reqUnits.isEmpty() && reqUnits.sumScore() > 0) {
			tileScore.add(new MultipleWorkerRequestScoreValue(reqUnits));
		}
	}

	private ScoreableObjectsList<SingleWorkerRequestScoreValue> simulate(ProductionSimulation.UnitTypeByGoodsTypePolicy unitTypeByGoodsTypePolicy) {
		ScoreableObjectsList<SingleWorkerRequestScoreValue> reqUnits = new ScoreableObjectsList<SingleWorkerRequestScoreValue>(MAX_UNITS_TYPES);

		colonyProvider = new ColonySimulationSettingProvider(colony);
		colonyProduction = new ColonyProduction(colonyProvider);
		productionSimulation = colonyProduction.simulation();

		if (consumeWarehouseResources) {
			colonyProvider.withConsumeWarehouseResources();
		}

		while (reqUnits.size() < MAX_UNITS_TYPES) {
			if (!colonyProduction.canSustainNewWorker()) {
				SingleWorkerRequestScoreValue scoreValue = tryFindFoodProducer(unitTypeByGoodsTypePolicy);
				if (scoreValue == null || colonyProduction.isNegativeProductionBonus()) {
					break;
				}
				reqUnits.add(scoreValue);
			} else {
				SingleWorkerRequestScoreValue scoreValue = tryFindMaxValuableProducer(unitTypeByGoodsTypePolicy);
				if (scoreValue == null || colonyProduction.isNegativeProductionBonus()) {
					break;
				}
				reqUnits.add(scoreValue);
			}
		}
		removeLastNotDesiredProduction(reqUnits);
		return reqUnits;
	}

	private void removeLastNotDesiredProduction(ScoreableObjectsList<SingleWorkerRequestScoreValue> reqUnits) {
		// usually food
		while (!reqUnits.isEmpty()) {
			SingleWorkerRequestScoreValue lastScoreValue = reqUnits.lastObj();
			if (!goodsTypeToScore.containsId(lastScoreValue.getGoodsType())) {
				reqUnits.removeLast();
			} else {
				break;
			}
		}
	}

	private SingleWorkerRequestScoreValue tryFindFoodProducer(ProductionSimulation.UnitTypeByGoodsTypePolicy unitTypeByGoodsTypePolicy) {
		List<MaxGoodsProductionLocation> maxProductionForGoods = productionSimulation.determinePotentialMaxGoodsProduction(
			Specification.instance.foodsGoodsTypes,
			unitTypeByGoodsTypePolicy,
			IGNORE_INDIAN_OWNER
		);
		MaxGoodsProductionLocation foodTheBestLocation = null;
		for (MaxGoodsProductionLocation loc : maxProductionForGoods) {
			if (!loc.getGoodsType().isFood()) {
				continue;
			}
			if (foodTheBestLocation == null || loc.getProduction() > foodTheBestLocation.getProduction()) {
				foodTheBestLocation = loc;
			}
		}
		if (foodTheBestLocation != null) {
			UnitType unitType = unitTypeByGoodsTypePolicy.unitType(foodTheBestLocation.getGoodsType());
			colonyProvider.addWorkerToColony(unitType, foodTheBestLocation);
			colonyProduction.updateRequest();
			return new SingleWorkerRequestScoreValue(
				foodTheBestLocation.getGoodsType(),
				foodTheBestLocation.getProduction(), 0, unitType,
				colony.tile
			);
		}
		return null;
	}

	private SingleWorkerRequestScoreValue tryFindMaxValuableProducer(ProductionSimulation.UnitTypeByGoodsTypePolicy unitTypeByGoodsTypePolicy) {
		List<MaxGoodsProductionLocation> maxProductionForGoods = productionSimulation.determinePotentialMaxGoodsProduction(
			goodsTypeToScore.entities(),
			unitTypeByGoodsTypePolicy,
			IGNORE_INDIAN_OWNER
		);
		
		MaxGoodsProductionLocation theBestScoreLoc = null;
		int theBestScore = 0;
		
		for (MaxGoodsProductionLocation goodMaxProductionLocation : maxProductionForGoods) {
			int score = market.getSalePrice(
				goodMaxProductionLocation.getGoodsType(), 
				goodMaxProductionLocation.getProduction()
			); 
			if (theBestScoreLoc == null || score > theBestScore) {
				theBestScoreLoc = goodMaxProductionLocation;
				theBestScore = score;
			}
		}
		if (theBestScoreLoc != null) {
			UnitType unitType = unitTypeByGoodsTypePolicy.unitType(theBestScoreLoc.getGoodsType());
			colonyProvider.addWorkerToColony(unitType, theBestScoreLoc);
			colonyProduction.updateRequest();

			return new SingleWorkerRequestScoreValue(
				theBestScoreLoc.getGoodsType(),
				theBestScoreLoc.getProduction(),
				theBestScore, unitType,
				colony.tile
			);
		}
		return null;
	}

	public boolean isConsumeWarehouseResources() {
		return consumeWarehouseResources;
	}

	public ColonyWorkerReqScore withConsumeWarehouseResources() {
		this.consumeWarehouseResources = true;
		return this;
	}
}
