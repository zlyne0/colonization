package promitech.colonization.ai;

import net.sf.freecol.common.model.ai.missions.AbstractMission;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.orders.combat.CombatService;
import promitech.colonization.orders.move.MoveService;
import promitech.colonization.screen.map.MapActor;
import promitech.colonization.screen.map.hud.GUIGameController;
import promitech.colonization.screen.map.hud.GUIGameModel;

public class MissionExecutorDebugRun {

    private GUIGameModel gameModel;
    private final MoveService moveService;
    private final MapActor mapActor;
    
    private final TileDebugView tileDebugView;
    private final MissionExecutor missionExecutor;
    
    public MissionExecutorDebugRun(
		GUIGameModel gameModel, 
		MoveService moveService, MapActor mapActor, 
		CombatService combatService, 
		GUIGameController guiGameController,
		PathFinder pathFinder
	) {
    	this.moveService = moveService;
        this.gameModel = gameModel;
        this.mapActor = mapActor;

        tileDebugView = new TileDebugView(mapActor, gameModel);
        missionExecutor = new MissionExecutor(gameModel.game, moveService, combatService, guiGameController, pathFinder);
    }
    
    public void runMission(Player player, AbstractMission mission) {
    	PlayerMissionsContainer missionContainer = gameModel.game.aiContainer.getMissionContainer(player);
    	missionExecutor.executeSingleMission(missionContainer, mission);
    }
}
