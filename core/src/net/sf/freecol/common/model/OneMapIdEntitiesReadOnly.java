package net.sf.freecol.common.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class OneMapIdEntitiesReadOnly<T extends Identifiable> implements MapIdEntitiesReadOnly<T> {

    private T obj;
    
    public OneMapIdEntitiesReadOnly(T obj) {
        this.obj = obj;
    }
    
    @Override
    public T getById(String id) {
        if (obj == null) {
            throw new IllegalArgumentException("can not find entity by id: " + id);
        }
        if (obj.getId().equals(id)) {
            return obj;
        }
        throw new IllegalArgumentException("can not find entity by id: " + id);
    }

    @Override
    public T first() {
        return obj;
    }

    @Override
    public T firstButNot(T exclude) {
        if (obj.getId().equals(exclude)) {
            return null;
        }
        return obj;
    }

    @Override
    public T getByIdOrNull(String id) {
        if (obj.getId().equals(id)) {
            return obj;
        }
        return null;
    }

    @Override
    public boolean isEmpty() {
        return obj == null;
    }

    @Override
    public boolean isNotEmpty() {
        return obj != null;
    }

    @Override
    public boolean containsId(String id) {
        return obj.getId().equals(id);
    }

    @Override
    public boolean containsId(Identifiable element) {
        return obj.getId().equals(element.getId());
    }

    @Override
    public int size() {
        if (obj != null) {
            return 1;
        }
        return 0;
    }

    @Override
    public Collection<T> entities() {
        List<T> arrayList = new ArrayList<T>();
        arrayList.add(obj);
        return arrayList;
    }

    @Override
    public Map<String, T> innerMap() {
        throw new IllegalStateException("not implemented");
    }
    
}
