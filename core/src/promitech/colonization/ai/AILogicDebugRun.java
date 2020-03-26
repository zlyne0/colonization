package promitech.colonization.ai;

import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.ai.missions.AbstractMission;
import net.sf.freecol.common.model.ai.missions.FoundColonyMission;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;
import net.sf.freecol.common.model.ai.missions.RellocationMission;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.map.path.TransportPathFinder;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.orders.NewTurnService;
import promitech.colonization.orders.combat.CombatService;
import promitech.colonization.orders.move.MoveService;
import promitech.colonization.screen.map.MapActor;
import promitech.colonization.screen.map.hud.GUIGameController;
import promitech.colonization.screen.map.hud.GUIGameModel;

public class AILogicDebugRun {

    private GUIGameModel gameModel;
    private final MoveService moveService;
    private final MapActor mapActor;
    
    private final TileDebugView tileDebugView;
    private final AILogic aiLogic;
    
    public AILogicDebugRun(
		GUIGameModel gameModel, 
		MoveService moveService, MapActor mapActor, 
		CombatService combatService, 
		GUIGameController guiGameController
	) {
    	this.moveService = moveService;
        this.gameModel = gameModel;
        this.mapActor = mapActor;

        tileDebugView = new TileDebugView(mapActor, gameModel);
        
        NewTurnService newTurnService = new NewTurnService(gameModel, combatService, moveService);
        aiLogic = new AILogic(gameModel.game, newTurnService, moveService, guiGameController);
    }
    
    public void runMission(Player player, AbstractMission mission) {
    	PlayerMissionsContainer missionContainer = gameModel.game.aiContainer.getMissionContainer(player);
    	aiLogic.executeSingleMission(missionContainer, mission);
    }
}
