package net.sf.freecol.common.model.map.path;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.savegame.SaveGameParser;

public class TransportPathFinderTest {

	Game game;
	Player player;
	Unit unit;
	Unit carrier;
	Tile destTile;
	
    @BeforeClass
    public static void beforeClass() {
        Gdx.files = new LwjglFiles();
    }

    @Before
    public void setup() throws Exception {
    	game = SaveGameParser.loadGameFormClassPath("maps/sg_transport_path_junit.xml");

    	player = game.players.getById("player:1");
    	unit = player.units.getById("unit:810");
        carrier = player.units.getById("unit:811");
        destTile = game.map.getSafeTile(32, 40);
    }
	
    @Test
	public void canFindTransportPathFromIslandToIsland() throws Exception {
    	// carrier is next to unit
		// given
    	PathFinder carrierRangeMap = new PathFinder();
    	carrierRangeMap.generateRangeMap(game.map, carrier.getTile(), carrier, false);
    	
		// when
    	TransportPathFinder sut = new TransportPathFinder(game.map);
    	Path path = sut.findToTile(unit.getTile(), destTile, unit, carrier, carrierRangeMap);
    	
		// then
    	PathAssert.assertThat(path)
    		.assertPathStep(0, 32, 23)
    		.assertPathStep(0, 33, 23)
    		.assertPathStep(0, 33, 25)
    		.assertPathStep(0, 33, 27)
    		.assertPathStep(0, 33, 29)
    		.assertPathStep(0, 33, 31)
    		.assertPathStep(1, 33, 33)
    		.assertPathStep(1, 33, 35)
    		.assertPathStep(1, 33, 37)
    		.assertPathStep(1, 33, 38)
    		.assertPathStep(2, 33, 40)
    		.assertPathStep(3, 32, 40);
	}

    @Test
	public void canFindTransportPathWhenCarrierChangeInitialPosition() throws Exception {
    	// carrier is far from unit
    	// given
    	Tile carrierSourceTile = game.map.getTile(27, 25);
    	carrier.changeUnitLocation(carrierSourceTile);

    	PathFinder carrierRangeMap = new PathFinder();
    	carrierRangeMap.generateRangeMap(game.map, carrier.getTile(), carrier, false);
    	
		// when
    	TransportPathFinder sut = new TransportPathFinder(game.map);
    	Path path = sut.findToTile(unit.getTile(), destTile, unit, carrier, carrierRangeMap);
    	
		// then
    	PathAssert.assertThat(path)
			.assertPathStep(0, 32, 23)
			.assertPathStep(0, 32, 24)
			.assertPathStep(1, 31, 24)
			.assertPathStep(1, 31, 26)
			.assertPathStep(1, 31, 28)
			.assertPathStep(1, 31, 30)
			.assertPathStep(1, 31, 32)
			.assertPathStep(2, 31, 34)
			.assertPathStep(2, 31, 35)
			.assertPathStep(2, 32, 36)
			.assertPathStep(3, 32, 38)
			.assertPathStep(4, 32, 40);
	}
    
    @Test
	public void canFindTransportPathWhenUnitIsOnCarrier() throws Exception {
		// given
    	Tile carrierSourceTile = game.map.getTile(27, 25);
    	carrier.changeUnitLocation(carrierSourceTile);
    	unit.changeUnitLocation(carrier);

    	PathFinder carrierRangeMap = new PathFinder();
    	carrierRangeMap.generateRangeMap(game.map, carrier.getTile(), carrier, false);
    	
		// when
    	TransportPathFinder sut = new TransportPathFinder(game.map);
    	Path path = sut.findToTile(carrier.getTile(), destTile, unit, carrier, carrierRangeMap);
    	
		// then
    	PathAssert.assertThat(path)
			.assertPathStep(0, 27, 25)
			.assertPathStep(0, 27, 27)
			.assertPathStep(0, 27, 29)
			.assertPathStep(0, 28, 29)
			.assertPathStep(0, 29, 29)
			.assertPathStep(1, 29, 31)
			.assertPathStep(1, 30, 31)
			.assertPathStep(1, 30, 33)
			.assertPathStep(1, 31, 33)
			.assertPathStep(2, 31, 35)
			.assertPathStep(2, 32, 36)
			.assertPathStep(3, 32, 38)
			.assertPathStep(4, 32, 40);
	}
}
