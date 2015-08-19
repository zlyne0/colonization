package promitech.colonization.savegame;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Identifiable;
import net.sf.freecol.common.model.MapIdEntities;

import org.xml.sax.SAXException;

public abstract class XmlNodeParser {
    public static final int INFINITY = Integer.MAX_VALUE;
    public static final int UNDEFINED = Integer.MIN_VALUE;
    
    private final Map<String, String> entityIdMapFieldNameByTag = new HashMap<String, String>();
    private final Map<String, Class<? extends Identifiable>> entityIdMapFieldClassByTag = new HashMap<String, Class<? extends Identifiable>>();
    private final Map<String, Boolean> entityIdMapWrapperTagPosses = new HashMap<String, Boolean>();
	private final java.util.Map<String,XmlNodeParser> nodeParserByTagName = new HashMap<String, XmlNodeParser>();
	
	private final XmlNodeParser parentXmlNodeParser;
	public Identifiable nodeObject;
	
	// unique entities
	//protected static Specification specification;
	protected static Game game;
	
	public XmlNodeParser(XmlNodeParser parent) {
		this.parentXmlNodeParser = parent;
	}
	
	public void addNode(XmlNodeParser node) {
		nodeParserByTagName.put(node.getTagName(), node);
	}
    
	public void addNodeForMapIdEntities(String wrapperTag, String fieldName, Class<? extends Identifiable> entityClass) {
	    entityIdMapFieldNameByTag.put(wrapperTag, fieldName);
	    entityIdMapFieldClassByTag.put(wrapperTag, entityClass);
        entityIdMapWrapperTagPosses.put(wrapperTag, Boolean.TRUE);
	}
	
    public void addNodeForMapIdEntities(String fieldName, Class<? extends Identifiable> entityClass) {
        Class<XmlNodeParser> xmlClass = getXmlClassFromEntityClass(entityClass);
        try {
            Method tagNameMethod = xmlClass.getDeclaredMethod("tagName");
            String tagName = (String)tagNameMethod.invoke(null);
            entityIdMapFieldNameByTag.put(tagName, fieldName);
            entityIdMapFieldClassByTag.put(tagName, entityClass);
            entityIdMapWrapperTagPosses.put(tagName, Boolean.FALSE);
        } catch (Exception e) {
            throw new IllegalStateException("can not invoke tagName method in Xml for entity " + entityClass);
        }
    }
	
	public void addAllNodes(XmlNodeParser node) {
	    nodeParserByTagName.putAll(node.nodeParserByTagName);
	}
	
	public XmlNodeParser parserForTag(String qName) {
	    XmlNodeParser xmlNodeParser = nodeParserByTagName.get(qName);
	    if (xmlNodeParser != null) {
	        return xmlNodeParser;
	    }
	    if (entityIdMapFieldNameByTag.containsKey(qName)) {
	        if (entityIdMapWrapperTagPosses.get(qName)) {
	            xmlNodeParser = new MapIdEntities.Xml(this, qName, entityIdMapFieldNameByTag.get(qName), entityIdMapFieldClassByTag.get(qName));
	        } else {
	            xmlNodeParser = new MapIdEntities.Xml(this, entityIdMapFieldNameByTag.get(qName), entityIdMapFieldClassByTag.get(qName));
	        }
	        nodeParserByTagName.put(qName, xmlNodeParser);
	    }
		return xmlNodeParser;
	}
	
	private ObjectFromNodeSetter setter;
	public XmlNodeParser addSetter(ObjectFromNodeSetter setter) {
	    this.setter = setter;
	    return this;
	}
	
	public void addToParent() {
	    if (setter != null) {
	        setter.set(nodeObject);
	    }
	}
	
	@SuppressWarnings("unchecked")
	public <T extends XmlNodeParser> T getParentXmlParser() {
		return (T)parentXmlNodeParser;
	}
	
	public abstract void startElement(XmlNodeAttributes attr);
	public void endElement(String uri, String localName, String qName) throws SAXException {
	}
	@Deprecated
	public void startReadChildren(XmlNodeAttributes attr) {
	}
	public void endReadChildren(String qName) {
	}
	
	public abstract String getTagName();

	
    protected XmlNodeParser entityXmlParser(Class<? extends Identifiable> entityClass, XmlNodeParser parent) {
        Class<XmlNodeParser> xmlClass = getXmlClassFromEntityClass(entityClass);
        return createXmlParser(xmlClass, parent);
    }

    protected XmlNodeParser createXmlParser(Class<? extends XmlNodeParser> xmlClass, XmlNodeParser parent) {
        try {
            XmlNodeParser entityXmlParser = xmlClass.getDeclaredConstructor(XmlNodeParser.class).newInstance(parent);
            return entityXmlParser;
        } catch (Exception e) {
            throw new IllegalStateException(e);
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
}

