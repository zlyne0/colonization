package promitech.colonization.ai;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.GridPoint2;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.ai.ObjectsListScore.ObjectScore;

public class TransportGoodsToSellMissionPlaner {
	
	private List<GoodsType> goodsTypeToScore = new ArrayList<GoodsType>();

	private final PathFinder pathFinder;
	
	public TransportGoodsToSellMissionPlaner(PathFinder pathFinder) {
		this.pathFinder = pathFinder;
		
    	Specification spec = Specification.instance;
    	goodsTypeToScore.add(spec.goodsTypes.getById("model.goods.sugar"));
    	goodsTypeToScore.add(spec.goodsTypes.getById("model.goods.tobacco"));
    	goodsTypeToScore.add(spec.goodsTypes.getById("model.goods.cotton"));
    	goodsTypeToScore.add(spec.goodsTypes.getById("model.goods.furs"));
    	goodsTypeToScore.add(spec.goodsTypes.getById("model.goods.ore"));
    	goodsTypeToScore.add(spec.goodsTypes.getById("model.goods.silver"));
    	goodsTypeToScore.add(spec.goodsTypes.getById("model.goods.rum"));
    	goodsTypeToScore.add(spec.goodsTypes.getById("model.goods.cigars"));
    	goodsTypeToScore.add(spec.goodsTypes.getById("model.goods.cloth"));
    	goodsTypeToScore.add(spec.goodsTypes.getById("model.goods.coats"));
		
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
			System.out.println("XXX " + carrier + " slots " + carrier.allGoodsCargoSlots());
			
			Tile startLocation = scoreColonyGoodsStartLocation(game.map, carrier);
			ObjectsListScore<Settlement> score = scoreGoodsCalculator.score(carrier, startLocation);
			
			System.out.println("XXX before minus");
			for (ObjectScore<Settlement> objectScore : score) {
				System.out.println("score " + objectScore.getObj().getId() + " " + objectScore.getScore());
			}
			if (!score.isEmpty()) {
				// from scoreCalculator reduce settlement goods with the best score for free cargo space in carrier
				scoreGoodsCalculator.settlementGoodsReduce(score.first().getObj(), carrier);
			}
			score = scoreGoodsCalculator.score(carrier, startLocation);
			
			System.out.println("XXX after minus");
			for (ObjectScore<Settlement> objectScore : score) {
				System.out.println("score " + objectScore.getObj().getId() + " " + objectScore.getScore());
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
	
}
