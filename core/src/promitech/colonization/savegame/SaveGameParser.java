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
import net.sf.freecol.common.model.SavedGame;
import net.sf.freecol.common.model.Specification;

public class SaveGameParser {

	private String saveGameFileName;
	
	public SaveGameParser(String saveGameFileName) {
	    this.saveGameFileName = saveGameFileName;
	}
	
	public SaveGameParser() {
	}
	
	public Game parse() throws IOException, ParserConfigurationException, SAXException {
		loadDefaultSpecification();
        FileHandle fh = Gdx.files.internal(saveGameFileName);
        
		InputStream read = fh.read();
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();

		final SavedGame.Xml xmlSavedGame = new SavedGame.Xml();
		SaveGameSaxXmlDefaultHandler df = new SaveGameSaxXmlDefaultHandler(xmlSavedGame);

		try {
			saxParser.parse(read, df);
		} finally {
			read.close();
		}
		return xmlSavedGame.savedGame.game;
	}
	
	public Game parse(InputStream is) throws IOException, ParserConfigurationException, SAXException {
		loadDefaultSpecification();
        
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();

		final SavedGame.Xml xmlSavedGame = new SavedGame.Xml();
		SaveGameSaxXmlDefaultHandler df = new SaveGameSaxXmlDefaultHandler(xmlSavedGame);
		
		try {
			saxParser.parse(is, df);
		} finally {
			is.close();
		}
		return xmlSavedGame.savedGame.game;
	}
	
	public static void loadDefaultSpecification() throws IOException, ParserConfigurationException, SAXException {
		FileHandle fh = Gdx.files.internal("rules/classic/specification.xml");
		//FileHandle fh = Gdx.files.internal("rules/freecol/specification.xml");
		InputStream read = fh.read();

		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();

		XmlNodeParser specificationParser = new XmlNodeParser() {
			@Override
			public void startElement(XmlNodeAttributes attr) {
			}
			@Override
			public String getTagName() {
				return null;
			}
		};
		specificationParser.addNode(new Specification.Xml());
		
		SaveGameSaxXmlDefaultHandler df = new SaveGameSaxXmlDefaultHandler(specificationParser);
		saxParser.parse(read, df);
		read.close();
	}

}
