package promitech.colonization.ai.goodsToSell;

import java.util.List;

import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.Unit.UnitState;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.AbstractGoods;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.ai.ObjectsListScore;
import promitech.colonization.ai.ObjectsListScore.ObjectScore;

public class GoodsLoader {
	private final Player player;
	private final List<GoodsType> goodsToScore;
	
	private boolean loaded;
	
	public GoodsLoader(Player player, List<GoodsType> goodsToScore) {
		this.player = player;
		this.goodsToScore = goodsToScore;
	}
	
	public void load(Settlement settlement, Unit carrier) {
		loaded = false;
		
		// first load containing goods to fill slots, no score need
		loadCarrierContainingGoods(settlement, carrier);
		loadMostValuableGoods(settlement, carrier);
		
		if (loaded && !carrier.hasFullMovesPoints()) {
			carrier.reduceMovesLeftToZero();
			carrier.setState(UnitState.SKIPPED);
		}
	}

	private void loadCarrierContainingGoods(Settlement settlement, Unit carrier) {
		List<AbstractGoods> slotedGoods = carrier.getGoodsContainer().slotedGoods();
		// for goods in cargo can load without score
		for (AbstractGoods goodsSlot : slotedGoods) {
			// free space on carrier and settlement amount
			int amountToFillSlot = Math.min(
				goodsSlot.amountToFillSlot(), 
				settlement.getGoodsContainer().goodsAmount(goodsSlot.getTypeId())
			);
			
			if (amountToFillSlot > 0) {
				loaded = true;
				settlement.getGoodsContainer().transferGoods(
					goodsSlot.getTypeId(),
					amountToFillSlot,
					carrier.getGoodsContainer()
				);
			}
		}
	}
	
	private void loadMostValuableGoods(Settlement settlement, Unit carrier) {
		SettlementGoods settlementGoods = new SettlementGoods(settlement, player.market(), goodsToScore);
		ObjectsListScore<GoodsType> goodsScores = new ObjectsListScore<GoodsType>(goodsToScore.size());
		settlementGoods.score(goodsScores);
		
		for (ObjectScore<GoodsType> goodsTypeScore : goodsScores) {
			int amountToFillSlot = Math.min(
				carrier.maxGoodsAmountToFillFreeSlots(goodsTypeScore.getObj().getId()), 
				settlement.getGoodsContainer().goodsAmount(goodsTypeScore.getObj())
			);
			if (amountToFillSlot > 0) {
				loaded = true;
				settlement.getGoodsContainer().transferGoods(
					goodsTypeScore.getObj(),
					amountToFillSlot,
					carrier.getGoodsContainer()
				);
			}
		}
	}
}