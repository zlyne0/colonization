package promitech.colonization.savegame;

import net.sf.freecol.common.model.Identifiable;

public abstract class ObjectFromNodeSetter<T extends Identifiable,R extends Identifiable> {
    public abstract void set(T target, R entity);
}
