package promitech.colonization.savegame;

import java.lang.reflect.Field;

import net.sf.freecol.common.model.Identifiable;

public class FieldObjectFromNodeSetter implements ObjectFromNodeSetter {
    private String targetFieldName;
    
    public FieldObjectFromNodeSetter(String targetFieldName) {
        this.targetFieldName = targetFieldName;
    }
    
    @Override
    public void set(Identifiable target, Identifiable entity) {
        try {
            Field field = target.getClass().getDeclaredField(targetFieldName);
            field.setAccessible(true);
            field.set(target, entity);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
    
}
