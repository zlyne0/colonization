package promitech.colonization.savegame;

import java.util.LinkedList;

import net.sf.freecol.common.model.MapIdEntities;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SaveGameSaxXmlDefaultHandler extends DefaultHandler {
    LinkedList<XmlNodeParser> parsers = new LinkedList<XmlNodeParser>();
    LinkedList<String> parsersName = new LinkedList<String>();
    XmlNodeParser xmlNodeParser;
    
    private final XmlNodeAttributes nodeAttributes = new XmlNodeAttributes();
    
    public SaveGameSaxXmlDefaultHandler(XmlNodeParser rootXmlNodeParser) {
        xmlNodeParser = rootXmlNodeParser;
        parsersName.add(rootXmlNodeParser.getTagName());
        parsers.add(rootXmlNodeParser);
    }
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        XmlNodeParser parserForTag = xmlNodeParser.parserForTag(qName);
        if (parserForTag == null) {
            nodeAttributes.qName = qName;
            nodeAttributes.attributes = attributes;
            xmlNodeParser.startReadChildren(nodeAttributes);
            return;
        }
        XmlNodeParser parentXmlParser = xmlNodeParser;
        
        parsersName.add(qName);
        parsers.add(parserForTag);
        xmlNodeParser = parserForTag;

        nodeAttributes.qName = qName;
        nodeAttributes.attributes = attributes;
        xmlNodeParser.startElement(nodeAttributes);
        if (xmlNodeParser instanceof MapIdEntities.Xml) {
            ((MapIdEntities.Xml)xmlNodeParser).setMap(parentXmlParser);
        }
        if (xmlNodeParser.nodeObject == null) {
            System.out.println("parsersName = " + parsersName);
            throw new IllegalArgumentException("parser nodeObject is null for tag: " + xmlNodeParser.getTagName() + ", xml parser: " + xmlNodeParser.getClass() + ", attributes: " + nodeAttributes); 
        }
        xmlNodeParser.addToParent(parentXmlParser);
    }
    
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        try {
            if (parsersName.size() > 0 && parsersName.getLast().equals(qName)) {
                xmlNodeParser.endElement(uri, localName, qName);
                
                parsersName.removeLast();
                parsers.removeLast();
                if (!parsers.isEmpty()) {
                    xmlNodeParser = parsers.getLast();
                }
            } else {
                xmlNodeParser.endReadChildren(qName);
            }
        } catch (RuntimeException e) {
            System.out.println("endElement exception. Element '" + qName + "' Parse list: " + parsersName);
            throw e;
        }
    }
}

