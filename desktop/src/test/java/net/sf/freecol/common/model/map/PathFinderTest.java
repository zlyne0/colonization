package net.sf.freecol.common.model.map;

import static org.junit.Assert.*;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
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

    Path path;
    Game game;
    PathFinder sut = new PathFinder();
    
    @BeforeClass
    public static void beforeClass() {
        Gdx.files = new LwjglFiles();
    }

    @Before
    public void setup() throws Exception {
    	SaveGameParser saveGameParser = new SaveGameParser("maps/savegame_1600_for_jtests.xml");
    	game = saveGameParser.parse();
    }
    
	@Test
	public void canFindLandPathForColonist() throws Exception {
		// given
        Tile startTile = game.map.getTile(24, 78);
        Tile endTile = game.map.getTile(21, 72);
        Unit moveUnit = startTile.units.getById("unit:6762");
        
		// when
		path = sut.find(game.map, startTile, endTile, moveUnit);
        
		// then
		System.out.println("path = \r\n" + path );
		
		assertEquals(7, path.tiles.size);
        assertEquals(7, path.turns.size);
		
        assertPathStep(0, 0, 24, 78);
        assertPathStep(1, 0, 23, 78);
        assertPathStep(2, 0, 23, 76);
        assertPathStep(3, 0, 22, 75);
        assertPathStep(4, 1, 22, 74);
        assertPathStep(5, 1, 22, 72);
        assertPathStep(6, 2, 21, 72);
        
        assertTrue(path.startTile.equalsCoordinates(24, 78));
        assertTrue(path.endTile.equalsCoordinates(21, 72));
	}

	@Test
	public void canFindMarinePathForNavy() throws Exception {
		// given
        Tile startTile = game.map.getTile(12, 79);
        Tile endTile = game.map.getTile(12, 83);
        Unit moveUnit = startTile.units.getById("unit:6900");
		
		// when
        path = sut.find(game.map, startTile, endTile, moveUnit);
		
		// then
		System.out.println("path = \r\n" + path );

		assertEquals(3, path.tiles.size);
        assertEquals(3, path.turns.size);
		
        assertPathStep(0, 0, 12, 79);
        assertPathStep(1, 0, 12, 81);
        assertPathStep(2, 0, 12, 83);
	}
	
	private void assertPathStep(int stepIndex, int turns, int x, int y) {
	    assertEquals(turns, path.turns.get(stepIndex));
	    assertTrue(path.tiles.get(stepIndex).equalsCoordinates(x, y));
	}
	
}
