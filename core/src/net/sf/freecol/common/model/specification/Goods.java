package net.sf.freecol.common.model.specification;

import net.sf.freecol.common.model.ObjectWithId;

public class Goods extends ObjectWithId {

	protected int amount;
	
	public Goods(String id) {
		super(id);
	}
	
	public Goods(String id, int amount) {
		this(id);
		this.amount = amount;
	}

	public int getAmount() {
		return amount;
	}
}
