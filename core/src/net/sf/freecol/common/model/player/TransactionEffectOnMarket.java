package net.sf.freecol.common.model.player;

import net.sf.freecol.common.model.specification.GoodsType;

public class TransactionEffectOnMarket extends MarketChangePrice {
	public int quantity;
	public int sellPrice;
	
	public int grossPrice;
	public int netPrice;
	public int taxPercent;
	public int tax;
	
    public void reset() {
    	super.reset();

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
}

