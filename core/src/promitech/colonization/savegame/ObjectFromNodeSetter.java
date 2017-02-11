package promitech.colonization.savegame;

import java.util.Collection;

public interface ObjectFromNodeSetter<T,R> {
    public void set(T target, R entity);
    public Collection<R> get(T source);
}
