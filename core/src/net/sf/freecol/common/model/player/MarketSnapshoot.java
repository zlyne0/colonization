package net.sf.freecol.common.model.player;

import net.sf.freecol.common.model.MapIdEntities;

public class MarketSnapshoot {
	public final MapIdEntities<MarketChangePrice> prices = new MapIdEntities<MarketChangePrice>();
	
	public MarketSnapshoot(Market market) {
		for (MarketData md : market.marketGoods.entities()) {
			if (md.getGoodsType().isStorable()) {
				MarketChangePrice marketChangePrice = new MarketChangePrice(md.getId());
				marketChangePrice.setPricesBeforeTransaction(md);
				prices.add(marketChangePrice);
			}
		}
	}

}
