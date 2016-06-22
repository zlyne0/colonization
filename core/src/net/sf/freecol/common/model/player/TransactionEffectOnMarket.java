package net.sf.freecol.common.model.player;

public class TransactionEffectOnMarket {
	public int goodsModifiedMarket;
	public int beforePrice;
	public int afterPrice;
	
	public boolean priceChanged() {
		return beforePrice != afterPrice;
	}

    public void reset() {
        beforePrice = 0;
        afterPrice = 0;
    }
}

