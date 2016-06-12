package net.sf.freecol.common.model.map;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

import net.sf.freecol.common.model.Building;
import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.player.Player;
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
		path = sut.findToTile(game.map, startTile, endTile, moveUnit);
        
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
        path = sut.findToTile(game.map, startTile, endTile, moveUnit);
		
		// then
		System.out.println("path = \r\n" + path );

		assertEquals(3, path.tiles.size);
        assertEquals(3, path.turns.size);
		
        assertPathStep(0, 0, 12, 79);
        assertPathStep(1, 0, 12, 81);
        assertPathStep(2, 0, 12, 83);
	}
	
	@Test
    public void canFindNavyPathAndOmitFortressTile() throws Exception {
        // given
        Tile startTile = game.map.getTile(26, 77);
        Tile endTile = game.map.getTile(28, 72);

        Tile privateerTile = game.map.getTile(12, 79);
        Unit privateer = privateerTile.units.getById("unit:6900");
        privateerTile.units.removeId(privateer);
        startTile.units.add(privateer);

        Player fortressOwner = game.players.getById("player:112");
        Colony fortressColony = new Colony("colony:-1");
        fortressColony.setOwner(fortressOwner);

        Building fortressBuilding = new Building("building:-1", Specification.instance.buildingTypes.getById("model.building.fortress"));
        fortressBuilding.buildingType = Specification.instance.buildingTypes.getById("model.building.fortress");
        fortressColony.buildings.add(fortressBuilding);
        fortressColony.updateColonyFeatures();

        Tile fortressTile = game.map.getTile(27, 75);
        fortressTile.setSettlement(fortressColony);

        // when
        path = sut.findToTile(game.map, startTile, endTile, privateer);

        // then
        System.out.println("path = \r\n" + path);
        
        assertEquals(8, path.tiles.size);
        assertEquals(8, path.turns.size);

        assertPathStep(0, 0, 26, 77);
        assertPathStep(1, 0, 27, 78);
        assertPathStep(2, 0, 28, 78);
        assertPathStep(3, 0, 29, 78);
        assertPathStep(4, 0, 29, 76);
        assertPathStep(5, 0, 29, 74);
        assertPathStep(6, 0, 28, 73);
        assertPathStep(7, 0, 28, 72);
    }
	
	@Test
    public void canFindNavyPathAndDoesNotOmitFortressTile() throws Exception {
        // given
        Tile startTile = game.map.getTile(26, 77);
        Tile endTile = game.map.getTile(28, 72);

        Tile privateerTile = game.map.getTile(12, 79);
        Unit privateer = privateerTile.units.getById("unit:6900");
        privateerTile.units.removeId(privateer);
        startTile.units.add(privateer);

        // when
        path = sut.findToTile(game.map, startTile, endTile, privateer);

        // then
        System.out.println("path = \r\n" + path);

        assertEquals(5, path.tiles.size);
        assertEquals(5, path.turns.size);
        
        assertPathStep(0, 0, 26, 77);
        assertPathStep(1, 0, 27, 77);
        assertPathStep(2, 0, 28, 76);
        assertPathStep(3, 0, 28, 74);
        assertPathStep(4, 0, 28, 72);
    }
	
	@Test
    public void canFindPathToEurope() throws Exception {
        // given
        Tile startTile = game.map.getTile(12, 79);
        Unit moveUnit = startTile.units.getById("unit:6900");

        // when
	    path = sut.findToEurope(game.map, startTile, moveUnit);
        // then
	    System.out.println("path = " + path);

        assertPathStep(0, 0, 12, 79);
        assertPathStep(1, 0, 12, 81);
        assertPathStep(2, 0, 12, 83);
        assertPathStep(3, 0, 12, 85);
        assertPathStep(4, 0, 12, 87);
        assertPathStep(5, 0, 12, 89);
        assertPathStep(6, 0, 13, 89);
        assertPathStep(7, 0, 14, 89);
        assertPathStep(8, 0, 15, 89);
        assertPathStep(9, 1, 16, 89);
        assertPathStep(10, 1, 17, 89);
        assertPathStep(11, 1, 18, 89);
        assertPathStep(12, 1, 19, 89);
        assertPathStep(13, 1, 20, 89);
        assertPathStep(14, 1, 21, 89);
        assertPathStep(15, 1, 22, 89);
        assertPathStep(16, 1, 23, 90);
        assertPathStep(17, 2, 24, 90);
        assertPathStep(18, 2, 25, 90);
        assertPathStep(19, 2, 26, 90);
        assertPathStep(20, 2, 27, 90);
        assertPathStep(21, 2, 28, 90);
        assertPathStep(22, 2, 29, 90);
        assertPathStep(23, 2, 30, 90);
        assertPathStep(24, 2, 30, 88);    
    }
	
	private void assertPathStep(int stepIndex, int turns, int x, int y) {
	    assertEquals(turns, path.turns.get(stepIndex));
	    assertTrue(path.tiles.get(stepIndex).equalsCoordinates(x, y));
	}
	
}
