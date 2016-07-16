package net.sf.freecol.common.model.player;

import net.sf.freecol.common.model.specification.GoodsType;

public class TransactionEffectOnMarket {
	public int sellPriceBeforeTransaction;
	public int buyPriceBeforeTransaction;
	public int sellPriceAfterTransaction;
	public int buyPriceAfterTransaction;
	
	public String goodsTypeId;
	public int quantity;
	public int sellPrice;
	
	public int grossPrice;
	public int netPrice;
	public int taxPercent;
	public int tax;
	
	public boolean isMarketPriceChanged() {
		return sellPriceBeforeTransaction != sellPriceAfterTransaction || buyPriceBeforeTransaction != buyPriceAfterTransaction;
	}

	public boolean isPriceIncrease() {
		return sellPriceAfterTransaction > sellPriceBeforeTransaction;
	}
	
    public void reset() {
    	sellPriceBeforeTransaction = 0;
    	buyPriceBeforeTransaction = 0;
    	sellPriceAfterTransaction = 0;
    	buyPriceAfterTransaction = 0;

        goodsTypeId = null;
        quantity = 0;
        sellPrice = 0;
        
        grossPrice = 0;
        netPrice = 0;
        taxPercent = 0;
        tax = 0;
    }

	public void sell(GoodsType goodsType, int goodsAmount, MarketData marketData, int theTaxPercent) {
		goodsTypeId = goodsType.getId();
		quantity = goodsAmount;
		taxPercent = theTaxPercent;
		
		sellPrice = marketData.getSalePrice();
		grossPrice = goodsAmount * sellPrice;
	    
        netPrice = ((100 - taxPercent) * grossPrice) / 100;

        tax = grossPrice - netPrice;
	}

	public void setPricesBeforeTransaction(MarketData marketData) {
		sellPriceBeforeTransaction = marketData.getSalePrice();
		buyPriceBeforeTransaction = marketData.getBuyPrice();
	}

	public void setPricesAfterTransaction(MarketData marketData) {
		sellPriceAfterTransaction = marketData.getSalePrice();
		buyPriceAfterTransaction = marketData.getBuyPrice();
	}
}

