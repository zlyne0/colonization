package net.sf.freecol.common.model.ai.missions.workerrequest;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.colonyproduction.ColonyProduction;
import net.sf.freecol.common.model.colonyproduction.ColonySimulationSettingProvider;
import net.sf.freecol.common.model.colonyproduction.MaxGoodsProductionLocation;
import net.sf.freecol.common.model.colonyproduction.ProductionSimulation;
import net.sf.freecol.common.model.player.Market;
import net.sf.freecol.common.model.specification.GoodsType;

import java.util.List;

import promitech.colonization.ai.ObjectsListScore;

class ColonyWorkerReqScore {

	private static final int MAX_UNITS_TYPES = 3;
	private static final boolean IGNORE_INDIAN_OWNER = true;
	
	private final Market market;
	private final Tile colonyLocation;
	private ObjectsListScore<WorkerRequestScoreValue> reqUnits = new ObjectsListScore<WorkerRequestScoreValue>(MAX_UNITS_TYPES);
	private final MapIdEntities<GoodsType> goodsTypeToScore;
	private boolean consumeWarehouseResources = false;
	
	private final ColonyProduction colonyProduction;
	private final ProductionSimulation productionSimulation;
	private final ColonySimulationSettingProvider colonyProvider;
	
	public ColonyWorkerReqScore(Colony colony, MapIdEntities<GoodsType> goodsTypeToScore) {
		this.market = colony.getOwner().market();		
		this.goodsTypeToScore = goodsTypeToScore;
		this.colonyLocation = colony.tile;

		colonyProvider = new ColonySimulationSettingProvider(colony);
		colonyProduction = new ColonyProduction(colonyProvider);
		productionSimulation = colonyProduction.simulation();
	}
	
	public ObjectsListScore<WorkerRequestScoreValue> simulate() {
		if (consumeWarehouseResources) {
			colonyProvider.withConsumeWarehouseResources();
		}

		while (reqUnits.size() < MAX_UNITS_TYPES) {
			if (!colonyProduction.canSustainNewWorker()) {
				boolean found = tryFindFoodProducer();
				if (!found) {
					break;
				}
			} else {
				boolean found = tryFindMaxValuableProducer();
				if (!found) {
					break;
				}
			}
		}

		removeLastNotDesiredProduction();
		return reqUnits;
	}

	private void removeLastNotDesiredProduction() {
		// usually food
		while (!reqUnits.isEmpty()) {
			UnitType unitType = reqUnits.lastObj().getWorkerType();
			if (unitType.isType(UnitType.FREE_COLONIST)) {
				break;
			}
			if (isExistsOnDesiredProductionGoods(unitType.getExpertProductionForGoodsId())) {
				break;
			} else {
				reqUnits.removeLast();
			}
		}
	}

	private boolean isExistsOnDesiredProductionGoods(String goodsTypeId) {
		return goodsTypeToScore.containsId(goodsTypeId);
	}
	
	private boolean tryFindFoodProducer() {
		List<MaxGoodsProductionLocation> maxProductionForGoods = productionSimulation.determinePotentialMaxGoodsProduction(
			Specification.instance.foodsGoodsTypes,
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
			UnitType expertType = Specification.instance.expertUnitTypeForGoodsType(foodTheBestLocation.getGoodsType());
			reqUnits.add(new WorkerRequestScoreValue(
				foodTheBestLocation.getGoodsType(),
				foodTheBestLocation.getProduction(), 0, expertType,
				colonyLocation
			),0);
			colonyProvider.addWorkerToColony(expertType, foodTheBestLocation);
			colonyProduction.updateRequest();
			return true;
		}
		return false;
	}

	private boolean tryFindMaxValuableProducer() {
		List<MaxGoodsProductionLocation> maxProductionForGoods = productionSimulation.determinePotentialMaxGoodsProduction(
			goodsTypeToScore.entities(),
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
			UnitType expertType = Specification.instance.expertUnitTypeForGoodsType(theBestScoreLoc.getGoodsType());
			reqUnits.add(
				new WorkerRequestScoreValue(
					theBestScoreLoc.getGoodsType(),
					theBestScoreLoc.getProduction(),
					theBestScore, expertType,
					colonyLocation
				),
				theBestScore
			);
			colonyProvider.addWorkerToColony(expertType, theBestScoreLoc);
			colonyProduction.updateRequest();
			return true;
		}
		return false;
	}

	public boolean isConsumeWarehouseResources() {
		return consumeWarehouseResources;
	}

	public ColonyWorkerReqScore withConsumeWarehouseResources() {
		this.consumeWarehouseResources = true;
		return this;
	}
}
