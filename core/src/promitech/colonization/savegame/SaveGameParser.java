package promitech.colonization.savegame;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Specification;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

class XmlDefaultHandler extends DefaultHandler {
	LinkedList<XmlNodeParser> parsers = new LinkedList<XmlNodeParser>();
	LinkedList<String> parsersName = new LinkedList<String>();
	XmlNodeParser xmlNodeParser;
	
	public XmlDefaultHandler(XmlNodeParser rootXmlNodeParser) {
		xmlNodeParser = rootXmlNodeParser;
		parsersName.add(rootXmlNodeParser.getTagName());
		parsers.add(rootXmlNodeParser);
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
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

public class SaveGameParser {

	Specification defaultSpecification;
	
	public Game parse() throws IOException, ParserConfigurationException, SAXException {
		XmlNodeParser.specification = defaultSpecification = loadDefaultSpecification();

		
		//FileHandle fh = Gdx.files.internal("maps/savegame.xml");
		FileHandle fh = Gdx.files.internal("maps/america_map.xml");
		InputStream read = fh.read();
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();

		final Game.Xml xmlGame = new Game.Xml();
		XmlDefaultHandler df = new XmlDefaultHandler(xmlGame);
		
		saxParser.parse(read, df);
		read.close();
		
		return xmlGame.game;
	}
	
	private Specification loadDefaultSpecification() throws IOException, ParserConfigurationException, SAXException {
		FileHandle fh = Gdx.files.internal("rules/classic/specification.xml");
		//FileHandle fh = Gdx.files.internal("rules/freecol/specification.xml");
		InputStream read = fh.read();

		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();

		final Game.Xml xmlGame = new Game.Xml();
		XmlDefaultHandler df = new XmlDefaultHandler(xmlGame);
		saxParser.parse(read, df);
		read.close();
		
		return xmlGame.game.specification;
	}

}
