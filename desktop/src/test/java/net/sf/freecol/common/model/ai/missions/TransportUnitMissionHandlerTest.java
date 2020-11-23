package net.sf.freecol.common.model.ai.missions;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.TileAssert;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitAssert;
import net.sf.freecol.common.model.UnitFactory;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.map.path.Path;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.player.Player;

import java.util.Locale;

import promitech.colonization.DI;
import promitech.colonization.ai.MissionExecutor;
import promitech.colonization.orders.NewTurnService;
import promitech.colonization.orders.combat.CombatService;
import promitech.colonization.orders.diplomacy.FirstContactController;
import promitech.colonization.orders.move.MoveContext;
import promitech.colonization.orders.move.MoveController;
import promitech.colonization.orders.move.MoveService;
import promitech.colonization.savegame.SaveGameParser;
import promitech.colonization.screen.map.hud.GUIGameController;
import promitech.colonization.screen.map.hud.GUIGameModel;
import promitech.colonization.ui.resources.Messages;

class TransportUnitMissionHandlerTest {
	
	DI di;
	Game game;
	Player dutch;
	Player spain;
	
    Tile sourceTile;
    Tile disembarkTile;
    Tile fortOrangeTile;
    Unit galleon;
    Unit u1;
    Unit u2;
	
    @BeforeAll
    public static void beforeClass() throws Exception {
        Gdx.files = new LwjglFiles();
		Locale.setDefault(Locale.US);
		Messages.instance().load();
	}

    @BeforeEach
    public void setup() throws Exception {
    	game = SaveGameParser.loadGameFormClassPath("maps/savegame_1600_for_jtests.xml");
    	dutch = game.players.getById("player:1");
        spain = game.players.getById("player:133");
    	di = createDependencies();
    	di.guiGameModel.game = game;
    }

    @Test
	void canGoFromOneTileToAnother() throws Exception {
		// given
        game.aiContainer.missionContainer(this.dutch).clearAllMissions();

    	Tile tileSource = game.map.getTile(22, 83);
    	Tile tileDest = game.map.getTile(29, 73);

    	Unit galleon = UnitFactory.create(UnitType.GALLEON, dutch, tileSource);
    	
		Path path = di.pathFinder.findToTile(
			game.map,
			galleon.getTile(),
			tileDest,
			galleon,
			PathFinder.includeUnexploredAndExcludeNavyThreatTiles
		);
		MoveContext moveContext = new MoveContext(path);

		// when
		di.moveService.aiConfirmedMovePath(moveContext);
		newTurnAndExecuteMission(dutch);
		di.moveService.aiConfirmedMovePath(moveContext);

		// then
		TileAssert.assertThat(galleon.getTile()).isEqualsCords(tileDest);
	}
    
    @Test
    void canTranportUnits() {
        // given
        createTransportUnitMission();

        // when
        // move to europe and back to new world for colonists
        newTurnAndExecuteMission(dutch, 8);

        // move to destination
        newTurnAndExecuteMission(dutch, 2);

        // then
        UnitAssert.assertThat(u1)
        	.isNotAtLocation(galleon)
        	.isNotAtLocation(dutch.getEurope())
        	.isAtLocation(fortOrangeTile);
        UnitAssert.assertThat(u2)
	    	.isNotAtLocation(galleon)
	    	.isNotAtLocation(dutch.getEurope())
	    	.isAtLocation(disembarkTile);
        UnitAssert.assertThat(galleon)
	        .hasNoUnits()
	        .isAtLocation(fortOrangeTile)
        ;
    }

    @Test
	public void canNotEnterToColony() throws Exception {
        // given
        createTransportUnitMission();
		fortOrangeTile.getSettlement().setOwner(spain);
        
        // when
        // move to europe and back to new world for colonists
        newTurnAndExecuteMission(dutch, 8);
        // move to destination
        newTurnAndExecuteMission(dutch, 2);

		// then
        UnitAssert.assertThat(u1)
	    	.isAtLocation(galleon)
	    	.isNotAtLocation(dutch.getEurope())
	    	.isNotAtLocation(fortOrangeTile);
	    UnitAssert.assertThat(u2)
	    	.isNotAtLocation(galleon)
	    	.isNotAtLocation(dutch.getEurope())
	    	.isAtLocation(disembarkTile);
	    UnitAssert.assertThat(galleon)
	        .hasUnit(u1)
	        .isNextToLocation(game.map, fortOrangeTile)
        ;
	}
    
    @Test
	public void canNotDisembarkToOccupiedTile() throws Exception {
        // given
        createTransportUnitMission();
        UnitFactory.create(UnitType.FREE_COLONIST, spain, disembarkTile);
        
        // when
        // move to europe and back to new world for colonists
        newTurnAndExecuteMission(dutch, 8);
        // move to destination
        newTurnAndExecuteMission(dutch, 2);

		// then
        UnitAssert.assertThat(u1)
	    	.isNotAtLocation(galleon)
	    	.isNotAtLocation(dutch.getEurope())
	    	.isAtLocation(fortOrangeTile);
	    UnitAssert.assertThat(u2)
	    	.isAtLocation(galleon)
	    	.isNotAtLocation(dutch.getEurope())
	    	.isNotAtLocation(disembarkTile);
	    UnitAssert.assertThat(galleon)
	        .hasUnit(u2)
	        .isAtLocation(fortOrangeTile)
        ;
	}

	TransportUnitMission createTransportUnitMission() {
		game.aiContainer.missionContainer(this.dutch).clearAllMissions();

        sourceTile = game.map.getTile(26, 79);
        disembarkTile = game.map.getTile(27, 76);
        fortOrangeTile = game.map.getTile(25, 75);

        galleon = UnitFactory.create(UnitType.GALLEON, dutch, sourceTile);
        u1 = UnitFactory.create(UnitType.FREE_COLONIST, dutch, dutch.getEurope());
        u2 = UnitFactory.create(UnitType.FREE_COLONIST, dutch, dutch.getEurope());

        TransportUnitMission transportMission = new TransportUnitMission(galleon)
    		.addUnitDest(u2, disembarkTile)
            .addUnitDest(u1, fortOrangeTile);

        game.aiContainer.missionContainer(this.dutch).addMission(transportMission);
        
        return transportMission;
	}
    
    void newTurnAndExecuteMission(Player player, int turns) {
		for (int i = 0; i < turns; i++) {
            newTurnAndExecuteMission(player);
        }
    }

    void newTurnAndExecuteMission(Player player) {
		di.newTurnService.newTurn(player);

		MissionExecutor missionExecutor = new MissionExecutor(
			di.guiGameModel.game,
			di.moveService,
			di.combatService,
			di.guiGameController,
			di.pathFinder
		);

		missionExecutor.executeMissions(player);
	}

	DI createDependencies() {
		DI di = new DI();
		di.pathFinder = new PathFinder();
		di.moveService = new MoveService();
		di.combatService = new CombatService();
		di.guiGameModel = new GUIGameModel();
		di.newTurnService = new NewTurnService(di.guiGameModel, di.combatService, di.moveService);

		GUIGameController guiGameController = Mockito.mock(GUIGameController.class);
		MoveController moveController = Mockito.mock(MoveController.class);
		FirstContactController firstContactController = Mockito.mock(FirstContactController.class);

		di.moveService.inject(guiGameController, moveController, di.guiGameModel, di.combatService, firstContactController);
		di.combatService.inject(di.moveService, di.guiGameModel);
		return di;
	}

}
