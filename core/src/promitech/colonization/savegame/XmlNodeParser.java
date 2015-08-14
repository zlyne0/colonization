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
	
	public void addNodeMapIdEntities(String wrapperTag, String filedName, Class entityClass) {
	}
    public void addNodeMapIdEntities(String filedName, Class entityClass) {
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
	
	public abstract void startElement(String qName, Attributes attributes);
	public void endElement(String uri, String localName, String qName) throws SAXException {
	}
	@Deprecated
	public void startReadChildren(String qName, Attributes attributes) {
	}
	public void endReadChildren(String qName) {
	}
	
	public int getIntAttribute(Attributes attributes, String name) {
		return Integer.parseInt(attributes.getValue(name));
	}

	public float getFloatAttribute(Attributes attributes, String name) {
		return Float.parseFloat(attributes.getValue(name));
	}
	
	public int getIntAttribute(Attributes attributes, String name, int defaultVal) {
		String val = attributes.getValue(name);
		if (val == null) {
			return defaultVal;
		}
		return Integer.parseInt(val);
	}
	
	public String getStrAttribute(Attributes attributes, String name) {
		return attributes.getValue(name);
	}

	public boolean getBooleanAttribute(Attributes attributes, String name) {
		String val = attributes.getValue(name);
		return val != null && "true".equals(val.toLowerCase());
	}
	
	public <T extends Enum<T>> T getEnumAttribute(Attributes attributes, Class<T> enumClass, String name) {
		String val = attributes.getValue(name);
		if (val == null) {
			return null;
		}
		return Enum.valueOf(enumClass, val.toUpperCase());
	}
	
	public abstract String getTagName();
}

