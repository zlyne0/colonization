package net.sf.freecol.common.model;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.FoundingFather;
import promitech.colonization.savegame.SaveGameParser;

public class UnitTest {

    Game game; 
    
    @BeforeClass
    public static void beforeClass() {
        Gdx.files = new LwjglFiles();
    }

    @Before
    public void before() throws Exception {
        game = SaveGameParser.loadGameFormClassPath("maps/savegame_1600_for_jtests.xml");
    }

    @Test
    public void canCalculateInitialMovesForNavyWithoutFerdinandMagellan() {
        // given
        Player player = game.players.getById("player:1");
        assertNull("should not have father", player.foundingFathers.getByIdOrNull(FoundingFather.FERDINAND_MAGELLAN));
        Unit merchantman = player.units.getById("unit:6437");
        
        // when
        int initialMovesLeft = merchantman.getInitialMovesLeft();
        
        // then
        assertEquals(15, initialMovesLeft);
    }
    
    @Test
    public void canCalculateInitialMovesForNavyWithFerdinandMagellan() {
        // given
        Player player = game.players.getById("player:1");
        if (player.foundingFathers.getByIdOrNull(FoundingFather.FERDINAND_MAGELLAN) == null) {
            FoundingFather foundingFather = Specification.instance.foundingFathers.getById(FoundingFather.FERDINAND_MAGELLAN);
            player.addFoundingFathers(foundingFather);
        }
        Unit merchantman = player.units.getById("unit:6437");
        
        // when
        int initialMovesLeft = merchantman.getInitialMovesLeft();
        
        // then
        assertEquals(18, initialMovesLeft);
    }

    @Test
	public void europeanCanNotExploreRuinsWhenThereIsEnemyUnitOnIt() throws Exception {
		// given
    	Player player = game.players.getById("player:1");
    	
    	Tile srcTile = game.map.getSafeTile(23, 78);
    	Tile destTile = game.map.getSafeTile(22, 79);
    	destTile.addLostCityRumors();
    	
    	Unit unit = new Unit(Game.idGenerator.nextId(Unit.class), 
    		Specification.instance.unitTypes.getById(UnitType.FREE_COLONIST), 
    		Specification.instance.unitRoles.getById(UnitRole.DEFAULT_ROLE_ID), 
    		player
    	);
    	unit.changeUnitLocation(srcTile);;
		
    	// when
    	MoveType moveType = unit.getMoveType(srcTile, destTile);
    	
		// then
    	assertEquals(MoveType.MOVE_NO_ATTACK_CIVILIAN, moveType);
	}
    
    @Test
	public void indianShouldSimpleMoveToRuins() throws Exception {
		// given
    	Player indian = game.players.getById("player:22");
    	
    	Tile srcTile = game.map.getSafeTile(23, 78);
    	Tile destTile = game.map.getSafeTile(22, 79);
    	destTile.addLostCityRumors();
    	destTile.getUnits().clear();

    	Unit unit = new Unit(Game.idGenerator.nextId(Unit.class), 
    		Specification.instance.unitTypes.getById(UnitType.FREE_COLONIST), 
    		Specification.instance.unitRoles.getById(UnitRole.DEFAULT_ROLE_ID), 
    		indian
    	);
    	unit.changeUnitLocation(srcTile);;
    	
    	// when
    	MoveType moveType = unit.getMoveType(srcTile, destTile);
    	
		// then
		assertEquals(MoveType.MOVE, moveType);
	}
}
