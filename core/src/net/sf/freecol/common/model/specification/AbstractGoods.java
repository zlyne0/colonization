package net.sf.freecol.common.model.specification;

public class AbstractGoods {
	private int amount;
	private GoodsType type;

	public AbstractGoods(GoodsType type, int amount) {
	    this.amount = amount;
	    this.type = type;
	}
	
	public int getAmount() {
		return amount;
	}

	public GoodsType getType() {
		return type;
	}

}
