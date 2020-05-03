package net.sf.freecol.common.model.specification;

import net.sf.freecol.common.model.Identifiable;

public class Goods implements Identifiable {

	protected int amount;
	protected GoodsType goodsType;
	
	public Goods(GoodsType goodsType, int amount) {
		this.goodsType = goodsType;
		this.amount = amount;
	}

	public int getAmount() {
		return amount;
	}

	@Override
	public String getId() {
		return goodsType.getId();
	}

	public GoodsType getType() {
		return goodsType;
	}
}
