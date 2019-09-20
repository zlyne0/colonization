package promitech.colonization.savegame;

import java.io.IOException;
import java.lang.reflect.Field;

import net.sf.freecol.common.model.Identifiable;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.ObjectWithInsertOrder;

public class MapIdEntitySetter<T, R extends Identifiable> implements ObjectFromNodeSetter<T,R> {
    private final String targetFieldName;
    
    public MapIdEntitySetter(String targetFieldName) {
        this.targetFieldName = targetFieldName;
    }
    
    @Override
    public void set(T targetObject, R entity) {
        MapIdEntities<R> target = getTargetMap(targetObject);
        target.add(entity);
        if (entity instanceof ObjectWithInsertOrder) {
            ((ObjectWithInsertOrder)entity).setInsertOrder(target.size());
        }
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
    
    public String toString() {
        return "MapIdEntitySetter.targetFieldName = " + targetFieldName;
    }

	@Override
	public void generateXml(T source, ChildObject2XmlCustomeHandler<R> xmlGenerator) throws IOException {
		throw new RuntimeException("not implemented");
	}
}
