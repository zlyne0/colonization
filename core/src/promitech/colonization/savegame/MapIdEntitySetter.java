package promitech.colonization.savegame;

import java.lang.reflect.Field;

import net.sf.freecol.common.model.Identifiable;
import net.sf.freecol.common.model.MapIdEntities;

public class MapIdEntitySetter extends ObjectFromNodeSetter {
    private MapIdEntities<? extends Identifiable> targetMap;
    private final String targetFieldName;
    
    public MapIdEntitySetter(String targetFieldName) {
        this.targetFieldName = targetFieldName;
    }
    
    @Override
    public void set(Identifiable targetObject, Identifiable entity) {
        MapIdEntities target = getTargetMap(targetObject);
        target.add(entity);
    }
    
    private MapIdEntities<? extends Identifiable> getTargetMap(Identifiable targetObject) {
        if (targetMap != null) {
            return targetMap;
        }
        if (targetObject == null) {
            throw new IllegalArgumentException("can not get MapIdEntities from field " + targetFieldName + " because target object is null");
        }
        try {
            Field field = targetObject.getClass().getField(targetFieldName);
            targetMap = (MapIdEntities)field.get(targetObject);
            return targetMap;
        } catch (NoSuchFieldException e) {
            System.out.println("can not find field " + targetFieldName + ", on class " + targetObject.getClass());
            throw new IllegalArgumentException(e);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
    
    public void reset() {
        targetMap = null;
    }
}
