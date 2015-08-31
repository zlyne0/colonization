package promitech.colonization.savegame;

import net.sf.freecol.common.model.Identifiable;

public class XmlTagFieldMetaData extends XmlTagMetaData {
    private final String targetFieldName;
    
    public XmlTagFieldMetaData(Class<? extends Identifiable> entityClass, String fieldName) {
        this.tagName = tagNameForEntityClass(entityClass);
        this.entityClass = entityClass;
        this.targetFieldName = fieldName;
        this.setter = new FieldObjectFromNodeSetter(fieldName);
    }
}
