package net.sf.freecol.common.model.map;

import static org.junit.Assert.*;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import promitech.colonization.savegame.SaveGameParser;

public class PathFinderTest {

    @BeforeClass
    public static void beforeClass() {
        Gdx.files = new LwjglFiles();
    }
	
	@Test
	public void canFindPath() throws Exception {
		// given
        SaveGameParser saveGameParser = new SaveGameParser("maps/savegame_1600_for_jtests.xml");
        Game game = saveGameParser.parse();

        Tile startTile = game.map.getTile(24, 78);
        Tile endTile = game.map.getTile(21, 72);
        Unit moveUnit = startTile.units.getById("unit:6762");
        
        
        PathFinder finder = new PathFinder();
        
		// when
		Path path = finder.find(game.map, startTile, endTile, moveUnit);
        
		
		// then
		System.out.println("path = \r\n" + path );
	}

}
