package net.sf.freecol.common.model.player;

import net.sf.freecol.common.model.player.Monarch.MonarchAction;
import net.sf.freecol.common.model.specification.WithProbability;

class MonarchActionProbability implements WithProbability<MonarchAction> {
	private final int weight;
	private final MonarchAction monarchAction;
	
	public MonarchActionProbability(int weight, MonarchAction moveAction) {
		this.weight = weight;
		this.monarchAction = moveAction;
	}
	
	@Override
	public int getOccureProbability() {
		return weight;
	}

	@Override
	public MonarchAction probabilityObject() {
		return monarchAction;
	}
}