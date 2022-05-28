package net.sf.freecol.common.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.SAXException;

import promitech.colonization.savegame.MapIdEntitySetter;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;
import promitech.colonization.savegame.XmlTagMetaData;

public class MapIdEntities<T extends Identifiable> implements MapIdEntitiesReadOnly<T>, Iterable<T> {
	
    protected final java.util.Map<String,T> entities;

    public static <T extends Identifiable> MapIdEntities<T> linkedMapIdEntities() {
    	return new MapIdEntities<T>(new LinkedHashMap<String, T>());
    }

	public static <T extends Identifiable> MapIdEntities<T> linkedMapIdEntities(int initialCapacity) {
    	return new MapIdEntities<T>(new LinkedHashMap<String, T>(initialCapacity));
	}
    
	public static <TT extends Identifiable> MapIdEntities<TT> unmodifiableEmpty() {
		Map<String, TT> emptyMap = Collections.emptyMap();
		return new MapIdEntities<TT>(Collections.unmodifiableMap(emptyMap));
	}
	
    public MapIdEntities() {
    	entities = new HashMap<String,T>();
    }
    
    public MapIdEntities(List<T> entitiesList) {
    	entities = new HashMap<String, T>();
    	for (T entity : entitiesList) {
    		entities.put(entity.getId(), entity);
    	}
    }
    
    private MapIdEntities(java.util.Map<String,T> entitiesMapImplementation) {
    	this.entities = entitiesMapImplementation;
    }
    
    public MapIdEntities(MapIdEntitiesReadOnly<T> aMapEntities) {
        entities = new HashMap<String,T>(aMapEntities.size());
    	addAll(aMapEntities);
    }
    
    public void add(T entity) {
        entities.put(entity.getId(), entity);
    }

    public java.util.Map<String,T> innerMap() {
        return entities;
    }
    
    public void addAll(MapIdEntitiesReadOnly<T> parentEntities) {
        entities.putAll(parentEntities.innerMap());
    }
    
    public T getById(String id) {
        T en = entities.get(id);
        if (en == null) {
            throw new IllegalArgumentException("can not find entity by id: " + id);
        }
        return en;
    }

    public T getById(Identifiable identifiable) {
        T en = entities.get(identifiable.getId());
        if (en == null) {
            throw new IllegalArgumentException("can not find entity by id: " + identifiable.getId());
        }
        return en;
    }

    public T getByIdOrNull(String id) {
        return entities.get(id);
    }
    
    public T getByIdOrNull(Identifiable obj) {
    	return entities.get(obj.getId());
    }
    
    public List<T> sortedEntities() {
        throw new IllegalStateException("invoke sortedEntities on not SortedMapIdEntities implementation");
    }
    
    public List<T> allToProcessedOrder(T startEntity) {
    	Collection<T> sorted = entities();
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
    
	public T firstButNot(T exclude) {
		if (exclude == null) {
			return first();
		}
		for (T entity : entities()) {
			if (!entity.getId().equals(exclude.getId())) {
				return entity;
			}
		}
		return null;
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

	@Override
	public Iterator<T> iterator() {
		return entities.values().iterator();
	}
    
    public MapIdEntities<T> reduceBy(MapIdEntities<T> reducer) {
        MapIdEntities<T> reduced = new MapIdEntities<T>();
        for (T u : this.entities()) {
            if (!reducer.containsId(u)) {
                reduced.add(u);
            }
        }
        return reduced;
    }
    
    public void removeId(String id) {
    	entities.remove(id);
    }
    
    public void removeId(Identifiable element) {
    	entities.remove(element.getId());
    }
    
    public void clear() {
        entities.clear();
    }
    
    @Override
    public List<T> copy() {
    	return new ArrayList<T>(entities.values());
    }
    
    public static class Xml extends XmlNodeParser {
        private final String tagName;
        private final boolean withWrapperTag;
        private final XmlNodeParser entityXmlParser;
        private final MapIdEntitySetter entitySetter;
        
        public Xml(String wrapperTagName, String targetFieldName, Class<? extends Identifiable> entityClass) {
            this.withWrapperTag = true;

            entityXmlParser = new XmlTagMetaData(entityClass).createXmlParser();
            this.tagName = wrapperTagName;
            
            entitySetter = new MapIdEntitySetter(targetFieldName);
            entityXmlParser.addSetter(entitySetter);
            addNode(entityXmlParser);
        }
        
        public Xml(String targetFieldName, Class<? extends Identifiable> entityClass) {
            this.withWrapperTag = false;
            
            entityXmlParser = new XmlTagMetaData(entityClass).createXmlParser();
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
        
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
        	entityXmlParser.endElement(uri, localName, qName);
        }
        
        @Override
        public void endReadChildren(String qName) {
        	entityXmlParser.endReadChildren(qName);
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

