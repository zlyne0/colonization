package net.sf.freecol.common.model.ai.missions.goodsToSell;

import com.badlogic.gdx.utils.ObjectIntMap;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.ProductionSummary;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.GoodsType;

import java.util.Collections;

import promitech.colonization.ai.MissionPlanStatus;
import promitech.colonization.ai.score.ObjectScoreList;

public class TransportGoodsToSellMissionPlaner {

	public static final int MIN_SETTLEMENT_SCORE = 200;
	private static final int CARGO_AMOUNT_WORTH_TO_SELL = 80;

	private final MapIdEntities<GoodsType> goodsTypeToScore;
	private final PathFinder pathFinder;
	private final Game game;
	
	public TransportGoodsToSellMissionPlaner(Game game, PathFinder pathFinder) {
		this.pathFinder = pathFinder;
		this.game = game;
		
    	goodsTypeToScore = Specification.instance.goodsTypeToScoreByPrice;
	}

	/**
	 * Early stage of game, sell goods to buy unit
	 */
	public MissionPlanStatus planSellGoodsToBuyUnit(Unit navyUnit) {
		Player player = navyUnit.getOwner();

		if (player.units.size() > 5) {
			return MissionPlanStatus.NO_MISSION;
		}

		ColoniesProductionValue coloniesProductionValue = new ColoniesProductionValue(player);
		Settlement firstSettlement = coloniesProductionValue.findSettlementWorthTakeGoodsToBuyUnit(navyUnit);
		if (firstSettlement == null) {
			return MissionPlanStatus.NO_MISSION;
		}

		TransportGoodsToSellMission mission = new TransportGoodsToSellMission(
			navyUnit,
			firstSettlement
		);
		game.aiContainer.missionContainer(player).addMission(mission);
		return MissionPlanStatus.MISSION_CREATED;
	}

	public MissionPlanStatus plan(Unit navyUnit) {
		Player player = navyUnit.getOwner();
		if (!navyUnit.hasSpaceForAdditionalCargo()) {
			TransportGoodsToSellMission mission = TransportGoodsToSellMission.sellTransportedCargoInEurope(navyUnit);
			game.aiContainer.missionContainer(player).addMission(mission);
			return MissionPlanStatus.MISSION_CREATED;
		}
		SettlementWarehouseScoreGoods scoreGoodsCalculator = new SettlementWarehouseScoreGoods(
			goodsTypeToScore, player, game.map, pathFinder, Collections.<String>emptySet()
		);
		return singleUnitPlaner(player, scoreGoodsCalculator, navyUnit);
	}

	private MissionPlanStatus singleUnitPlaner(Player player, SettlementWarehouseScoreGoods scoreGoodsCalculator, Unit carrier) {
		Tile startLocation = scoreColonyGoodsStartLocation(carrier);
		ObjectScoreList<Settlement> score = scoreGoodsCalculator.score(carrier, startLocation);
		if (score.isEmpty()) {
			return MissionPlanStatus.NO_MISSION;
		}

		ObjectScoreList.ObjectScore<Settlement> theBestScore = score.theBestScore();
		if (theBestScore.score() >= MIN_SETTLEMENT_SCORE) {
			TransportGoodsToSellMission mission = new TransportGoodsToSellMission(
				carrier,
				theBestScore.getObj()
			);
			game.aiContainer.missionContainer(player).addMission(mission);

			// from scoreCalculator reduce settlement goods with the best score for free cargo space in carrier
			// because to avoid two carriers go to the same colony
			scoreGoodsCalculator.settlementGoodsReduce(theBestScore.getObj(), carrier);
			return MissionPlanStatus.MISSION_CREATED;
		}
		return MissionPlanStatus.NO_MISSION;
	}

	private Tile scoreColonyGoodsStartLocation(Unit carrier) {
		return carrier.positionRelativeToMap(game.map);
	}

	public void determineNextSettlementToVisit(TransportGoodsToSellMission mission, Player player) {
		SettlementWarehouseScoreGoods scoreGoodsCalculator = new SettlementWarehouseScoreGoods(
			Specification.instance.goodsTypeToScoreByPrice, player, game.map, pathFinder, mission.getVisitedSettlements()
		);
		ObjectScoreList<Settlement> score = scoreGoodsCalculator.score(
			mission.getTransporter(), 
			mission.getTransporter().getTile()
		);
		
		mission.removeFirstSettlement();
		for (ObjectScoreList.ObjectScore<Settlement> objectScore : score) {
			if (objectScore.score() >= MIN_SETTLEMENT_SCORE && mission.isSettlementPossibleToVisit(objectScore.getObj())) {
				mission.visitSettlementAsFirst(objectScore.getObj());
				break;
			}
		}
	}

	public CargoAmountWaitingForTransport potentialCargoAmountWaitingForTransport(Player player) {
		CargoAmountWaitingForTransport amount = new CargoAmountWaitingForTransport();

		for (Settlement settlement : player.settlements) {
			for (ObjectIntMap.Entry<String> goodsEntry : settlement.getGoodsContainer().entries()) {
				if (goodsTypeToScore.containsId(goodsEntry.key)) {
					amount.cargoValue += player.market().getSalePrice(goodsEntry.key, goodsEntry.value);

					if (goodsEntry.value >= CARGO_AMOUNT_WORTH_TO_SELL) {
						amount.cargoSlots += ProductionSummary.slotsForQuantity(goodsEntry.value);
					}
				}
			}
		}
		return amount;
	}

	public static class CargoAmountWaitingForTransport {
		int cargoSlots = 0;
		int cargoValue = 0;

		public int getCargoSlots() {
			return cargoSlots;
		}

		public int getCargoValue() {
			return cargoValue;
		}
	}
}
