package promitech.colonization;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.ResourceType;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.TileImprovementType;
import net.sf.freecol.common.model.TileType;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import promitech.colonization.savegame.XmlNodeParser;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

class XmlGame extends XmlNodeParser {
	Game game = new Game();
	
	public XmlGame() {
		super(null);
		
		addNode(new XmlSpecification(this));
		addNode(new XmlMap(this));
	}
	
	@Override
	public void startElement(String qName, Attributes attributes) {
	}
	
	@Override
	public String getTagName() {
		return "game";
	}
}


class XmlSpecification extends XmlNodeParser {
	Specification specification; 
	
	public XmlSpecification(XmlGame parent) {
		super(parent);
		addNode(new XmlTileType(this));
		addNode(new XmlResourceType(this));
		addNode(new TileImprovementType.Xml(this));
	}

	@Override
	public void startElement(String qName, Attributes attributes) {
		specification = new Specification();
		XmlGame xmlGame = getParentXmlParser();
		xmlGame.game.specification = specification;
	}

	@Override
	public String getTagName() {
		return "freecol-specification";
	}
}

class XmlTileType extends XmlNodeParser {
	public XmlTileType(XmlNodeParser parent) {
		super(parent);
	}

	@Override
	public void startElement(String qName, Attributes attributes) {
		String id = getStrAttribute(attributes, "id");
		boolean isForest = getBooleanAttribute(attributes, "is-forest");
		TileType tileType = new TileType(id, isForest);
		
		((XmlSpecification)this.parentXmlNodeParser).specification.addTileType(tileType);
	}

	@Override
	public String getTagName() {
		return "tile-type";
	}
}

class XmlResourceType extends XmlNodeParser {

	public XmlResourceType(XmlNodeParser parent) {
		super(parent);
	}

	@Override
	public void startElement(String qName, Attributes attributes) {
		String id = getStrAttribute(attributes, "id");
		ResourceType resourceType = new ResourceType(id);
		((XmlSpecification)this.parentXmlNodeParser).specification.addResourceType(resourceType);
	}

	@Override
	public String getTagName() {
		return "resource-type";
	}
}

class XmlMap extends XmlNodeParser {
	Map map;
	
	public XmlMap(XmlNodeParser parent) {
		super(parent);
		
		addNode(new Tile.Xml(this));
	}

	@Override
	public void startElement(String qName, Attributes attributes) {
		int width = getIntAttribute(attributes, "width");
		int height = getIntAttribute(attributes, "height");
		map = new Map(width, height);
		
		((XmlGame)parentXmlNodeParser).game.map = map;
	}

	@Override
	public String getTagName() {
		return "map";
	}
	
}

public class SaveGameParser {

	Game game;
	
	public void parse() throws IOException, ParserConfigurationException, SAXException {
		FileHandle fh = Gdx.files.internal("maps/savegame.xml");
		InputStream read = fh.read();
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();

		final XmlGame xmlGame = new XmlGame();
		DefaultHandler df = new DefaultHandler() {
			LinkedList<XmlNodeParser> parsers = new LinkedList<XmlNodeParser>();
			LinkedList<String> parsersName = new LinkedList<String>();
			XmlNodeParser xmlNodeParser;
			
			@Override
			public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
				if (xmlNodeParser == null) {
					xmlNodeParser = xmlGame;
					parsersName.add("game");
					parsers.add(xmlGame);
				}

				if (xmlNodeParser.rootGame == null) {
					xmlNodeParser.rootGame = xmlGame.game;
				}
				
				if (qName.equals("game")) {
					xmlNodeParser.startElement(qName, attributes);
				}
				
				XmlNodeParser parserForTag = xmlNodeParser.parserForTag(qName);
				if (parserForTag == null) {
					xmlNodeParser.startReadChildren(qName, attributes);
					return;
				}
				parsersName.add(qName);
				parsers.add(parserForTag);
				xmlNodeParser = parserForTag;
				
				if (xmlNodeParser.rootGame == null) {
					xmlNodeParser.rootGame = xmlGame.game;
				}
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
		};
		
		saxParser.parse(read, df);
		read.close();
		
		this.game = xmlGame.game;
	}

}
