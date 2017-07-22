package promitech.colonization.ai;

import java.util.HashSet;
import java.util.LinkedList;

import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.map.path.TransportPathFinder;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.GUIGameModel;
import promitech.colonization.GameLogic;
import promitech.colonization.MoveLogic;
import promitech.colonization.actors.map.MapActor;

public class AILogicDebugRun {

    private final PathFinder pathFinder = new PathFinder();
    private final TransportPathFinder transportPathFinder;
    private final ExplorerMissionHandler explorerMissionHandler;
    private GUIGameModel gameModel;
    private final MoveLogic moveLogic;
    private final MapActor mapActor;
    
    private final TileDebugView tileDebugView;
    private final FoundColonyMissionHandler foundColonyMissionHandler;
    private final RellocationMissionHandler rellocationMissionHandler;
    
    public AILogicDebugRun(GUIGameModel gameModel, MoveLogic moveLogic, MapActor mapActor) {
    	this.moveLogic = moveLogic;
        this.gameModel = gameModel;
        this.mapActor = mapActor;
        
        transportPathFinder = new TransportPathFinder(gameModel.game.map);
        explorerMissionHandler = new ExplorerMissionHandler(gameModel.game, pathFinder, moveLogic);

        tileDebugView = new TileDebugView(mapActor, gameModel);
        foundColonyMissionHandler = new FoundColonyMissionHandler(pathFinder, gameModel.game);
        rellocationMissionHandler = new RellocationMissionHandler(pathFinder, transportPathFinder, gameModel.game, moveLogic);
    }
    
    public void run() {
        Unit unit = gameModel.getActiveUnit();
//        ExplorerMission explorerMission = new ExplorerMission(unit);
//        explorerMissionHandler.executeMission(explorerMission);
//        explorerMissionHandler.exploreByOneMove(unit);
        //explorerMissionHandler.exploreByAllMoves(unit);
        
        Player aiNativePlayer = gameModel.game.players.getById("player:3");
        aiNativePlayer = gameModel.game.players.getById("player:11");
        
//        if (missionsContainer == null) {
//            missionsContainer = new AIMissionsContainer();
//            
//        	staticSimulation();
//        	
//			//createStartGameMissions(unit.getOwner());
//        }
        
        GameLogic gameLogic = new GameLogic(gameModel);
        AILogic aiLogic = new AILogic(gameModel.game, gameLogic, moveLogic);
        aiLogic.aiNewTurn(aiNativePlayer);
        
    	//executeMissions(unit);
    }

	private void staticSimulation() {
		Player player = gameModel.game.players.getById("player:1");
        Unit unit = player.units.getById("unit:810");
        Unit unit2 = player.units.getById("unit:812");
        Unit potentialCarrier = player.units.getById("unit:811");
        
        potentialCarrier.clearDestination();
//        if (!potentialCarrier.getUnits().containsId(unit)) {
//        	unit.changeUnitLocation(potentialCarrier);
//        }
        Tile destTile = gameModel.game.map.getSafeTile(32, 40);

        RellocationMission rellocationMission = new RellocationMission(destTile, unit, potentialCarrier);
        GlobalStrategyPlaner.blockUnitsForMission(rellocationMission);

        RellocationMission rellocationMission2 = new RellocationMission(destTile, unit2, potentialCarrier);
        GlobalStrategyPlaner.blockUnitsForMission(rellocationMission2);
        
        FoundColonyMission foundColonyMission = new FoundColonyMission(destTile, unit);
        GlobalStrategyPlaner.blockUnitsForMission(foundColonyMission);
        foundColonyMission.addDependMission(rellocationMission);

        FoundColonyMission foundColonyMission2 = new FoundColonyMission(destTile, unit2);
        GlobalStrategyPlaner.blockUnitsForMission(foundColonyMission2);
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
	
	private void createStartGameMissions(Player player) {
    	Unit ship = null;
    	MapIdEntities<Unit> colonists = new MapIdEntities<Unit>();
    	for (Unit unit : player.units.entities()) {
    		if (unit.isColonist()) {
    			colonists.add(unit);
    		}
    		if (unit.isNaval() && unit.isCarrier()) {
    			ship = unit;
    		}
    	}
    	if (ship == null) {
    		throw new IllegalStateException("can not find ship to generate start game ai missions");
    	}
    	if (colonists.isEmpty()) {
    		throw new IllegalStateException("can not find colonists to build colony");
    	}
    	
    	Tile tileToBuildColony = foundColonyMissionHandler.findTileToBuildColony(ship.getOwner(), ship, ship.getTile());

    	for (Unit colonist : colonists.entities()) {
    		RellocationMission rellocationMission = new RellocationMission(tileToBuildColony, colonist, ship);
            GlobalStrategyPlaner.blockUnitsForMission(rellocationMission);
            
    		FoundColonyMission foundColonyMission = new FoundColonyMission(tileToBuildColony, colonist);
    		foundColonyMission.addDependMission(rellocationMission);
    		GlobalStrategyPlaner.blockUnitsForMission(foundColonyMission);
    		
    		//missionsContainer.addMission(foundColonyMission);
    	}
    }

}
