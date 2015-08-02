package promitech.colonization.savegame;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Specification;

import org.xml.sax.SAXException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class SaveGameParser {

	Specification defaultSpecification;
	
	public Game parse() throws IOException, ParserConfigurationException, SAXException {
		XmlNodeParser.specification = defaultSpecification = loadDefaultSpecification();

		
		//FileHandle fh = Gdx.files.internal("maps/savegame.xml");
		//FileHandle fh = Gdx.files.internal("maps/america_map.xml");
        //FileHandle fh = Gdx.files.internal("maps/dutch_1522.xml");
        FileHandle fh = Gdx.files.internal("maps/savegame_1600.xml");
        
		InputStream read = fh.read();
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();

		final Game.Xml xmlGame = new Game.Xml();
		SaveGameSaxXmlDefaultHandler df = new SaveGameSaxXmlDefaultHandler(xmlGame);
		
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
		SaveGameSaxXmlDefaultHandler df = new SaveGameSaxXmlDefaultHandler(xmlGame);
		saxParser.parse(read, df);
		read.close();
		
		return xmlGame.game.specification;
	}

}
