package promitech.colonization.ai;

import java.util.List;

import net.sf.freecol.common.model.GoodsContainer;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.GoodsType;

class SettlementWarehouseScoreGoods {
	private final ObjectsListScore<GoodsType> settlementGoodsScore = new ObjectsListScore<GoodsType>();
	private final ObjectsListScore<Settlement> settlementsScore = new ObjectsListScore<Settlement>();
	
	private final int carierGoodsTotalSlots;
	private final Player player;
	private final Unit carrier;
	private final GoodsContainer tmpCarrierGoodsContainer = new GoodsContainer();
	
	SettlementWarehouseScoreGoods(Player player, Unit carrier) {
		this.player = player;
		this.carrier = carrier;
		
		carierGoodsTotalSlots = carrier.unitType.getSpace() - carrier.unitContainerSpaceTaken();
	}
	
	ObjectsListScore<Settlement> score(List<GoodsType> goodsType) {
		settlementsScore.clear();
		for (Settlement settlement : player.settlements.entities()) {
			settlementsScore.add(settlement, scoreSettlementGoods(settlement, goodsType));
		}
		settlementsScore.sortDescending();
		return settlementsScore;
	}
	
	private int scoreSettlementGoods(Settlement settlement, List<GoodsType> goodsType) {
		settlementAllGoodsScore(player, settlement, goodsType);
		
		tmpCarrierGoodsContainer.decreaseAllToZero();
		carrier.getGoodsContainer().cloneTo(tmpCarrierGoodsContainer);
		int score = 0;
		
		for (int i = 0; i < settlementGoodsScore.size(); i++) {
			GoodsType gt = settlementGoodsScore.obj(i);
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