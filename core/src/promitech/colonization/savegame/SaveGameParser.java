package promitech.colonization.savegame;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import net.sf.freecol.common.model.Game;

public class SaveGameParser {

	private String saveGameFileName;
	
	public SaveGameParser(String saveGameFileName) {
	    this.saveGameFileName = saveGameFileName;
	}
	
	public Game parse() throws IOException, ParserConfigurationException, SAXException {
		loadDefaultSpecification();

        FileHandle fh = Gdx.files.internal(saveGameFileName);
        
		InputStream read = fh.read();
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();

		final Game.Xml xmlGame = new Game.Xml();
		SaveGameSaxXmlDefaultHandler df = new SaveGameSaxXmlDefaultHandler(xmlGame);
		
		saxParser.parse(read, df);
		read.close();
		
		return xmlGame.game;
	}
	
	private void loadDefaultSpecification() throws IOException, ParserConfigurationException, SAXException {
		FileHandle fh = Gdx.files.internal("rules/classic/specification.xml");
		//FileHandle fh = Gdx.files.internal("rules/freecol/specification.xml");
		InputStream read = fh.read();

		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();

		final Game.Xml xmlGame = new Game.Xml();
		SaveGameSaxXmlDefaultHandler df = new SaveGameSaxXmlDefaultHandler(xmlGame);
		saxParser.parse(read, df);
		read.close();
	}

}
