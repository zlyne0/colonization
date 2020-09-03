package promitech.colonization.ai.goodsToSell;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.math.GridPoint2;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.ai.missions.TransportGoodsToSellMission;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.ai.ObjectsListScore;
import promitech.colonization.ai.Units;
import promitech.colonization.ai.ObjectsListScore.ObjectScore;

public class TransportGoodsToSellMissionPlaner {

	private static final int MIN_SETTLEMENT_SCORE = 200;

	private final List<GoodsType> goodsTypeToScore;
	private final PathFinder pathFinder;
	private final Game game;
	
	public TransportGoodsToSellMissionPlaner(Game game, PathFinder pathFinder) {
		this.pathFinder = pathFinder;
		this.game = game;
		
    	goodsTypeToScore = Specification.instance.goodsTypeToScoreByPrice;
	}
	
	public void plan(Player player) {
		List<Unit> carriers = Units.findCarrierTransportResources(game, player);
		if (carriers.isEmpty()) {
			return;
		}
		
		SettlementWarehouseScoreGoods scoreGoodsCalculator = new SettlementWarehouseScoreGoods(
			goodsTypeToScore, player, game.map, pathFinder
		);

		for (Unit carrier : carriers) {
			Tile startLocation = scoreColonyGoodsStartLocation(carrier);
			ObjectsListScore<Settlement> score = scoreGoodsCalculator.score(carrier, startLocation);

			if (!score.isEmpty()) {
				if (score.theBestScore().getScore() >= MIN_SETTLEMENT_SCORE) {
					Set<String> possibleSettlementsToVisit = createPossibleSettlementToVisit(score);

					TransportGoodsToSellMission mission = new TransportGoodsToSellMission(
						carrier,
						score.theBestScore().getObj(),
						possibleSettlementsToVisit
					);
					game.aiContainer.missionContainer(player).addMission(mission);

					// from scoreCalculator reduce settlement goods with the best score for free cargo space in carrier
					// because to avoid two carriers go to the same colony
					scoreGoodsCalculator.settlementGoodsReduce(score.theBestScore().getObj(), carrier);
				}
			}
		}
	}

	private Set<String> createPossibleSettlementToVisit(ObjectsListScore<Settlement> score) {
		Set<String> settlementIds = new HashSet<String>();
		for (ObjectScore<Settlement> settlementObjectScore : score) {
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
		return game.map.getTile(
			carrier.getOwner().getEntryLocationX(), 
			carrier.getOwner().getEntryLocationY()
		);		
	}

	public void determineNextSettlementToVisit(TransportGoodsToSellMission mission, Player player) {
		SettlementWarehouseScoreGoods scoreGoodsCalculator = new SettlementWarehouseScoreGoods(
			Specification.instance.goodsTypeToScoreByPrice, player, game.map, pathFinder
		);
		ObjectsListScore<Settlement> score = scoreGoodsCalculator.score(
			mission.getTransporter(), 
			mission.getTransporter().getTile()
		);
		
		mission.removeFirstSettlement();
		for (ObjectScore<Settlement> objectScore : score) {
			if (objectScore.getScore() >= MIN_SETTLEMENT_SCORE && mission.isSettlementPossibleToVisit(objectScore.getObj())) {
				mission.visitSettlementAsFirst(objectScore.getObj());
				break;
			}
		}
	}

}
