package promitech.colonization.ai;

import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.ai.missions.FoundColonyMission;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;
import net.sf.freecol.common.model.ai.missions.RellocationMission;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.map.path.TransportPathFinder;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.GUIGameModel;
import promitech.colonization.GameLogic;
import promitech.colonization.actors.map.MapActor;
import promitech.colonization.move.MoveService;

public class AILogicDebugRun {

    private final PathFinder pathFinder = new PathFinder();
    private final TransportPathFinder transportPathFinder;
    private final ExplorerMissionHandler explorerMissionHandler;
    private GUIGameModel gameModel;
    private final MoveService moveService;
    private final MapActor mapActor;
    
    private final TileDebugView tileDebugView;
    private final FoundColonyMissionHandler foundColonyMissionHandler;
    private final RellocationMissionHandler rellocationMissionHandler;
    
    public AILogicDebugRun(GUIGameModel gameModel, MoveService moveService, MapActor mapActor) {
    	this.moveService = moveService;
        this.gameModel = gameModel;
        this.mapActor = mapActor;
        
        transportPathFinder = new TransportPathFinder(gameModel.game.map);
        explorerMissionHandler = new ExplorerMissionHandler(gameModel.game, pathFinder, moveService);

        tileDebugView = new TileDebugView(mapActor, gameModel);
        foundColonyMissionHandler = new FoundColonyMissionHandler(pathFinder, gameModel.game);
        rellocationMissionHandler = new RellocationMissionHandler(pathFinder, transportPathFinder, gameModel.game, moveService);
    }
    
    public void run() {
        Unit unit = gameModel.getActiveUnit();
//        ExplorerMission explorerMission = new ExplorerMission(unit);
//        explorerMissionHandler.executeMission(explorerMission);
//        explorerMissionHandler.exploreByOneMove(unit);
        //explorerMissionHandler.exploreByAllMoves(unit);
        
//        Player aiNativePlayer = gameModel.game.players.getById("player:3");
//        aiNativePlayer = gameModel.game.players.getById("player:11");
        
//        if (missionsContainer == null) {
//            missionsContainer = new AIMissionsContainer();
//            
//        	staticSimulation();
//        	
//			//createStartGameMissions(unit.getOwner());
//        }
        
        Player aiNativePlayer = unit.getOwner();
        
        GameLogic gameLogic = new GameLogic(gameModel);
        AILogic aiLogic = new AILogic(gameModel.game, gameLogic, moveService);
        aiLogic.aiNewTurn(aiNativePlayer);
        
    	//executeMissions(unit);
    }

	private void staticSimulation() {
		Player player = gameModel.game.players.getById("player:1");
		PlayerMissionsContainer playerMissionContainer = gameModel.game.aiContainer.getMissionContainer(player);
        Unit unit = player.units.getById("unit:810");
        Unit unit2 = player.units.getById("unit:812");
        Unit potentialCarrier = player.units.getById("unit:811");
        
        potentialCarrier.clearDestination();
//        if (!potentialCarrier.getUnits().containsId(unit)) {
//        	unit.changeUnitLocation(potentialCarrier);
//        }
        Tile destTile = gameModel.game.map.getSafeTile(32, 40);

        RellocationMission rellocationMission = new RellocationMission(destTile, unit, potentialCarrier);
        playerMissionContainer.blockUnitsForMission(rellocationMission);

        RellocationMission rellocationMission2 = new RellocationMission(destTile, unit2, potentialCarrier);
        playerMissionContainer.blockUnitsForMission(rellocationMission2);
        
        FoundColonyMission foundColonyMission = new FoundColonyMission(destTile, unit);
        playerMissionContainer.blockUnitsForMission(foundColonyMission);
        foundColonyMission.addDependMission(rellocationMission);

        FoundColonyMission foundColonyMission2 = new FoundColonyMission(destTile, unit2);
        playerMissionContainer.blockUnitsForMission(foundColonyMission2);
        foundColonyMission2.addDependMission(rellocationMission2);
        
//		missionsContainer.addMission(foundColonyMission);
//        missionsContainer.addMission(foundColonyMission2);
        
        //mapActor.mapDrawModel().unitPath = pathToDestination;
        //String[][] debugPathRange = new String[gameModel.game.map.height][gameModel.game.map.width];
        //transportPath.toStringArrays(debugPathRange);
        //pathFinder.totalCostToStringArrays(debugPathRange);
        //rellocationMission.showDebugOnMap(debugPathRange);
		//mapActor.showTileDebugStrings(debugPathRange);
	}

}
