package net.sf.freecol.common.model;

import java.util.HashMap;

public class MapIdEntities<T extends Identifiable> {
    private java.util.Map<String,T> entities = new HashMap<String,T>();
    
    public void add(T entity) {
        entities.put(entity.getId(), entity);
    }
    
    public T getById(String id) {
        T en = entities.get(id);
        if (en == null) {
            throw new IllegalArgumentException("can not find entity by id: " + id);
        }
        return en;
    }
}

