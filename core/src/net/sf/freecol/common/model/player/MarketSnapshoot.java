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

	public void comparePrices(Player playingPlayer) {
		for (MarketData md : playingPlayer.market().marketGoods.entities()) {
			MarketChangePrice mcp = prices.getByIdOrNull(md.getId());
			if (mcp != null) {
				mcp.setPricesAfterTransaction(md);
				if (mcp.isMarketPriceChanged()) {
					MessageNotification goodsPriceChangeNotification = MessageNotification.createGoodsPriceChangeNotification(playingPlayer, mcp);
					playingPlayer.eventsNotifications.addMessageNotification(goodsPriceChangeNotification);
				}
			}
		}
	}
	
	
}
