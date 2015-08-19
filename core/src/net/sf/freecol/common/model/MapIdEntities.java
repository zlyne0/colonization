package net.sf.freecol.common.model;

import java.util.Collection;
import java.util.HashMap;

import promitech.colonization.savegame.MapIdEntitySetter;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class MapIdEntities<T extends Identifiable> {
    private java.util.Map<String,T> entities = new HashMap<String,T>();
    
    public void add(T entity) {
        if (entity instanceof ObjectWithId) {
            ((ObjectWithId)entity).setInsertOrder(entities.size());
        }
        entities.put(entity.getId(), entity);
    }
    
    public T getById(String id) {
        T en = entities.get(id);
        if (en == null) {
            throw new IllegalArgumentException("can not find entity by id: " + id);
        }
        return en;
    }
    
    public T first() {
    	if (entities.isEmpty()) {
    		return null;
    	}
    	return entities.values().iterator().next();
    }
    
    public T getByIdOrNull(String id) {
        T en = entities.get(id);
        return en;
    }
    
    public boolean isEmpty() {
    	return entities.isEmpty();
    }

    public boolean isNotEmpty() {
    	return !entities.isEmpty();
    }
    
    public boolean containsId(Identifiable element) {
    	return entities.containsKey(element.getId());
    }
    
    public int size() {
    	return entities.size();
    }
    
    public Collection<T> entities() {
    	return entities.values();
    }
    
    public static class Xml extends XmlNodeParser {
        private final String tagName;
        private final boolean withWrapperTag;
        private final XmlNodeParser entityXmlParser;
        private final MapIdEntitySetter entitySetter;
        
        public Xml(XmlNodeParser parent, String wrapperTagName, String targetFieldName, Class<? extends Identifiable> entityClass) {
            super(parent);
            this.withWrapperTag = true;

            entityXmlParser = entityXmlParser(entityClass, this);
            this.tagName = wrapperTagName;
            
            entitySetter = new MapIdEntitySetter(parent, targetFieldName);
            entityXmlParser.addSetter(entitySetter);
            addNode(entityXmlParser);
        }
        
        public Xml(XmlNodeParser parent, String targetFieldName, Class<? extends Identifiable> entityClass) {
            super(parent);
            this.withWrapperTag = false;
            
            entityXmlParser = entityXmlParser(entityClass, parent);
            this.tagName = entityXmlParser.getTagName();
            
            entitySetter = new MapIdEntitySetter(parent, targetFieldName);
            entityXmlParser.addSetter(entitySetter);
            addAllNodes(entityXmlParser);
        }
        
        public void addToParent() {
            if (!withWrapperTag) {
                entityXmlParser.addToParent();
            }
        }
        
        @Override
        public void startElement(XmlNodeAttributes attr) {
            entitySetter.reset();
            if (!withWrapperTag) {
                entityXmlParser.startElement(attr);
            }
        }

        @Override
        public String getTagName() {
            return tagName;
        }
    }
}

