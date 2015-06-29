package promitech.colonization.savegame;

import java.util.LinkedList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SaveGameSaxXmlDefaultHandler extends DefaultHandler {
    LinkedList<XmlNodeParser> parsers = new LinkedList<XmlNodeParser>();
    LinkedList<String> parsersName = new LinkedList<String>();
    XmlNodeParser xmlNodeParser;
    
    public SaveGameSaxXmlDefaultHandler(XmlNodeParser rootXmlNodeParser) {
        xmlNodeParser = rootXmlNodeParser;
        parsersName.add(rootXmlNodeParser.getTagName());
        parsers.add(rootXmlNodeParser);
    }
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (xmlNodeParser.getTagName().equals("unit") && qName.equals("unit")) {
            parsersName.add(qName);
            parsers.add(xmlNodeParser);
            xmlNodeParser.startElement(qName, attributes);
            return;
        }
        XmlNodeParser parserForTag = xmlNodeParser.parserForTag(qName);
        if (parserForTag == null) {
            xmlNodeParser.startReadChildren(qName, attributes);
            return;
        }
        parsersName.add(qName);
        parsers.add(parserForTag);
        xmlNodeParser = parserForTag;
        
        xmlNodeParser.startElement(qName, attributes);
        
    }
    
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
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
    }
}

