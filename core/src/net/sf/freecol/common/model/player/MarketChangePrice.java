package net.sf.freecol.common.model.player;

import net.sf.freecol.common.model.Identifiable;

public class MarketChangePrice implements Identifiable {
	public int sellPriceBeforeTransaction;
	public int buyPriceBeforeTransaction;
	public int sellPriceAfterTransaction;
	public int buyPriceAfterTransaction;
	
	public String goodsTypeId;
	
	public MarketChangePrice() {
	}
	
	public MarketChangePrice(String goodsTypeId) {
		this.goodsTypeId = goodsTypeId;
	}
	
	@Override
	public String getId() {
		return goodsTypeId;
	}
	
	public boolean isMarketPriceChanged() {
		return sellPriceBeforeTransaction != sellPriceAfterTransaction || buyPriceBeforeTransaction != buyPriceAfterTransaction;
	}
	
	public boolean isPriceIncrease() {
		return sellPriceAfterTransaction > sellPriceBeforeTransaction;
	}
	
	public void setPricesBeforeTransaction(MarketData marketData) {
		sellPriceBeforeTransaction = marketData.getSalePrice();
		buyPriceBeforeTransaction = marketData.getBuyPrice();
	}

	public void setPricesAfterTransaction(MarketData marketData) {
		sellPriceAfterTransaction = marketData.getSalePrice();
		buyPriceAfterTransaction = marketData.getBuyPrice();
	}

	public void reset() {
        goodsTypeId = null;
		
    	sellPriceBeforeTransaction = 0;
    	buyPriceBeforeTransaction = 0;
    	sellPriceAfterTransaction = 0;
    	buyPriceAfterTransaction = 0;
	}
}