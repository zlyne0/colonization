package net.sf.freecol.common.model.ai.missions.goodsToSell;

import com.badlogic.gdx.math.GridPoint2;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.GoodsType;

import java.util.HashSet;
import java.util.Set;

import promitech.colonization.ai.MissionPlanStatus;
import promitech.colonization.ai.score.ObjectScoreList;

public class TransportGoodsToSellMissionPlaner {

	public static final int MIN_SETTLEMENT_SCORE = 200;

	private final MapIdEntities<GoodsType> goodsTypeToScore;
	private final PathFinder pathFinder;
	private final Game game;
	
	public TransportGoodsToSellMissionPlaner(Game game, PathFinder pathFinder) {
		this.pathFinder = pathFinder;
		this.game = game;
		
    	goodsTypeToScore = Specification.instance.goodsTypeToScoreByPrice;
	}
	
	public MissionPlanStatus plan(Unit navyUnit) {
		if (!navyUnit.hasSpaceForAdditionalCargo()) {
			// TODO: fix: has cargo and no mission?
			return MissionPlanStatus.NO_MISSION;
		}
		Player player = navyUnit.getOwner();
		SettlementWarehouseScoreGoods scoreGoodsCalculator = new SettlementWarehouseScoreGoods(
			goodsTypeToScore, player, game.map, pathFinder
		);
		return singleUnitPlaner(player, scoreGoodsCalculator, navyUnit);
	}

	private MissionPlanStatus singleUnitPlaner(Player player, SettlementWarehouseScoreGoods scoreGoodsCalculator, Unit carrier) {
		Tile startLocation = scoreColonyGoodsStartLocation(carrier);
		ObjectScoreList<Settlement> score = scoreGoodsCalculator.score(carrier, startLocation);
		MissionPlanStatus result = MissionPlanStatus.NO_MISSION;

		if (!score.isEmpty()) {
			ObjectScoreList.ObjectScore<Settlement> theBestScore = score.theBestScore();
			if (theBestScore.getScore() >= MIN_SETTLEMENT_SCORE) {
				Set<String> possibleSettlementsToVisit = createPossibleSettlementToVisit(score);

				TransportGoodsToSellMission mission = new TransportGoodsToSellMission(
					carrier,
					theBestScore.getObj(),
					possibleSettlementsToVisit
				);
				game.aiContainer.missionContainer(player).addMission(mission);
				result = MissionPlanStatus.MISSION_CREATED;

				// from scoreCalculator reduce settlement goods with the best score for free cargo space in carrier
				// because to avoid two carriers go to the same colony
				scoreGoodsCalculator.settlementGoodsReduce(theBestScore.getObj(), carrier);
			}
		}
		return result;
	}

	private Set<String> createPossibleSettlementToVisit(ObjectScoreList<Settlement> score) {
		Set<String> settlementIds = new HashSet<String>();
		for (ObjectScoreList.ObjectScore<Settlement> settlementObjectScore : score) {
			settlementIds.add(settlementObjectScore.getObj().getId());
		}
		settlementIds.remove(score.theBestScore().getObj().getId());
		return settlementIds;
	}

	private Tile scoreColonyGoodsStartLocation(Unit carrier) {
		Tile tileLocation = carrier.getTileLocationOrNull();
		if (tileLocation != null) {
			return tileLocation;
		}
		GridPoint2 enterHighSea = carrier.getEnterHighSea();
		if (enterHighSea != null) {
			return game.map.getSafeTile(enterHighSea);
		}
		return game.map.getSafeTile(carrier.getOwner().getEntryLocation());
	}

	public void determineNextSettlementToVisit(TransportGoodsToSellMission mission, Player player) {
		SettlementWarehouseScoreGoods scoreGoodsCalculator = new SettlementWarehouseScoreGoods(
			Specification.instance.goodsTypeToScoreByPrice, player, game.map, pathFinder
		);
		ObjectScoreList<Settlement> score = scoreGoodsCalculator.score(
			mission.getTransporter(), 
			mission.getTransporter().getTile()
		);
		
		mission.removeFirstSettlement();
		for (ObjectScoreList.ObjectScore<Settlement> objectScore : score) {
			if (objectScore.getScore() >= MIN_SETTLEMENT_SCORE && mission.isSettlementPossibleToVisit(objectScore.getObj())) {
				mission.visitSettlementAsFirst(objectScore.getObj());
				break;
			}
		}
	}

}
