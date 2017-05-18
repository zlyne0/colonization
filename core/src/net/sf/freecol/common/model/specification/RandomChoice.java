package net.sf.freecol.common.model.specification;

public class RandomChoice<T> implements WithProbability<T> {

	private final T obj;
	private final int prob;
	
	public RandomChoice(T obj, int prob) {
		this.obj = obj;
		this.prob = prob;
	}
	
	@Override
	public int getOccureProbability() {
		return prob;
	}

	@Override
	public T probabilityObject() {
		return obj;
	}

}
