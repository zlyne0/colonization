package net.sf.freecol.common.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import promitech.colonization.savegame.MapIdEntitySetter;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;
import promitech.colonization.savegame.XmlTagMetaData;

public class MapIdEntities<T extends Identifiable> {
    protected java.util.Map<String,T> entities = new HashMap<String,T>();
    protected List<T> sortedEntities; 
    
    public void add(T entity) {
        if (entity instanceof ObjectWithId) {
            ((ObjectWithId)entity).setInsertOrder(entities.size());
        }
        entities.put(entity.getId(), entity);
        sortedEntities = null;
    }

    public void addAll(MapIdEntities<T> parentEntities) {
        entities.putAll(parentEntities.entities);
        sortedEntities = null;
    }
    
    public T getById(String id) {
        T en = entities.get(id);
        if (en == null) {
            throw new IllegalArgumentException("can not find entity by id: " + id);
        }
        return en;
    }
    
    public List<T> sortedEntities() {
    	if (sortedEntities == null) {
	    	sortedEntities = new ArrayList<T>(entities.values());
	    	Collections.sort((List<ObjectWithId>)sortedEntities, ObjectWithId.INSERT_ORDER_ASC_COMPARATOR);
    	}
    	return sortedEntities;
    }
    
    public List<T> allToProcessedOrder(T startEntity) {
    	List<T> sorted = sortedEntities();
    	List<T> startPart = new ArrayList<T>(sorted.size());
    	List<T> endPart = new ArrayList<T>(sorted.size());
    	boolean found = false;
		for (T entity : sorted) {
    		if (entity == startEntity) {
    			found = true;
    			continue;
    		}
    		if (found) {
    			startPart.add(entity);
    		} else {
    			endPart.add(entity);
    		}
    	}
    	List<T> list = new ArrayList<T>(sorted.size());
    	list.addAll(startPart);
    	list.addAll(endPart);
    	return list;
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
    
    public boolean containsId(String id) {
    	return entities.containsKey(id);
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
    
    public void removeId(String id) {
    	entities.remove(id);
    	sortedEntities = null;
    }
    
    public void removeId(Identifiable element) {
    	entities.remove(element.getId());
    	sortedEntities = null;
    }
    
    public void clear() {
        entities.clear();
        sortedEntities = null;
    }
    
    public static class Xml extends XmlNodeParser {
        private final String tagName;
        private final boolean withWrapperTag;
        private final XmlNodeParser entityXmlParser;
        private final MapIdEntitySetter entitySetter;
        
        public Xml(String wrapperTagName, String targetFieldName, Class<? extends Identifiable> entityClass) {
            this.withWrapperTag = true;

            entityXmlParser = new XmlTagMetaData(entityClass, null).createXmlParser();
            this.tagName = wrapperTagName;
            
            entitySetter = new MapIdEntitySetter(targetFieldName);
            entityXmlParser.addSetter(entitySetter);
            addNode(entityXmlParser);
        }
        
        public Xml(String targetFieldName, Class<? extends Identifiable> entityClass) {
            this.withWrapperTag = false;
            
            entityXmlParser = new XmlTagMetaData(entityClass, null).createXmlParser();
            this.tagName = entityXmlParser.getTagName();
            
            entitySetter = new MapIdEntitySetter(targetFieldName);
            entityXmlParser.addSetter(entitySetter);
            addAllNodes(entityXmlParser);
        }
        
        public void addToParent(XmlNodeParser parentXmlParser) {
            if (!withWrapperTag) {
                entityXmlParser.addToParent(parentXmlParser);
            }
        }
        
        @Override
        public void startElement(XmlNodeAttributes attr) {
            if (!withWrapperTag) {
                entityXmlParser.startElement(attr);
            }
        }

        @Override
        public void startReadChildren(XmlNodeAttributes attr) {
            entityXmlParser.startReadChildren(attr);
        }
        
        public void setMap(XmlNodeParser parentXmlParser) {
            if (withWrapperTag) {
                nodeObject = parentXmlParser.nodeObject;
            } else {
                nodeObject = entityXmlParser.nodeObject;
            }
        }
        
        @Override
        public String getTagName() {
            return tagName;
        }
    }

}

