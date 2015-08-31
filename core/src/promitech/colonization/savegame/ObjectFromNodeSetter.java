package promitech.colonization.savegame;

import net.sf.freecol.common.model.Identifiable;

public interface ObjectFromNodeSetter<T extends Identifiable,R extends Identifiable> {
    public void set(T target, R entity);
}
