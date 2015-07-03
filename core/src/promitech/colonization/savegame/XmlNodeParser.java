package promitech.colonization.savegame;

import java.util.HashMap;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Specification;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public abstract class XmlNodeParser {
    public static final int INFINITY = Integer.MAX_VALUE;
    public static final int UNDEFINED = Integer.MIN_VALUE;
    
	private java.util.Map<String,XmlNodeParser> nodes = new HashMap<String, XmlNodeParser>();
	public final XmlNodeParser parentXmlNodeParser;
	
	// unique entities
	protected static Specification specification;
	protected static Game game;
	
	public XmlNodeParser(XmlNodeParser parent) {
		this.parentXmlNodeParser = parent;
	}
	
	public void addNode(XmlNodeParser node) {
		nodes.put(node.getTagName(), node);
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
	public void startReadChildren(String qName, Attributes attributes) {
	}
	public void endReadChildren(String qName) {
	}
	
	public int getIntAttribute(Attributes attributes, String name) {
		return Integer.parseInt(attributes.getValue(name));
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
	
	public abstract String getTagName();
}

