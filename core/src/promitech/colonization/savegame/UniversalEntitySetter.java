package promitech.colonization.savegame;

import java.lang.reflect.Field;

import net.sf.freecol.common.model.Identifiable;
import net.sf.freecol.common.model.MapIdEntities;

public class UniversalEntitySetter {

    public static void set(Object targetObject, String targetFieldName, Identifiable entity) {
        
        try {
            Field field = targetObject.getClass().getField(targetFieldName);
            
            if (MapIdEntities.class.equals(field.getType())) {
                MapIdEntities map = (MapIdEntities)field.get(targetObject);
                map.add(entity);
            }
        } catch (NoSuchFieldException e) {
            System.out.println("can not find field " + targetFieldName + ", on class " + targetObject.getClass());
            throw new IllegalArgumentException(e);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        
        
    }
}
