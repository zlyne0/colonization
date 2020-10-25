package net.sf.freecol.common.model.ai;

import java.util.ArrayList;
import java.util.List;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.GoodMaxProductionLocation;
import net.sf.freecol.common.model.ProductionSummary;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitFactory;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.player.Market;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.ai.ObjectsListScore.ObjectScore;

class ColonyWorkerReq {

	private static final boolean IGNORE_INDIAN_OWNER = true;
	
	// Every unit has owner, for simulation add unit to colony, so i have to know who unit is for simulation only.
	private final List<Unit> createdUnits = new ArrayList<Unit>();
	private final Colony colony;
	private final Market market;
	private final List<UnitType> reqUnits = new ArrayList<UnitType>();
	private final List<GoodsType> goodsTypeToScore;
	private int colonyScore = 0;
	private boolean consumeWarehouseResources = false;
	
	public ColonyWorkerReq(Colony colony, List<GoodsType> goodsTypeToScore) {
		this.colony = colony;
		this.market = colony.getOwner().market();		
		this.goodsTypeToScore = goodsTypeToScore;
	}
	
	public ObjectScore<List<UnitType>> simulate() {
		ProductionSummary warehouseCopy = null;
		if (!consumeWarehouseResources) {
			warehouseCopy = colony.getGoodsContainer().cloneGoods();
			colony.getGoodsContainer().decreaseAllToZero();
			colony.updateModelOnWorkerAllocationOrGoodsTransfer();
		}
		
		while (reqUnits.size() < 3) {
			Unit colonist = UnitFactory.create(UnitType.FREE_COLONIST, colony.getOwner(), colony.tile);
			createdUnits.add(colonist);

			if (!colony.canSustainNewWorker(colonist)) {
				boolean found = tryFindFoodProducer(colonist);
				if (!found) {
					break;
				}
			} else {
				boolean found = tryFindMaxValuableProducer(colonist);
				if (!found) {
					break;
				}
			}
		}
		removeLastNotDesiredProduction();
		
		for (Unit unit : createdUnits) {
			colony.getOwner().removeUnit(unit);
		}
		if (!consumeWarehouseResources) {
			colony.getGoodsContainer().decreaseAllToZero();
			colony.getGoodsContainer().increaseGoodsQuantity(warehouseCopy);
		}
		
		colony.updateColonyPopulation();
		colony.updateModelOnWorkerAllocationOrGoodsTransfer();
		return new ObjectScore<List<UnitType>>(reqUnits, colonyScore);
	}

	private void removeLastNotDesiredProduction() {
		// usually food
		while (!reqUnits.isEmpty()) {
			UnitType unitType = reqUnits.get(reqUnits.size() - 1);
			if (unitType.isType(UnitType.FREE_COLONIST)) {
				break;
			}
			if (isExistsOnDesiredProductionGoods(unitType.getExpertProductionForGoodsId())) {
				break;
			} else {
				reqUnits.remove(reqUnits.size() - 1);
			}
		}
	}

	private boolean isExistsOnDesiredProductionGoods(String goodsTypeId) {
		for (GoodsType goodsType : goodsTypeToScore) {
			if (goodsType.isType(goodsTypeId)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean tryFindFoodProducer(Unit colonist) {
		List<GoodMaxProductionLocation> maxProductionForGoods = colony.determinePotentialMaxGoodsProduction(
			Specification.instance.foodsGoodsTypes,
			colonist, 
			IGNORE_INDIAN_OWNER
		);
		GoodMaxProductionLocation foodTheBestLocation = null;
		for (GoodMaxProductionLocation loc : maxProductionForGoods) {
			if (!loc.getGoodsType().isFood()) {
				continue;
			}
			if (foodTheBestLocation == null || loc.getProduction() > foodTheBestLocation.getProduction()) {
				foodTheBestLocation = loc;
			}
		}
		if (foodTheBestLocation != null) {
			UnitType expertType = Specification.instance.expertUnitTypeByGoodType.get(foodTheBestLocation.getGoodsType().getId());
			if (expertType == null) {
				expertType = Specification.instance.unitTypes.getById(UnitType.FREE_COLONIST);
			}
			reqUnits.add(expertType);
			colonist.changeUnitType(expertType);
			addWorkerToColony(colonist, foodTheBestLocation);
			return true;
		}
		return false;
	}

	private boolean tryFindMaxValuableProducer(Unit colonist) {
		List<GoodMaxProductionLocation> maxProductionForGoods = colony.determinePotentialMaxGoodsProduction(
			goodsTypeToScore,
			colonist, 
			IGNORE_INDIAN_OWNER
		);
		
		GoodMaxProductionLocation theBestScoreLoc = null;
		int theBestScore = 0;
		
		for (GoodMaxProductionLocation goodMaxProductionLocation : maxProductionForGoods) {
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
			colonyScore += theBestScore;
			
			UnitType expertType = Specification.instance.expertUnitTypeByGoodType.get(theBestScoreLoc.getGoodsType().getId());
			if (expertType == null) {
				expertType = Specification.instance.unitTypes.getById(UnitType.FREE_COLONIST);
			}
			
			reqUnits.add(expertType);
			colonist.changeUnitType(expertType);
			
			addWorkerToColony(colonist, theBestScoreLoc);
			return true;
		}
		return false;
	}

	private void addWorkerToColony(Unit colonist, GoodMaxProductionLocation location) {
		if (location.getBuilding() != null) {
			colony.addWorkerToBuilding(location.getBuilding(), colonist);
		}
		if (location.getColonyTile() != null) {
			colony.addWorkerToTerrain(location.getColonyTile(), colonist, location.getGoodsType());
		}
		colony.updateColonyPopulation();
	}

	public boolean isConsumeWarehouseResources() {
		return consumeWarehouseResources;
	}

	public ColonyWorkerReq withConsumeWarehouseResources() {
		this.consumeWarehouseResources = true;
		return this;
	}
}
