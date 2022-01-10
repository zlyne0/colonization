package net.sf.freecol.common.model.colonyproduction;

import net.sf.freecol.common.model.player.Market;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.GoodsType;

interface ProductionValueComparator {

	public static final ProductionValueComparator byQuantity = new ProductionValueComparator() {
		@Override
		public boolean more(MaxGoodsProductionLocation maxProd, GoodsType key, int goodsQuantity) {
			if (maxProd == null) {
				return true;
			}
			return goodsQuantity > maxProd.production;
		}
	};
	
	public static class ByGoldValue implements ProductionValueComparator {
		
		private final Market market;
		
		public ByGoldValue(Player player) {
			this.market = player.market();
		}
		
		@Override
		public boolean more(MaxGoodsProductionLocation maxProd, GoodsType goodsType, int goodsQuantity) {
			if (maxProd == null) {
				return true;
			}
			int firstGold = market.getSalePrice(maxProd.goodsType, maxProd.production);
			int secondGold = market.getSalePrice(goodsType, goodsQuantity);
			return secondGold > firstGold;
		}
	}
	
	boolean more(MaxGoodsProductionLocation maxProd, GoodsType key, int goodsQuantity);

}
