package promitech.colonization.ai.goodsToSell;

import java.util.List;

import com.badlogic.gdx.math.GridPoint2;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Map;
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
	
	private final List<GoodsType> goodsTypeToScore;
	private final PathFinder pathFinder;
	
	public TransportGoodsToSellMissionPlaner(PathFinder pathFinder) {
		this.pathFinder = pathFinder;
		
    	goodsTypeToScore = Specification.instance.goodsTypeToScoreByPrice;
	}
	
	public void plan(Game game, Player player) {
		List<Unit> carriers = Units.findCarrierTransportResources(game, player);
		if (carriers.isEmpty()) {
			return;
		}
		
		SettlementWarehouseScoreGoods scoreGoodsCalculator = new SettlementWarehouseScoreGoods(
			goodsTypeToScore, player, game.map, pathFinder
		);
		
		for (Unit carrier : carriers) {
			Tile startLocation = scoreColonyGoodsStartLocation(game.map, carrier);
			ObjectsListScore<Settlement> score = scoreGoodsCalculator.score(carrier, startLocation);
			
			System.out.println("brefore redure carrier " + carrier + " " + carrier.allGoodsCargoSlots());
			for (ObjectScore<Settlement> objectScore : score) {
				System.out.println("settlement " + objectScore.getObj() + " score " + objectScore.getScore());
			}
			if (!score.isEmpty()) {
				// from scoreCalculator reduce settlement goods with the best score for free cargo space in carrier
				scoreGoodsCalculator.settlementGoodsReduce(score.theBestScore().getObj(), carrier);
			}
			score = scoreGoodsCalculator.score(carrier, startLocation);
			System.out.println("after reduce carrier " + carrier + " " + carrier.allGoodsCargoSlots());
			for (ObjectScore<Settlement> objectScore : score) {
				System.out.println("settlement " + objectScore.getObj() + " score " + objectScore.getScore());
			}
		}
	}
	
	public Tile scoreColonyGoodsStartLocation(Map map, Unit carrier) {
		Tile tileLocation = carrier.getTileLocationOrNull();
		if (tileLocation != null) {
			return tileLocation;
		}
		GridPoint2 enterHighSea = carrier.getEnterHighSea();
		if (enterHighSea != null) {
			return map.getSafeTile(enterHighSea);
		}
		return map.getTile(
			carrier.getOwner().getEntryLocationX(), 
			carrier.getOwner().getEntryLocationY()
		);		
	}

	public void determineNextSettlementToVisit(Game game, TransportGoodsToSellMission mission, Player player) {
		SettlementWarehouseScoreGoods scoreGoodsCalculator = new SettlementWarehouseScoreGoods(
			Specification.instance.goodsTypeToScoreByPrice, player, game.map, pathFinder
		);
		ObjectsListScore<Settlement> score = scoreGoodsCalculator.score(
			mission.getTransporter(), 
			mission.getTransporter().getTile()
		);
		
		mission.removeFirstSettlement();
		for (ObjectScore<Settlement> objectScore : score) {
			if (objectScore.getScore() >= 200 && mission.isSettlementPossibleToVisit(objectScore.getObj())) {
				mission.visitSettlementAsFirst(objectScore.getObj());
				break;
			}
		}
	}
	
}
