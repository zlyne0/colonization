package net.sf.freecol.common.model;

import java.util.Collection;

public interface MapIdEntitiesReadOnly<T extends Identifiable> {

    T getById(String id);
    
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
