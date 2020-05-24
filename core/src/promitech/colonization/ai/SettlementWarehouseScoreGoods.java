package promitech.colonization.ai;

import java.util.List;

import net.sf.freecol.common.model.GoodsContainer;
import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.GoodsType;

class SettlementWarehouseScoreGoods {
	private final ObjectsListScore<GoodsType> settlementGoodsScore;
	private final ObjectsListScore<Settlement> settlementsScore;
	
	private final int carierGoodsTotalSlots;
	private final Player player;
	private final Unit carrier;
	private final GoodsContainer tmpCarrierGoodsContainer = new GoodsContainer();
	
	SettlementWarehouseScoreGoods(Player player, Unit carrier) {
		this.player = player;
		this.carrier = carrier;
		
		carierGoodsTotalSlots = carrier.unitType.getSpace() - carrier.unitContainerSpaceTaken();
		
		settlementGoodsScore = new ObjectsListScore<GoodsType>(Specification.instance.goodsTypes.size());
		settlementsScore = new ObjectsListScore<Settlement>(player.settlements.size());
	}
	
	ObjectsListScore<Settlement> score(List<GoodsType> goodsType) {
		settlementsScore.clear();
		for (Settlement settlement : player.settlements.entities()) {
			settlementsScore.add(settlement, scoreSettlementGoods(settlement, goodsType));
		}
		settlementsScore.sortDescending();
		return settlementsScore;
	}

	ObjectsListScore<Settlement> score(List<GoodsType> goodsType, Map map, PathFinder rangeCalculator) {
		Tile carrierLocation = carrier.getTileLocationOrNull();
		if (carrierLocation == null) {
			throw new IllegalArgumentException("carrier location should be tile");
		}
		rangeCalculator.generateRangeMap(map, carrierLocation, carrier, false);
		
		settlementsScore.clear();
		for (Settlement settlement : player.settlements.entities()) {
			int settlementScore = settlementTurnsScore(
				scoreSettlementGoods(settlement, goodsType), 
				rangeCalculator.turnsCost(settlement.tile)
			);
			settlementsScore.add(settlement, settlementScore);
		}
		settlementsScore.sortDescending();
		return settlementsScore;
	}
	
	int settlementTurnsScore(int settlementScore, int turns) {
		if (turns > 100) {
			turns = 100;
		}
		return settlementScore * (100 - (turns*15))/100;
	}
	
	private int scoreSettlementGoods(Settlement settlement, List<GoodsType> goodsType) {
		settlementAllGoodsScore(player, settlement, goodsType);
		
		tmpCarrierGoodsContainer.decreaseAllToZero();
		carrier.getGoodsContainer().cloneTo(tmpCarrierGoodsContainer);
		int score = 0;
		
		for (int i = 0; i < settlementGoodsScore.size(); i++) {
			GoodsType gt = settlementGoodsScore.get(i).getObj();
			int maxGoodsForContainer = tmpCarrierGoodsContainer.maxGoodsAmountToFillFreeSlots(gt.getId(), carierGoodsTotalSlots);
			int goodsToScore = Math.min(maxGoodsForContainer, settlement.getGoodsContainer().goodsAmount(gt));
			if (goodsToScore > 0) {
				score += player.market().getSalePrice(gt, goodsToScore);
				tmpCarrierGoodsContainer.increaseGoodsQuantity(gt, goodsToScore);
			}
		}
		return score;
	}
	
	private void settlementAllGoodsScore(Player player, Settlement settlement, List<GoodsType> goodsType) {
		settlementGoodsScore.clear();
		for (GoodsType gt : goodsType) {
			int amount = settlement.getGoodsContainer().goodsAmount(gt);
			if (amount <= 0) {
				continue;
			}
			int salePrice = player.market().getSalePrice(gt, amount);
			settlementGoodsScore.add(gt, salePrice);
		}
		settlementGoodsScore.sortDescending();
	}
}