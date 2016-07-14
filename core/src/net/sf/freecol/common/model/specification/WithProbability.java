package net.sf.freecol.common.model.specification;

public interface WithProbability<T> {
	int getOccureProbability();
	T probabilityObject();
}
