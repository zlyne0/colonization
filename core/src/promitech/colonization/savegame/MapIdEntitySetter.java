package promitech.colonization.savegame;

import java.lang.reflect.Field;

import net.sf.freecol.common.model.Identifiable;
import net.sf.freecol.common.model.MapIdEntities;

public class MapIdEntitySetter extends ObjectFromNodeSetter {
    private MapIdEntities<? extends Identifiable> targetMap;
    private final String targetFieldName;
    private final XmlNodeParser parent;
    
    public MapIdEntitySetter(XmlNodeParser parent, String targetFieldName) {
        this.parent = parent;
        this.targetFieldName = targetFieldName;
    }
    
    @Override
    public void set(Identifiable entity) {
        MapIdEntities target = getTargetMap();
        target.add(entity);
    }
    
    private MapIdEntities<? extends Identifiable> getTargetMap() {
        if (targetMap != null) {
            return targetMap;
        }
        try {
            Identifiable parentNodeObject = parent.nodeObject;
            Field field = parentNodeObject.getClass().getField(targetFieldName);
            targetMap = (MapIdEntities)field.get(parentNodeObject);
            return targetMap;
        } catch (NoSuchFieldException e) {
            System.out.println("can not find field " + targetFieldName + ", on class " + parent.nodeObject.getClass());
            throw new IllegalArgumentException(e);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
    
    public void reset() {
        targetMap = null;
    }
}
