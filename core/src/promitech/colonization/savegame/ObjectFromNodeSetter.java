package promitech.colonization.savegame;

public interface ObjectFromNodeSetter<T,R> {
    public void set(T target, R entity);
}
