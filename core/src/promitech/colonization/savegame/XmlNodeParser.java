package promitech.colonization.savegame;

import java.util.HashMap;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Identifiable;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.TileType;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public abstract class XmlNodeParser {
    public static final int INFINITY = Integer.MAX_VALUE;
    public static final int UNDEFINED = Integer.MIN_VALUE;
    
	private java.util.Map<String,XmlNodeParser> nodes = new HashMap<String, XmlNodeParser>();
	public final XmlNodeParser parentXmlNodeParser;
	
	public Identifiable nodeObject;
	
	// unique entities
	//protected static Specification specification;
	protected static Game game;
	
	public XmlNodeParser(XmlNodeParser parent) {
		this.parentXmlNodeParser = parent;
	}
	
	public void addNode(XmlNodeParser node) {
		nodes.put(node.getTagName(), node);
	}
	
	private java.util.Map<String, String> mapField = new HashMap<String, String>();
    private java.util.Map<String, Class> mapFieldClass = new HashMap<String, Class>();
	public void addNodeForMapIdEntities(String wrapperTag, String filedName, Class entityClass) {
	    mapField.put(wrapperTag, filedName);
	    mapFieldClass.put(wrapperTag, entityClass);
	}
    public void addNodeForMapIdEntities(String filedName, Class entityClass) {
    }
	
	public void addAllNodes(XmlNodeParser node) {
	    nodes.putAll(node.nodes);
	}
	
	protected ObjectFromNodeSetter setter;
	public XmlNodeParser addSetter(ObjectFromNodeSetter setter) {
		this.setter = setter;
		return this;
	}
	
	public void addToParent() {
	    if (setter != null) {
	        setter.set(nodeObject);
	    }
	}
	
	public XmlNodeParser parserForTag(String qName) {
		return nodes.get(qName);
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
}

