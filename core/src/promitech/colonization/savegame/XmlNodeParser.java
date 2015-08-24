package promitech.colonization.savegame;

import java.util.HashMap;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Identifiable;

import org.xml.sax.SAXException;

public abstract class XmlNodeParser {
    public static final int INFINITY = Integer.MAX_VALUE;
    public static final int UNDEFINED = Integer.MIN_VALUE;
    
    
    private final java.util.Map<String, XmlTagMetaData> nodeMetaData = new HashMap<String, XmlTagMetaData>();
	private final java.util.Map<String,XmlNodeParser> nodeParserByTagName = new HashMap<String, XmlNodeParser>();
	
	public Identifiable nodeObject;
	
	// unique entities
	//protected static Specification specification;
	protected static Game game;
	
	public XmlNodeParser() {
	}
	
	public void addNode(XmlNodeParser node) {
		nodeParserByTagName.put(node.getTagName(), node);
	}
    
	public void addNode(Class<? extends Identifiable> entityClass, ObjectFromNodeSetter setter) {
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
	    if (setter != null) {
	        setter.set(parentXmlParser.nodeObject, nodeObject);
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
}

