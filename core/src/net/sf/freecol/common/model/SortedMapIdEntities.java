package net.sf.freecol.common.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SortedMapIdEntities<T extends Identifiable> extends MapIdEntities<T> {

	private Comparator<T> comparator;
	
	public SortedMapIdEntities(Comparator<T> comparator) {
		this.comparator = comparator;
	}
	
    public List<T> sortedEntities() {
    	if (sortedEntities == null) {
	    	sortedEntities = new ArrayList<T>(entities.values());
	    	Collections.sort((List<T>)sortedEntities, comparator);
    	}
    	return sortedEntities;
    }
	
}
