package promitech.colonization.savegame;

import org.junit.Before;
import org.junit.Test;

import net.sf.freecol.common.model.x.XGame;
import net.sf.freecol.common.model.x.XMap;
import net.sf.freecol.common.model.x.XPlayer;
import net.sf.freecol.common.model.x.XSavedGame;
import net.sf.freecol.common.model.x.XSpecification;
import net.sf.freecol.common.model.x.XTileType;

public class SaveGameTest {


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
	
	
}
