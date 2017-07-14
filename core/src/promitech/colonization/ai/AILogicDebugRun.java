package promitech.colonization.ai;

import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.map.path.TransportPathFinder;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.GUIGameModel;
import promitech.colonization.MoveLogic;
import promitech.colonization.actors.map.MapActor;

public class AILogicDebugRun {

    private final PathFinder pathFinder = new PathFinder();
    private final TransportPathFinder transportPathFinder;
    private final ExplorerMissionHandler explorerMissionHandler;
    private GUIGameModel gameModel;
    private final MoveLogic moveLogic;
    private final MapActor mapActor;
    
    public static AIMissionsContainer missionsContainer;
    
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
        foundColonyMissionHandler = new FoundColonyMissionHandler(pathFinder, gameModel, tileDebugView);
        rellocationMissionHandler = new RellocationMissionHandler(pathFinder, transportPathFinder, gameModel, moveLogic, tileDebugView);
    }
    
    public void run() {
        Unit unit = gameModel.getActiveUnit();
//        ExplorerMission explorerMission = new ExplorerMission(unit);
//        explorerMissionHandler.executeMission(explorerMission);
//        explorerMissionHandler.exploreByOneMove(unit);
        //explorerMissionHandler.exploreByAllMoves(unit);
        
        if (missionsContainer == null) {
        	//staticSimulation();
        	
			createStartGameMissions(unit.getOwner());
        }
        
    	executeMissions(unit);
    }

	private void staticSimulation() {
		Player player = gameModel.game.players.getById("player:1");
        Unit unit = player.units.getById("unit:810");
        Unit potentialCarrier = player.units.getById("unit:811");
        
        if (!potentialCarrier.getUnits().containsId(unit)) {
        	unit.changeUnitLocation(potentialCarrier);
        }
        Tile destTile = gameModel.game.map.getSafeTile(32, 40);

        RellocationMission rellocationMission = new RellocationMission(destTile, unit, potentialCarrier);
        
        FoundColonyMission foundColonyMission = new FoundColonyMission(destTile, unit);
        foundColonyMission.addDependMission(rellocationMission);
        
		missionsContainer = new AIMissionsContainer();
		missionsContainer.addMission(rellocationMission);
		missionsContainer.addMission(foundColonyMission);
        
        //mapActor.mapDrawModel().unitPath = pathToDestination;
        //String[][] debugPathRange = new String[gameModel.game.map.height][gameModel.game.map.width];
        //transportPath.toStringArrays(debugPathRange);
        //pathFinder.totalCostToStringArrays(debugPathRange);
        //rellocationMission.showDebugOnMap(debugPathRange);
		//mapActor.showTileDebugStrings(debugPathRange);
	}
	
	private void executeMissions(Unit unit) {
	    
        for (AbstractMission am : missionsContainer.missions) {
        	if (am.isDone() || am.hasDependMissions()) {
        		continue;
        	}
        	System.out.println("execute mission: " + am);
        	
        	if (am instanceof FoundColonyMission) {
        	    FoundColonyMission foundColonyMission = (FoundColonyMission)am;
        	    foundColonyMission.toStringDebugTileTab(tileDebugView.getDebugTileStrTab());
                foundColonyMissionHandler.handle(foundColonyMission);
        	}
        	if (am instanceof RellocationMission) {
        	    RellocationMission rellocationMission = (RellocationMission)am;
        	    rellocationMission.toStringDebugTileTab(tileDebugView.getDebugTileStrTab());
                rellocationMissionHandler.handle(rellocationMission);
        	}
        }
        missionsContainer.clearDoneMissions();
        
		mapActor.resetMapModel();
	}

	private void createStartGameMissions(Player player) {
    	missionsContainer = new AIMissionsContainer();
    	
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
    	
    	Tile tileToBuildColony = foundColonyMissionHandler.findTileToBuildColony(ship);

    	for (Unit colonist : colonists.entities()) {
    		RellocationMission rellocationMission = new RellocationMission(tileToBuildColony, colonist, ship);
    		missionsContainer.addMission(rellocationMission);
    		
    		FoundColonyMission foundColonyMission = new FoundColonyMission(tileToBuildColony, colonist);
    		foundColonyMission.addDependMission(rellocationMission);
    		missionsContainer.addMission(foundColonyMission);
    	}
    }

}
