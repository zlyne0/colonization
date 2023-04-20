package net.sf.freecol.common.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;

import net.sf.freecol.common.model.player.FoundingFather;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.GoodsType;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import promitech.colonization.Pair;
import promitech.colonization.ai.Units;
import promitech.colonization.savegame.SaveGameParser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class UnitTest {

    Game game; 
    FoundingFather ferdinandMagellan;
    FoundingFather hernandoDeSoto;
    
    @BeforeAll
    public static void beforeClass() {
        Gdx.files = new Lwjgl3Files();
    }

    @BeforeEach
    public void before() throws Exception {
        game = SaveGameParser.loadGameFormClassPath("maps/savegame_1600_for_jtests.xml");
        ferdinandMagellan = Specification.instance.foundingFathers.getById(FoundingFather.FERDINAND_MAGELLAN);
        hernandoDeSoto = Specification.instance.foundingFathers.getById("model.foundingFather.hernandoDeSoto");
    }

    @Test
    void canCalculateInitialMovesForNavyWithoutFerdinandMagellan() {
        // given
        Player player = game.players.getById("player:1");
        assertThat(player.foundingFathers.containsId(FoundingFather.FERDINAND_MAGELLAN))
		    .as("should not have founding father")
		    .isEqualTo(false);
        Unit merchantman = player.units.getById("unit:6437");
        
        // when
        int initialMovesLeft = merchantman.initialMoves();
        
        // then
        assertThat(initialMovesLeft).isEqualTo(15);
    }
    
    @Test
    void canCalculateInitialMovesForNavyWithFerdinandMagellan() {
        // given
        Player player = game.players.getById("player:1");
        if (!player.foundingFathers.containsId(FoundingFather.FERDINAND_MAGELLAN)) {
            player.addFoundingFathers(game, ferdinandMagellan);
        }
        Unit merchantman = player.units.getById("unit:6437");
        
        // when
        int initialMovesLeft = merchantman.initialMoves();
        
        // then
        assertThat(initialMovesLeft).isEqualTo(18);
    }

    @Test
	void europeanCanNotExploreRuinsWhenThereIsEnemyUnitOnIt() throws Exception {
		// given
    	Player player = game.players.getById("player:1");
    	
    	Tile srcTile = game.map.getSafeTile(23, 78);
    	Tile destTile = game.map.getSafeTile(22, 79);
    	destTile.addLostCityRumors();
    	
    	Unit unit = UnitFactory.create(UnitType.FREE_COLONIST, player, srcTile);
		
    	// when
		UnitMoveType unitMoveType = new UnitMoveType();
		MoveType moveType = unitMoveType.calculateMoveType(unit, srcTile, destTile);
    	
		// then
    	assertThat(moveType).isEqualTo(MoveType.MOVE_NO_ATTACK_CIVILIAN);
	}
    
    @Test
	void indianShouldSimpleMoveToRuins() throws Exception {
		// given
    	Player indian = game.players.getById("player:22");
    	
    	Tile srcTile = game.map.getSafeTile(23, 78);
    	Tile destTile = game.map.getSafeTile(22, 79);
    	destTile.addLostCityRumors();
    	((MapIdEntities<Unit>)destTile.getUnits()).clear();

    	Unit unit = UnitFactory.create(UnitType.FREE_COLONIST, indian, srcTile);
    	
    	// when
		UnitMoveType unitMoveType = new UnitMoveType();
		MoveType moveType = unitMoveType.calculateMoveType(unit, srcTile, destTile);

		// then
    	assertThat(moveType).isEqualTo(MoveType.MOVE);
	}

    @Test
	void canCalculateLineOfSightWithoutHernandoDeSoto() throws Exception {
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
	void canCalculateLineOfSightWithHernandoDeSoto() throws Exception {
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
    
    @Test
	void canDetermineHasMoreFreeCargoSpace() throws Exception {
		// given
    	Player dutch = game.players.getById("player:1");
    	Tile seaTile = game.map.getTile(25,80);
    	Tile landTile = game.map.getTile(24, 72);
    	
    	Unit freeColonist = UnitFactory.create(UnitType.FREE_COLONIST, dutch, landTile);
    	Unit galleon = UnitFactory.create(UnitType.GALLEON, dutch, seaTile);
    	Unit merchantman = UnitFactory.create("model.unit.merchantman", dutch, seaTile);
    	Unit galleonWithGoods = UnitFactory.create(UnitType.GALLEON, dutch, seaTile);
    	galleonWithGoods.getGoodsContainer().increaseGoodsQuantity(GoodsType.MUSKETS, 500);
    	
		// when
		assertThat(galleon.hasMoreFreeCargoSpace(freeColonist)).isTrue();
		assertThat(galleon.hasMoreFreeCargoSpace(merchantman)).isTrue();
		assertThat(galleon.hasMoreFreeCargoSpace(galleonWithGoods)).isTrue();
		
		assertThat(freeColonist.hasMoreFreeCargoSpace(galleon)).isFalse();
		assertThat(freeColonist.hasMoreFreeCargoSpace(merchantman)).isFalse();
		assertThat(freeColonist.hasMoreFreeCargoSpace(galleonWithGoods)).isFalse();

		assertThat(merchantman.hasMoreFreeCargoSpace(freeColonist)).isTrue();
		assertThat(merchantman.hasMoreFreeCargoSpace(galleon)).isFalse();
		assertThat(merchantman.hasMoreFreeCargoSpace(galleonWithGoods)).isTrue();
	}
    
    @Test
	void canSetUnitsListInFreeCargoSpaceOrder() throws Exception {
		// given
    	Player dutch = game.players.getById("player:1");
    	Tile seaTile = game.map.getTile(25,80);
    	Tile landTile = game.map.getTile(24, 72);
    	
    	Unit freeColonist = UnitFactory.create(UnitType.FREE_COLONIST, dutch, landTile);
    	Unit galleon = UnitFactory.create(UnitType.GALLEON, dutch, seaTile);
    	Unit merchantman = UnitFactory.create("model.unit.merchantman", dutch, seaTile);
    	Unit merchantman2 = UnitFactory.create("model.unit.merchantman", dutch, seaTile);
    	Unit galleonWithGoods = UnitFactory.create(UnitType.GALLEON, dutch, seaTile);
    	galleonWithGoods.getGoodsContainer().increaseGoodsQuantity(GoodsType.MUSKETS, 500);

    	List<Unit> units = Arrays.asList(
	    	freeColonist,
	    	merchantman,
	    	merchantman2,
	    	galleonWithGoods,
	    	galleon
		);

		// when
		Collections.sort(units, Units.FREE_CARGO_SPACE_COMPARATOR);

		// then
		assertThat(units).containsExactly(
			galleon,
			merchantman2,
			merchantman,
			galleonWithGoods,
			freeColonist
		);
	}

	@Test
	void shouldSortUnitsByExpertType() {
		// given
		Player dutch = game.players.getById("player:1");
		Tile landTile = game.map.getTile(24, 72);

		List<Unit> units = new ArrayList<>();
		units.add(UnitFactory.create(UnitType.FREE_COLONIST, dutch, landTile));
		units.add(UnitFactory.create(UnitType.EXPERT_FARMER, dutch, landTile));
		units.add(UnitFactory.create(UnitType.FREE_COLONIST, dutch, landTile));

		// when
		Collections.sort(units, Unit.EXPERTS_LAST_COMPARATOR);

		// then
		UnitAssert.assertThat(units.get(0)).isUnitType(UnitType.FREE_COLONIST);
		UnitAssert.assertThat(units.get(1)).isUnitType(UnitType.FREE_COLONIST);
		UnitAssert.assertThat(units.get(2)).isUnitType(UnitType.EXPERT_FARMER);
	}
}
