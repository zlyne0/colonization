package promitech.colonization.savegame;

import java.lang.reflect.Method;

import net.sf.freecol.common.model.Identifiable;

public class XmlTagMetaData {
    protected String tagName;
    protected ObjectFromNodeSetter setter;
    protected Class<? extends Identifiable> entityClass;
    protected String targetFieldName;
    
    protected XmlTagMetaData() {
    }

    public XmlTagMetaData(Class<? extends Identifiable> entityClass, ObjectFromNodeSetter setter) {
        String tagName = tagNameForEntityClass(entityClass);
        this.tagName = tagName;
        this.entityClass = entityClass;
        this.setter = setter;
    }

    public XmlTagMetaData(String tagName, Class<? extends Identifiable> entityClass, String targetFieldName) {
        this.tagName = tagName;
        this.entityClass = entityClass;
        this.targetFieldName = targetFieldName;
    }

    public XmlNodeParser createXmlParser() {
        XmlNodeParser entityXmlParser = entityXmlParser(entityClass);
        entityXmlParser.xmlNodeMetaData = this;
        entityXmlParser.addSetter(setter);
        return entityXmlParser;
    }
    
    private XmlNodeParser entityXmlParser(Class<? extends Identifiable> entityClass) {
        Class<XmlNodeParser> xmlClass = getXmlClassFromEntityClass(entityClass);
        try {
            XmlNodeParser entityXmlParser = xmlClass.getDeclaredConstructor().newInstance();
            return entityXmlParser;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
    
    protected Class<XmlNodeParser> getXmlClassFromEntityClass(Class<? extends Identifiable> entityClass) {
        Class<XmlNodeParser> xmlClazz = null;
        for (Class cz : entityClass.getDeclaredClasses()) {
            if (cz.getSimpleName().equals("Xml")) {
                xmlClazz = cz;
            }
        }
        if (xmlClazz == null) {
            throw new IllegalStateException("can not find inner Xml class in " + entityClass);
        }
        return xmlClazz;
    }

    protected String tagNameForEntityClass(Class<? extends Identifiable> entityClass) {
        try {
            Class<XmlNodeParser> xmlClass = getXmlClassFromEntityClass(entityClass);
            Method tagNameMethod = xmlClass.getDeclaredMethod("tagName");
            String tagName = (String)tagNameMethod.invoke(null);
            return tagName;
        } catch (Exception e) {
            throw new IllegalStateException("can not invoke tagName method in Xml for entity " + entityClass);
        }
    }

    public String getTagName() {
        return tagName;
    }
    
    public String toString() {
    	return "tagName = " + tagName + ", entityClass = " + entityClass + ", targetField = " + targetFieldName;
    }
}

