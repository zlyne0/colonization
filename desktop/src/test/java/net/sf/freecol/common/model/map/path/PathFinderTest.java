package net.sf.freecol.common.model.map.path;

import static net.sf.freecol.common.model.map.path.PathFinder.FlagTypes;
import static net.sf.freecol.common.model.map.path.PathFinder.FlagTypes.AllowCarrierEnterWithGoods;
import static net.sf.freecol.common.model.map.path.PathFinder.FlagTypes.AllowEmbark;
import static net.sf.freecol.common.model.map.path.PathFinder.FlagTypes.AvoidDisembark;
import static net.sf.freecol.common.model.map.path.PathFinder.INFINITY;
import static net.sf.freecol.common.model.map.path.PathFinder.excludeUnexploredTiles;
import static net.sf.freecol.common.model.map.path.PathFinder.includeUnexploredAndExcludeNavyThreatTiles;
import static net.sf.freecol.common.model.map.path.PathFinder.includeUnexploredTiles;
import static net.sf.freecol.common.util.CollectionUtils.enumSum;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.ColonyFactory;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.SettlementFactory;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.TileAssert;
import net.sf.freecol.common.model.TileImprovement;
import net.sf.freecol.common.model.TileImprovementType;
import net.sf.freecol.common.model.TileType;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitAssert;
import net.sf.freecol.common.model.UnitFactory;
import net.sf.freecol.common.model.UnitRole;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.player.Player;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

import promitech.colonization.Direction;
import promitech.colonization.savegame.SaveGameParser;
import promitech.colonization.ui.resources.Messages;
import promitech.map.isometric.NeighbourIterableTile;

class PathFinderTest {

    Path path;
    Game game;
    PathFinder sut = new PathFinder();
    Player dutch;
	Colony nieuwAmsterdam;
	Colony fortNassau;

	@BeforeAll
    public static void beforeClass() {
        Gdx.files = new Lwjgl3Files();
		Locale.setDefault(Locale.US);
		Messages.instance().load();
    }

    @BeforeEach
    public void setup() throws Exception {
    	game = SaveGameParser.loadGameFormClassPath("maps/savegame_1600_for_jtests.xml");
    	dutch = game.players.getById("player:1");

		nieuwAmsterdam = game.map.getTile(24, 78).getSettlement().asColony();
		fortNassau = game.map.getTile(20, 79).getSettlement().asColony();
    }
    
	@Test
	void canFindLandPathForColonist() throws Exception {
		// given
        Tile startTile = game.map.getTile(24, 78);
        Tile endTile = game.map.getTile(21, 72);
        Unit moveUnit = startTile.getUnits().getById("unit:6762");
        
		// when
		path = sut.findToTile(game.map, startTile, endTile, moveUnit, excludeUnexploredTiles);
        
		// then
		System.out.println("path = \r\n" + path );
		
		assertEquals(7, path.tiles.size);
        assertEquals(7, path.turns.size);

        PathAssert.assertThat(path)
        	.reachedDestination()
	        .assertPathStep(0, 0, 24, 78)
	        .assertPathStep(1, 0, 23, 78)
	        .assertPathStep(2, 0, 23, 76)
	        .assertPathStep(3, 0, 22, 75)
	        .assertPathStep(4, 1, 22, 74)
	        .assertPathStep(5, 1, 22, 72)
	        .assertPathStep(6, 2, 21, 72);
        
        assertTrue(path.startTile.equalsCoordinates(24, 78));
        assertTrue(path.endTile.equalsCoordinates(21, 72));
	}

	@Test
	void canFindMarinePathForNavy() throws Exception {
		// given
        Tile startTile = game.map.getTile(12, 79);
        Tile endTile = game.map.getTile(12, 83);
        Unit moveUnit = startTile.getUnits().getById("unit:6900");
		
		// when
        path = sut.findToTile(game.map, startTile, endTile, moveUnit, excludeUnexploredTiles);
		
		// then
		System.out.println("path = \r\n" + path );

		assertEquals(3, path.tiles.size);
        assertEquals(3, path.turns.size);
		
        PathAssert.assertThat(path)
        	.reachedDestination()
	        .assertPathStep(0, 0, 12, 79)
	        .assertPathStep(1, 0, 12, 81)
	        .assertPathStep(2, 0, 12, 83);
	}
	
	@Test
    void canFindNavyPathAndOmitFortressTile() throws Exception {
        // given
        Tile startTile = game.map.getTile(26, 77);
        Tile endTile = game.map.getTile(28, 72);

        Tile privateerTile = game.map.getTile(12, 79);
        Unit privateer = privateerTile.getUnits().getById("unit:6900");
        privateer.changeUnitLocation(startTile);

        Tile fortressTile = game.map.getTile(27, 75);
        
        Player fortressOwner = game.players.getById("player:112");
        Unit freeColonist = UnitFactory.create(UnitType.FREE_COLONIST, fortressOwner, fortressTile);
        
        ColonyFactory colonyFactory = new ColonyFactory(game, sut);
        Colony fortressColony = colonyFactory.buildColony(freeColonist, fortressTile, "fortress colony");
        fortressColony.addBuilding(Specification.instance.buildingTypes.getById("model.building.fortress"));
        fortressColony.updateColonyFeatures();


        // when
        path = sut.findToTile(game.map, startTile, endTile, privateer, excludeUnexploredTiles);

        // then
        System.out.println("path = \r\n" + path);
        
        assertEquals(8, path.tiles.size);
        assertEquals(8, path.turns.size);

        PathAssert.assertThat(path)
        	.reachedDestination()
	        .assertPathStep(0, 0, 26, 77)
	        .assertPathStep(1, 0, 27, 78)
	        .assertPathStep(2, 0, 28, 78)
	        .assertPathStep(3, 0, 29, 78)
	        .assertPathStep(4, 0, 29, 76)
	        .assertPathStep(5, 0, 29, 74)
	        .assertPathStep(6, 0, 28, 73)
	        .assertPathStep(7, 0, 28, 72);
    }
	
	@Test
    void canFindNavyPathAndDoesNotOmitFortressTile() throws Exception {
        // given
        Tile startTile = game.map.getTile(26, 77);
        Tile endTile = game.map.getTile(28, 72);

        Tile privateerTile = game.map.getTile(12, 79);
        Unit privateer = privateerTile.getUnits().getById("unit:6900");
        privateer.changeUnitLocation(startTile);

        // when
        path = sut.findToTile(game.map, startTile, endTile, privateer, excludeUnexploredTiles);

        // then
        System.out.println("path = \r\n" + path);

        assertEquals(5, path.tiles.size);
        assertEquals(5, path.turns.size);
        
        PathAssert.assertThat(path)
        	.reachedDestination()
	        .assertPathStep(0, 0, 26, 77)
	        .assertPathStep(1, 0, 27, 77)
	        .assertPathStep(2, 0, 28, 76)
	        .assertPathStep(3, 0, 28, 74)
	        .assertPathStep(4, 0, 28, 72);
    }
	
	@Test
    void canFindPathToEurope() throws Exception {
        // given
        Tile startTile = game.map.getTile(12, 79);
        Unit moveUnit = startTile.getUnits().getById("unit:6900");

        // when
	    path = sut.findToEurope(game.map, startTile, moveUnit, excludeUnexploredTiles);
        // then
	    System.out.println("path = " + path);

        PathAssert.assertThat(path)
        	.reachedDestination()
	        .assertPathStep(0, 0, 12, 79)
	        .assertPathStep(1, 0, 12, 81)
	        .assertPathStep(2, 0, 12, 83)
	        .assertPathStep(3, 0, 12, 85)
	        .assertPathStep(4, 0, 12, 87)
	        .assertPathStep(5, 0, 12, 89)
	        .assertPathStep(6, 0, 13, 89)
	        .assertPathStep(7, 0, 14, 89)
	        .assertPathStep(8, 0, 15, 89)
	        .assertPathStep(9, 1, 16, 89)
	        .assertPathStep(10, 1, 17, 89)
	        .assertPathStep(11, 1, 18, 89)
	        .assertPathStep(12, 1, 19, 89)
	        .assertPathStep(13, 1, 20, 89)
	        .assertPathStep(14, 1, 21, 89)
	        .assertPathStep(15, 1, 22, 89)
	        .assertPathStep(16, 1, 23, 90)
	        .assertPathStep(17, 2, 24, 90)
	        .assertPathStep(18, 2, 25, 90)
	        .assertPathStep(19, 2, 26, 90)
	        .assertPathStep(20, 2, 27, 90)
	        .assertPathStep(21, 2, 28, 90)
	        .assertPathStep(22, 2, 29, 90)
	        .assertPathStep(23, 2, 30, 90)
	        .assertPathStep(24, 2, 30, 88);    
    }

	@Test
	void colonistsShouldNotMoveViaEnemy() throws Exception {
		// given
		Player dutch = game.players.getById("player:1");
		Unit colonist = UnitFactory.create(UnitType.FREE_COLONIST, dutch, game.map.getSafeTile(24, 77));

		Player aztec = game.players.getById("player:40");
		UnitFactory.create(UnitType.BRAVE, aztec, game.map.getSafeTile(23, 79));
		UnitFactory.create(UnitType.BRAVE, aztec, game.map.getSafeTile(23, 78));
		UnitFactory.create(UnitType.BRAVE, aztec, game.map.getSafeTile(23, 77));
		UnitFactory.create(UnitType.BRAVE, aztec, game.map.getSafeTile(24, 76));
		UnitFactory.create(UnitType.BRAVE, aztec, game.map.getSafeTile(24, 75));
		
		Tile destTile = game.map.getSafeTile(23, 80);
		
		// when
		path = sut.findToTile(game.map, colonist.getTile(), destTile, colonist, excludeUnexploredTiles);

		// then
		for (Tile t : path.tiles) {
			if (t.hasSettlement()) {
				TileAssert.assertThat(t).hasSettlementOwnBy(dutch);
			}
			if (t.getUnits().isNotEmpty()) {
				UnitAssert.assertThat(t.getUnits().first()).isOwnedBy(dutch);
			}
		}
		PathAssert.assertThat(path)
			.reachedDestination()
			.lastStepEquals(destTile.x, destTile.y);
	}
	
	@Test
	void shouldReturnEmptyPathWhenCanNotFindNavyWayToEurope() throws Exception {
		// given
		Tile startTile = game.map.getSafeTile(23, 70);
    	Unit galleon = new Unit(
			"tmp:buildColony:findToEurope:-1",
			Specification.instance.unitTypes.getById(UnitType.GALLEON),
			Specification.instance.unitRoles.getById(UnitRole.DEFAULT_ROLE_ID),
			game.playingPlayer
		);
    	
		// when
		Path pathToEurope = sut.findToEurope(game.map, startTile, galleon, excludeUnexploredTiles);

		// then
		PathAssert.assertThat(pathToEurope)
			.notReachedDestination()
			.isEmpty();
	}
	
	@Test
	void shouldNotFindPathBecauseBlockedByEnemy() throws Exception {
		// given
		Player dutch = game.players.getById("player:1");
		Unit colonist = UnitFactory.create(UnitType.FREE_COLONIST, dutch, game.map.getSafeTile(23, 80));

		Player aztec = game.players.getById("player:40");
		UnitFactory.create(UnitType.BRAVE, aztec, game.map.getSafeTile(23, 79));
		UnitFactory.create(UnitType.BRAVE, aztec, game.map.getSafeTile(23, 78));
		UnitFactory.create(UnitType.BRAVE, aztec, game.map.getSafeTile(23, 77));
		UnitFactory.create(UnitType.BRAVE, aztec, game.map.getSafeTile(24, 76));
		UnitFactory.create(UnitType.BRAVE, aztec, game.map.getSafeTile(24, 77));
		
		Tile nieuwAmsterdamTile = game.map.getSafeTile(24, 78);
		
		// when
		path = sut.findToTile(game.map, colonist.getTile(), nieuwAmsterdamTile, colonist, excludeUnexploredTiles);
		
		// then
		PathAssert.assertThat(path)
			.notReachedDestination();
	}
	
	@Test
	void shouldNotFindPathBetweenIslands() throws Exception {
		// given
		Player dutch = game.players.getById("player:1");
		Unit colonist = UnitFactory.create(UnitType.FREE_COLONIST, dutch, game.map.getSafeTile(23, 80));

		Tile tileOnIsland = game.map.getSafeTile(24, 89);
		
		// when
		path = sut.findToTile(game.map, colonist.getTile(), tileOnIsland, colonist, excludeUnexploredTiles);

		// then
		PathAssert.assertThat(path)
			.notReachedDestination();
	}
	
	@Test
	void indianShouldFindPathFromSettlementToColony() throws Exception {
		// given
		Tile fromTile = game.map.getSafeTile(19, 78);
		Tile toTile = game.map.getSafeTile(20, 79);
		
		Player inca = game.players.getById("player:154");
		Unit brave = UnitFactory.create(UnitType.BRAVE, inca, fromTile);
		
		// when
		path = sut.findToTile(game.map, fromTile, toTile, brave, includeUnexploredTiles);

		// then
        PathAssert.assertThat(path)
	    	.reachedDestination()
	        .assertPathStep(0, 0, 19, 78)
	        .assertPathStep(1, 0, 20, 78)
	        .assertPathStep(2, 1, 20, 79);
	}

	@Test
	void shouldFindPathThroughHighSea() {
		// given
		Tile dest = game.map.getSafeTile(25, 86);
		Tile source = game.map.getSafeTile(29, 86);

		TileType highSeas = Specification.instance.tileTypes.getById(TileType.HIGH_SEAS);
		for (NeighbourIterableTile<Tile> neighbourTile : game.map.neighbourTiles(source)) {
			neighbourTile.tile.changeTileType(highSeas);
		}
		Unit caravel = UnitFactory.create(UnitType.CARAVEL, dutch, source);

		// when
		path = sut.findTheQuickestPath(game.map, source, Arrays.asList(dest), caravel, includeUnexploredAndExcludeNavyThreatTiles);
		//path = sut.findToTile(game.map, source, dest, caravel, PathFinder.includeUnexploredAndExcludeNavyThreatTiles);

		// then
		//System.out.println("path = " + path);

		PathAssert.assertThat(path)
			.reachedDestination()
			.assertPathStep(0, 0, 29, 86)
			.assertPathStep(1, 0, 28, 86)
			.assertPathStep(2, 0, 27, 86)
			.assertPathStep(3, 0, 26, 86)
			.assertPathStep(4, 0, 25, 86)
		;
	}

	@Test
	void shouldGenerateRangeToIndianSettlement() {
		// given
		Tile source = game.map.getTile(22, 80);
		Unit scout = UnitFactory.create(UnitType.SCOUT, UnitRole.SCOUT, dutch, source);

		// when
		sut.generateRangeMap(game.map, scout, includeUnexploredTiles);

		// then
		assertThat(sut.turnsCost(game.map.getTile(19, 78))).isEqualTo(0);
		assertThat(sut.turnsCost(game.map.getTile(15, 85))).isEqualTo(1);
	}

	@Test
	void shouldGenerateRangeMapWithoutDisembarkTiles() {
		// given
		Tile sourceTile = game.map.getTile(26, 83);
		Unit caravel = UnitFactory.create(UnitType.CARAVEL, dutch, sourceTile);

		// when
		Set<FlagTypes> flagTypes = enumSum(includeUnexploredTiles, AvoidDisembark);
		sut.generateRangeMap(game.map, caravel, flagTypes);

		// then
		assertThat(sut.turnsCost(game.map.getTile(23, 80))).isEqualTo(INFINITY);
		assertThat(sut.turnsCost(nieuwAmsterdam.tile)).isEqualTo(1);
	}

	@Test
	void shouldGenerateRangeMapWithEmbarkTiles() {
		// given
		Unit scout = UnitFactory.create(UnitType.SCOUT, UnitRole.SCOUT, dutch, fortNassau.tile);

		// when
		Set<FlagTypes> flagTypes = enumSum(includeUnexploredTiles, AllowEmbark);
		sut.generateRangeMap(
			game.map,
			fortNassau.tile,
			scout,
			flagTypes
		);

		// then
		// in middle of ocean
		assertThat(sut.turnsCost(game.map.getTile(22, 85))).isEqualTo(INFINITY);
		// ocean next to FortNassau
		assertThat(sut.turnsCost(game.map.getTile(20, 81))).isEqualTo(0);
		// ocean next to land, two tiles from colony
		assertThat(sut.turnsCost(game.map.getTile(21, 81))).isEqualTo(0);
	}

	@Test
	void canGenerateDirectionInto() {
		// given
		Tile sourceTile = game.map.getTile(28, 81);
		Tile westTile = game.map.getTile(sourceTile, Direction.W);

		Unit ship = UnitFactory.create(UnitType.CARAVEL, dutch, sourceTile);
		sut.generateRangeMap(game.map, ship, includeUnexploredTiles);
		int westTileIndex = sut.grid.toIndex(westTile.x, westTile.y);

		// when
		Direction directionInto = sut.getDirectionInto(westTileIndex);
		Path path = sut.createPath(westTile);

		// then
		assertThat(directionInto).isEqualTo(Direction.W);
		PathAssert.assertThat(path)
			.reachedDestination()
			.assertPathStep(0, 0, 28, 81)
			.assertPathStep(1, 0, 27, 81)
		;
	}

	@Test
	void shouldGenerateMaxRangePath() {
		// given
		Unit unit = UnitFactory.create(UnitType.FREE_COLONIST, dutch, nieuwAmsterdam.tile);
		int maxTurnsRange = 3;

		// when
		sut.generateRangeMap(game.map, nieuwAmsterdam.tile, unit, includeUnexploredTiles, maxTurnsRange);

		// then
		assertThat(sut.turnsCost(game.map.getTile(24, 68))).isEqualTo(3);
		assertThat(sut.turnsCost(game.map.getTile(24, 67))).isEqualTo(INFINITY);
	}

	@Nested
	class EnterSettlementsWithCarrierAndGoodsTest {

		Tile seaTile;
		Tile islandTile;
		Unit ship;

		@BeforeEach
		void beforeEach() {
			seaTile = game.map.getTile(28, 82);
			islandTile = game.map.getTile(25, 86);

			Player nativePlayer = game.players.getById("player:22");
			SettlementFactory settlementFactory = new SettlementFactory(game.map);
			settlementFactory.create(nativePlayer, islandTile, nativePlayer.nationType().getSettlementRegularType());

			ship = UnitFactory.create(UnitType.GALLEON, dutch, seaTile);
		}

		@Test
		void shouldFindPathWhenCanEnter() {
			// when
			Path path = sut.findToTile(game.map, ship, islandTile,
				enumSum(includeUnexploredTiles, AllowCarrierEnterWithGoods)
			);
//			printPathAsAssertPathCode(path);

			// then
			PathAssert.assertThat(path)
				.reachedDestination()
				.assertPathStep(0, 0, 28, 82)
				.assertPathStep(1, 0, 27, 82)
				.assertPathStep(2, 0, 26, 82)
				.assertPathStep(3, 0, 26, 84)
				.assertPathStep(4, 0, 26, 86)
				.assertPathStep(5, 0, 25, 86);
		}

		@Test
		void shouldNotFindPathWhenCanNotEnter() {
			// when
			Path path = sut.findToTile(game.map, ship, islandTile, includeUnexploredTiles);
			//printPathAsAssertPathCode(path);

			// then
			PathAssert.assertThat(path)
				.notReachedDestination();
		}
	}

	@Nested
	class ShortPathByRoad {
		Tile t1;
		Tile t2;
		Tile t3;
		Tile t4;
		Tile t5;
		Tile t6;

		@BeforeEach
		void setup() {
//    56
//   4  3
//	  12
			t1 = game.map.getTile(28, 90);
			t2 = game.map.getTile(28, 89);
			t3 = game.map.getTile(28, 87);
			t4 = game.map.getTile(27, 90);
			t5 = game.map.getTile(27, 88);
			t6 = game.map.getTile(27, 87);
			generateLand();
		}

		@Test
		void should_generate_the_shorter_path_without_road() {
			// given
			Unit unit = UnitFactory.create(UnitType.FREE_COLONIST, dutch, t1);

			// when
			Path path = sut.findToTile(game.map, unit, t3, includeUnexploredTiles);

			// then
			PathAssert.assertThat(path)
					.reachedDestination()
					.assertPathStep(0, 0, t1)
					.assertPathStep(1, 0, t2)
					.assertPathStep(2, 1, t3);
			assertThat(sut.totalCost(t3)).isEqualTo(106);
		}

		@Test
		void should_generate_the_shorter_path_with_road() {
			// given
			generateRoad();
			Unit unit = UnitFactory.create(UnitType.FREE_COLONIST, dutch, t1);

			// when
			Path path = sut.findToTile(game.map, unit, t3, includeUnexploredTiles);

			// then
			PathAssert.assertThat(path)
					.reachedDestination()
					.assertPathStep(0, 0, t1)
					.assertPathStep(1, 0, t4)
					.assertPathStep(2, 0, t5)
					.assertPathStep(3, 0, t6)
					.assertPathStep(4, 1, t3)
			;
			assertThat(sut.totalCost(t3)).isEqualTo(104);
		}

		void generateLand() {
			t1.changeTileType(Specification.instance.tileTypes.getById(TileType.PLAINS));
			t2.changeTileType(Specification.instance.tileTypes.getById(TileType.PLAINS));
			t3.changeTileType(Specification.instance.tileTypes.getById(TileType.PLAINS));
			t4.changeTileType(Specification.instance.tileTypes.getById(TileType.PLAINS));
			t5.changeTileType(Specification.instance.tileTypes.getById(TileType.PLAINS));
			t6.changeTileType(Specification.instance.tileTypes.getById(TileType.PLAINS));
		}

		void generateRoad() {
			TileImprovementType road = Specification.instance.tileImprovementTypes.getById(TileImprovementType.ROAD_MODEL_IMPROVEMENT_TYPE_ID);
			t1.addImprovement(new TileImprovement(Game.idGenerator, road));
			t4.addImprovement(new TileImprovement(Game.idGenerator, road));
			t5.addImprovement(new TileImprovement(Game.idGenerator, road));
			t6.addImprovement(new TileImprovement(Game.idGenerator, road));
			t3.addImprovement(new TileImprovement(Game.idGenerator, road));

			t1.updateRoadConnections(game.map);
			t2.updateRoadConnections(game.map);
			t3.updateRoadConnections(game.map);
			t4.updateRoadConnections(game.map);
			t5.updateRoadConnections(game.map);
			t6.updateRoadConnections(game.map);
		}
	}

	void printPathAsAssertPathCode(Path path) {
		System.out.println("path = " + path);

		String str = "PathAssert.assertThat(path)\n";
		if (path.isReachedDestination()) {
			str += "\t.reachedDestination()\n";
		} else {
			str += "\t.notReachedDestination()\n";
		}
		for (int i=0; i<path.tiles.size; i++) {
			Tile tile = path.tiles.get(i);
			String tileStr = ".assertPathStep(" + i + ", " + path.turns.get(i) +  ", " + tile.x + ", " + tile.y + ")";
			str += "\t" + tileStr + "\n";
		}
		System.out.println(str);
	}
}
