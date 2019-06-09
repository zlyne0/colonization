package net.sf.freecol.common.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

import net.sf.freecol.common.model.player.FoundingFather;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.Pair;
import promitech.colonization.savegame.SaveGameParser;

public class UnitTest {

    Game game; 
    FoundingFather ferdinandMagellan;
    FoundingFather hernandoDeSoto;
    
    @BeforeAll
    public static void beforeClass() {
        Gdx.files = new LwjglFiles();
    }

    @BeforeEach
    public void before() throws Exception {
        game = SaveGameParser.loadGameFormClassPath("maps/savegame_1600_for_jtests.xml");
        ferdinandMagellan = Specification.instance.foundingFathers.getById(FoundingFather.FERDINAND_MAGELLAN);
        hernandoDeSoto = Specification.instance.foundingFathers.getById("model.foundingFather.hernandoDeSoto");
    }

    @Test
    public void canCalculateInitialMovesForNavyWithoutFerdinandMagellan() {
        // given
        Player player = game.players.getById("player:1");
        assertThat(player.foundingFathers.containsId(FoundingFather.FERDINAND_MAGELLAN))
		    .as("should not have founding father")
		    .isEqualTo(false);
        Unit merchantman = player.units.getById("unit:6437");
        
        // when
        int initialMovesLeft = merchantman.getInitialMovesLeft();
        
        // then
        assertThat(initialMovesLeft).isEqualTo(15);
    }
    
    @Test
    public void canCalculateInitialMovesForNavyWithFerdinandMagellan() {
        // given
        Player player = game.players.getById("player:1");
        if (!player.foundingFathers.containsId(FoundingFather.FERDINAND_MAGELLAN)) {
            player.addFoundingFathers(game, ferdinandMagellan);
        }
        Unit merchantman = player.units.getById("unit:6437");
        
        // when
        int initialMovesLeft = merchantman.getInitialMovesLeft();
        
        // then
        assertThat(initialMovesLeft).isEqualTo(18);
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
    	assertThat(moveType).isEqualTo(MoveType.MOVE_NO_ATTACK_CIVILIAN);
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
    	assertThat(moveType).isEqualTo(MoveType.MOVE);
	}

    @Test
	public void canCalculateLineOfSightWithoutHernandoDeSoto() throws Exception {
		// given
    	Player dutch = game.players.getById("player:1");
        if (dutch.foundingFathers.containsId(hernandoDeSoto)) {
        	fail("player " + dutch + " should not have " + hernandoDeSoto);
        }
    	
    	Tile seaTile = game.map.getTile(25,80);
    	Tile landTile = game.map.getTile(24, 72);

    	List<Pair> testArgs = new ArrayList<>();
    	testArgs.add(Pair.of(UnitFactory.create(UnitType.GALLEON, dutch, seaTile), 2 ));
    	testArgs.add(Pair.of(UnitFactory.create(UnitType.SCOUT, dutch, landTile), 1 ));
    	testArgs.add(Pair.of(UnitFactory.create(UnitType.SCOUT, "model.role.scout", dutch, landTile), 2 ));
    	testArgs.add(Pair.of(UnitFactory.create(UnitType.FREE_COLONIST, dutch, landTile), 1 ));
    	
		for (Pair pair : testArgs) {
			Unit u = pair.getObj1();
			Integer expectedLineOfSight = pair.getObj2();
			// when
			int actualLineOfSight = u.lineOfSight();
			
			// then
			assertThat(actualLineOfSight)
				.as("unit " + u.toString() + " line of sight")
				.isEqualTo(expectedLineOfSight);
		}
	}
    
    @Test
	public void canCalculateLineOfSightWithHernandoDeSoto() throws Exception {
		// given
    	Player dutch = game.players.getById("player:1");
        if (!dutch.foundingFathers.containsId(hernandoDeSoto)) {
        	dutch.addFoundingFathers(game, hernandoDeSoto);
        }
    	
    	Tile seaTile = game.map.getTile(25,80);
    	Tile landTile = game.map.getTile(24, 72);

    	List<Pair> testArgs = new ArrayList<>();
    	testArgs.add(Pair.of(UnitFactory.create(UnitType.GALLEON, dutch, seaTile), 3 ));
    	testArgs.add(Pair.of(UnitFactory.create(UnitType.SCOUT, dutch, landTile), 1 ));
    	testArgs.add(Pair.of(UnitFactory.create(UnitType.SCOUT, "model.role.scout", dutch, landTile), 2 ));
    	testArgs.add(Pair.of(UnitFactory.create(UnitType.FREE_COLONIST, dutch, landTile), 1 ));
    	
		for (Pair pair : testArgs) {
			Unit u = pair.getObj1();
			Integer expectedLineOfSight = pair.getObj2();
			// when
			int actualLineOfSight = u.lineOfSight();
			
			// then
			assertThat(actualLineOfSight)
				.as("unit " + u.toString() + " line of sight")
				.isEqualTo(expectedLineOfSight);
		}
	}
}
