package promitech.colonization.savegame;

import java.lang.reflect.Field;
import java.util.List;

import net.sf.freecol.common.model.Identifiable;
import net.sf.freecol.common.model.MapIdEntities;

public class MapIdEntitySetter<T, R extends Identifiable> implements ObjectFromNodeSetter<T,R> {
    private final String targetFieldName;
    
    public MapIdEntitySetter(String targetFieldName) {
        this.targetFieldName = targetFieldName;
    }
    
    @Override
    public void set(T targetObject, R entity) {
        MapIdEntities<R> target = getTargetMap(targetObject);
        target.add(entity);
    }
    
    private MapIdEntities<R> getTargetMap(T targetObject) {
        if (targetObject == null) {
            throw new IllegalArgumentException("can not get MapIdEntities from field " + targetFieldName + " because target object is null");
        }
        try {
            //Field field = targetObject.getClass().getField(targetFieldName);
            Field field = targetObject.getClass().getDeclaredField(targetFieldName);
            field.setAccessible(true);
            return (MapIdEntities<R>)field.get(targetObject);
        } catch (NoSuchFieldException e) {
            System.out.println("can not find field " + targetFieldName + ", on class " + targetObject.getClass());
            throw new IllegalArgumentException(e);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
    
	@Override
	public List<R> get(T source) {
		throw new RuntimeException("not implemented");
	}
    
    public String toString() {
        return "MapIdEntitySetter.targetFieldName = " + targetFieldName;
    }
}
