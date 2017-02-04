package promitech.colonization.savegame;

import net.sf.freecol.common.model.Identifiable;
import net.sf.freecol.common.model.MapIdEntities;

public class XmlTagMapIdEntitiesMetaData extends XmlTagMetaData {
    protected boolean possesWrapperTag;
    protected String fieldName;

    public XmlTagMapIdEntitiesMetaData(String wrapperTag, String fieldName, Class<? extends Identifiable> entityClass) {
        this.tagName = wrapperTag;
        this.fieldName = fieldName;
        this.entityClass = entityClass;
        this.possesWrapperTag = true;
    }
    
    public XmlTagMapIdEntitiesMetaData(String fieldName, Class<? extends Identifiable> entityClass) {
        String tagName = tagNameForEntityClass(entityClass);
        this.tagName = tagName;
        this.fieldName = fieldName;
        this.entityClass = entityClass;
        this.possesWrapperTag = false;
    }
    
    @Override
    public XmlNodeParser createXmlParser() {
        XmlNodeParser xmlNodeParser;
        if (possesWrapperTag) {
            xmlNodeParser = new MapIdEntities.Xml(tagName, fieldName, entityClass);
        } else {
            xmlNodeParser = new MapIdEntities.Xml(fieldName, entityClass);
        }
        return xmlNodeParser;
    }

    public XmlNodeParser createEntityXmlParser() {
    	return entityXmlParser(entityClass);
    }
    
    public boolean isPossesWrapperTag() {
        return possesWrapperTag;
    }

    public String getFieldName() {
        return fieldName;
    }
}

