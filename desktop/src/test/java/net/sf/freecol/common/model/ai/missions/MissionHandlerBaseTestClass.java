package net.sf.freecol.common.model.ai.missions;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.DI;
import promitech.colonization.ai.MissionExecutor;
import promitech.colonization.ai.MissionPlaner;
import promitech.colonization.orders.NewTurnService;
import promitech.colonization.orders.combat.CombatService;
import promitech.colonization.orders.diplomacy.FirstContactController;
import promitech.colonization.orders.move.MoveController;
import promitech.colonization.orders.move.MoveService;
import promitech.colonization.savegame.Savegame1600BaseClass;
import promitech.colonization.screen.map.hud.GUIGameController;
import promitech.colonization.screen.map.hud.GUIGameModel;

public class MissionHandlerBaseTestClass extends Savegame1600BaseClass {

	protected DI di;

	@Override
    @BeforeEach
    public void setup() throws Exception {
    	super.setup();

    	createDependencies();
    	di.guiGameModel.game = game;
    }
	
    protected void newTurnAndExecuteMission(Player player, int turns) {
		for (int i = 0; i < turns; i++) {
            newTurnAndExecuteMission(player);
        }
    }

    protected void newTurnAndExecuteMission(Player player) {
		System.out.println("### NEW TURN ###");
		boolean wasAI = player.isAi();
		player.setAi(true);
		di.newTurnService.newTurn(player);
		MissionExecutor missionExecutor = createMissionExecutor();
		missionExecutor.executeMissions(player);
		player.setAi(wasAI);
	}

	protected void planMissions(Player player) {
		MissionExecutor missionExecutor = createMissionExecutor();
		MissionPlaner missionPlaner = new MissionPlaner(game, di.pathFinder, missionExecutor);
		missionPlaner.planMissions(player);
	}

	private MissionExecutor createMissionExecutor() {
		return new MissionExecutor(
			di.guiGameModel.game,
			di.moveService,
			di.combatService,
			di.guiGameController,
			di.pathFinder
		);
	}

	DI createDependencies() {
		di = new DI();
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
