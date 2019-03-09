package net.sf.freecol.common.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.FoundingFather;
import promitech.colonization.savegame.SaveGameParser;

public class UnitTest {

    Game game; 
    
    @BeforeAll
    public static void beforeClass() {
        Gdx.files = new LwjglFiles();
    }

    @BeforeEach
    public void before() throws Exception {
        game = SaveGameParser.loadGameFormClassPath("maps/savegame_1600_for_jtests.xml");
    }

    @Test
    public void canCalculateInitialMovesForNavyWithoutFerdinandMagellan() {
        // given
        Player player = game.players.getById("player:1");
        assertFalse(player.foundingFathers.containsId(FoundingFather.FERDINAND_MAGELLAN), "should not have father");
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
        if (!player.foundingFathers.containsId(FoundingFather.FERDINAND_MAGELLAN)) {
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
    	
    	Unit unit = UnitFactory.create(UnitType.FREE_COLONIST, player, srcTile);
		
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
    	((MapIdEntities<Unit>)destTile.getUnits()).clear();

    	Unit unit = UnitFactory.create(UnitType.FREE_COLONIST, indian, srcTile);
    	
    	// when
    	MoveType moveType = unit.getMoveType(srcTile, destTile);
    	
		// then
		assertEquals(MoveType.MOVE, moveType);
	}
}
