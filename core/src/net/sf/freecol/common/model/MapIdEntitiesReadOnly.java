package net.sf.freecol.common.model;

import java.util.Collection;
import java.util.List;

public interface MapIdEntitiesReadOnly<T extends Identifiable> {

    T getById(String id);

    List<T> sortedEntities();
    
    T first();
    
    T firstButNot(T exclude);
    
    T getByIdOrNull(String id);
    
    boolean isEmpty();

    boolean isNotEmpty();
    
    boolean containsId(String id);
    
    boolean containsId(Identifiable element);
    
    int size();
    
    Collection<T> entities();

    java.util.Map<String,T> innerMap();
    
}
