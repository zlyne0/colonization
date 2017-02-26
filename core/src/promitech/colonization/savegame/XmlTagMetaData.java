package promitech.colonization.savegame;

import java.lang.reflect.Method;

public class XmlTagMetaData<NODE_CLASS_TYPE> {
    protected String tagName;
    protected ObjectFromNodeSetter setter;
    protected Class<NODE_CLASS_TYPE> entityClass;
    protected String targetFieldName;
    
    protected XmlTagMetaData() {
    }

    public XmlTagMetaData(Class<NODE_CLASS_TYPE> entityClass, String fieldName) {
        this.tagName = tagNameForEntityClass(entityClass);
        this.entityClass = entityClass;
        this.targetFieldName = fieldName;
        this.setter = new FieldObjectFromNodeSetter(fieldName);
    }
    
    public XmlTagMetaData(Class<NODE_CLASS_TYPE> entityClass) {
        String tagName = tagNameForEntityClass(entityClass);
        this.tagName = tagName;
        this.entityClass = entityClass;
        this.setter = null;
    }
    
    public XmlTagMetaData(Class<NODE_CLASS_TYPE> entityClass, ObjectFromNodeSetter setter) {
        String tagName = tagNameForEntityClass(entityClass);
        this.tagName = tagName;
        this.entityClass = entityClass;
        this.setter = setter;
    }

    public XmlTagMetaData(String tagName, Class<NODE_CLASS_TYPE> entityClass, String targetFieldName) {
        this.tagName = tagName;
        this.entityClass = entityClass;
        this.targetFieldName = targetFieldName;
    }

    public XmlTagMetaData(String entityOverrideTagName, Class<NODE_CLASS_TYPE> entityClass, ObjectFromNodeSetter setter) {
        this.tagName = entityOverrideTagName;
        this.entityClass = entityClass;
        this.setter = setter;
    }
    
    public XmlNodeParser createXmlParser() {
        XmlNodeParser entityXmlParser = entityXmlParser(entityClass);
        entityXmlParser.xmlNodeMetaData = this;
        entityXmlParser.addSetter(setter);
        return entityXmlParser;
    }
    
    protected XmlNodeParser entityXmlParser(Class<NODE_CLASS_TYPE> entityClass) {
        Class<XmlNodeParser> xmlClass = getXmlClassFromEntityClass(entityClass);
        try {
            XmlNodeParser entityXmlParser = xmlClass.getDeclaredConstructor().newInstance();
            return entityXmlParser;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
    
    protected <T> Class<XmlNodeParser> getXmlClassFromEntityClass(Class<T> entityClass) {
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

    protected <T> String tagNameForEntityClass(Class<T> entityClass) {
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

