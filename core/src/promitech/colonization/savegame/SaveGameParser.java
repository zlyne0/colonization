package promitech.colonization.savegame;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

	public static Game loadGameFormClassPath(String classPathFileName) throws IOException, ParserConfigurationException, SAXException {
		FileHandle fh = Gdx.files.internal(classPathFileName);
		InputStream read = fh.read();
		
		SaveGameParser s = new SaveGameParser();
		
		return s.parse(read);
	}
	
	public static Game loadGameFromFile(File saveFile) throws FileNotFoundException, IOException, ParserConfigurationException, SAXException {
		SaveGameParser s = new SaveGameParser();
		return s.parse(new FileInputStream(saveFile));
	}
	
	public SaveGameParser() {
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
