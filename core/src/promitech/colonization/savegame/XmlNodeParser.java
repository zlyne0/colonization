package promitech.colonization.savegame;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.xml.sax.SAXException;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Identifiable;

public abstract class XmlNodeParser<NODE_ENTITY_CLASS> {
	
	protected static final String ATTR_VALUE = "value";
	protected static final String ATTR_ID = "id";
	
    public static final int INFINITY = Integer.MAX_VALUE;
    public static final int UNDEFINED = Integer.MIN_VALUE;
	public static final int UNLIMITED = -1;
    
    protected XmlTagMetaData xmlNodeMetaData;
    private final java.util.Map<String, XmlTagMetaData> nodeMetaData = new LinkedHashMap<String, XmlTagMetaData>();
    private final java.util.Map<String, XmlNodeParser> nodeParserByTagName = new HashMap<String, XmlNodeParser>();
	
	public NODE_ENTITY_CLASS nodeObject;
	
	// unique entities
	protected static Game game;
	
	public XmlNodeParser() {
	}
	
	public Collection<XmlTagMetaData> childrenNodeParsers() {
		return nodeMetaData.values();
	}
	
	public void addNode(XmlNodeParser node) {
		nodeParserByTagName.put(node.getTagName(), node);
	}

	public <T> void addNode(Class<T> entityClass, String fieldName) {
		XmlTagMetaData xmlTagMetaData = new XmlTagMetaData(entityClass, fieldName);
	    nodeMetaData.put(xmlTagMetaData.getTagName(), xmlTagMetaData);
	}
	
    public <T> void addNode(String entityOverrideTagName, Class<T> entityClass, String targetFieldName) {
        XmlTagMetaData xmlTagMetaData = new XmlTagMetaData(entityOverrideTagName, entityClass, targetFieldName);
        nodeMetaData.put(entityOverrideTagName, xmlTagMetaData);
    }

	public <T extends Identifiable> void addNode(String entityOverrideTagName, Class<? extends Identifiable> entityClass, ObjectFromNodeSetter<?,T> setter) {
		XmlTagMetaData xmlTagMetaData = new XmlTagMetaData(entityOverrideTagName, entityClass, setter);
		nodeMetaData.put(entityOverrideTagName, xmlTagMetaData);
	}	
	
	public <T> void addNode(Class<T> entityClass, ObjectFromNodeSetter<?,T> setter) {
	    XmlTagMetaData xmlTagMetaData = new XmlTagMetaData(entityClass, setter);
	    nodeMetaData.put(xmlTagMetaData.getTagName(), xmlTagMetaData);
	}
	
	public void addNodeForMapIdEntities(String wrapperTag, String fieldName, Class<? extends Identifiable> entityClass) {
	    XmlTagMapIdEntitiesMetaData xmlMetaData = new XmlTagMapIdEntitiesMetaData(
	        wrapperTag, 
	        fieldName, 
	        entityClass
	    );
	    nodeMetaData.put(wrapperTag, xmlMetaData);
	}
	
    public void addNodeForMapIdEntities(String fieldName, Class<? extends Identifiable> entityClass) {
        XmlTagMapIdEntitiesMetaData xmlMetaData = new XmlTagMapIdEntitiesMetaData(
            fieldName, 
            entityClass
        );
        nodeMetaData.put(xmlMetaData.getTagName(), xmlMetaData);
    }
	
	public void addAllNodes(XmlNodeParser node) {
	    nodeMetaData.putAll(node.nodeMetaData);
	    nodeParserByTagName.putAll(node.nodeParserByTagName);
	}
	
	public XmlNodeParser parserForTag(String qName) {
	    XmlNodeParser xmlNodeParser = nodeParserByTagName.get(qName);
	    if (xmlNodeParser != null) {
	        return xmlNodeParser;
	    }
	    XmlTagMetaData xmlTagMetaData = nodeMetaData.get(qName);
	    if (xmlTagMetaData != null) {
	        xmlNodeParser = xmlTagMetaData.createXmlParser();
            nodeParserByTagName.put(qName, xmlNodeParser);
            return xmlNodeParser;
	    }
		return xmlNodeParser;
	}
	
	private ObjectFromNodeSetter setter;
	public XmlNodeParser addSetter(ObjectFromNodeSetter setter) {
	    this.setter = setter;
	    return this;
	}
	
	public void addToParent(XmlNodeParser parentXmlParser) {
	    if (xmlNodeMetaData != null && xmlNodeMetaData.targetFieldName != null) {
	        UniversalEntitySetter.set(parentXmlParser.nodeObject, xmlNodeMetaData.targetFieldName, nodeObject);
	    } else {
            if (setter != null) {
                setter.set(parentXmlParser.nodeObject, nodeObject);
            }
	    }
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

	public void startWriteAttr(NODE_ENTITY_CLASS node, XmlNodeAttributesWriter attr) throws IOException {
	}
}

