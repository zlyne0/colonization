package promitech.colonization.savegame;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.SavedGame;
import net.sf.freecol.common.model.x.XGame;
import net.sf.freecol.common.model.x.XMap;
import net.sf.freecol.common.model.x.XPlayer;
import net.sf.freecol.common.model.x.XSavedGame;
import net.sf.freecol.common.model.x.XSpecification;
import net.sf.freecol.common.model.x.XTileType;

public class SaveGameTest {


    @BeforeClass
    public static void beforeClass() {
        Gdx.files = new LwjglFiles();
    }
	
	@Before
	public void setup() {
	}
	
	@Test
	public void canCreateSaveGameXml() throws Exception {
		// given
		
		XSpecification xSpecification = new XSpecification();
		xSpecification.addTileType(new XTileType("tileType:1"));
		xSpecification.addTileType(new XTileType("tileType:2"));
		xSpecification.addTileType(new XTileType("tileType:3"));
		
		XSavedGame savedGame = new XSavedGame();
		savedGame.game = new XGame();
		savedGame.game.setMap(new XMap());
		savedGame.game.setSpecification(xSpecification);
		savedGame.game.addPlayer(new XPlayer("player:1"));
		savedGame.game.addPlayer(new XPlayer("player:2"));
		
		// when
		new SaveGameCreator().generateXmlFrom(savedGame); 
		
		// then
	}

	@Test
	public void canCreateXml() throws Exception {
		// given
        SaveGameParser saveGameParser = new SaveGameParser("maps/savegame_1600_for_jtests.xml");
        Game game = saveGameParser.parse();
		
        SavedGame savedGameObj = new SavedGame();
        savedGameObj.game = game;
        
		// when
        new SaveGameCreator().generateXmlFrom(savedGameObj);
		
		// then
	
	}
	
}
