package promitech.colonization.savegame;

import java.lang.reflect.Field;

import net.sf.freecol.common.model.Identifiable;
import net.sf.freecol.common.model.MapIdEntities;

public class UniversalEntitySetter {

    public static void set(Object targetObject, String targetFieldName, Object entity) {
        
        try {
        	// it gets fields from superclasses but only public
            //Field field = targetObject.getClass().getField(targetFieldName);
            
        	// getDeclaredField does not get fields from superclasses 
        	Field field = targetObject.getClass().getDeclaredField(targetFieldName);
            field.setAccessible(true);
            
            
            if (MapIdEntities.class.equals(field.getType())) {
                MapIdEntities map = (MapIdEntities)field.get(targetObject);
                map.add((Identifiable)entity);
            } else {
            	if (entity.getClass().equals(field.getType())) {
            		field.set(targetObject, entity);
            	}
            }
        } catch (NoSuchFieldException e) {
            System.out.println("can not find field " + targetFieldName + ", on class " + targetObject.getClass());
            throw new IllegalArgumentException(e);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        
        
    }
}
