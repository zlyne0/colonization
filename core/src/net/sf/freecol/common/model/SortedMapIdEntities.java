package net.sf.freecol.common.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SortedMapIdEntities<T extends Identifiable> extends MapIdEntities<T> {

	private final Comparator<T> comparator;
    protected final List<T> sortedEntities = new ArrayList<T>();
    protected boolean sortRequired = true;
	
	public SortedMapIdEntities(Comparator<T> comparator) {
		this.comparator = comparator;
	}

	@Override
    public List<T> sortedEntities() {
    	if (sortRequired) {
    	    sortRequired = false;
    	    sortedEntities.clear();
    	    sortedEntities.addAll(entities.values());
	    	Collections.sort((List<T>)sortedEntities, comparator);
    	}
    	return sortedEntities;
    }
	
    @Override
    public void add(T entity) {
        super.add(entity);
        sortRequired = true;
    }
    
    @Override
    public void addAll(MapIdEntitiesReadOnly<T> parentEntities) {
        super.addAll(parentEntities);
        sortRequired = true;
    }
    
    @Override
    public void removeId(Identifiable element) {
        super.removeId(element);
        sortRequired = true;
    }
    
    @Override
    public void removeId(String id) {
        super.removeId(id);
        sortRequired = true;
    }
    
    @Override
    public void clear() {
        super.clear();
        sortRequired = true;
    }
}
