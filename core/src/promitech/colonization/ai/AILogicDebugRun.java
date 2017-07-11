package promitech.colonization.ai;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;

import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.map.path.Path;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.map.path.TransportPathFinder;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.Direction;
import promitech.colonization.GUIGameModel;
import promitech.colonization.MoveLogic;
import promitech.colonization.actors.map.MapActor;
import promitech.colonization.ai.BuildColony.TileBCWeight;
import promitech.colonization.ai.BuildColony.TileSelection;
import promitech.colonization.ai.ExplorerMissionHandler.ExploreStatus;
import promitech.colonization.gamelogic.BuildColonyOrder;
import promitech.colonization.gamelogic.BuildColonyOrder.OrderStatus;
import promitech.colonization.gamelogic.MoveContext;
import promitech.colonization.gamelogic.MoveType;

abstract class AbstractMission {
	boolean done = false;
	private List<AbstractMission> dependMissions = new ArrayList<AbstractMission>();
	
	public void addDependMission(AbstractMission m) {
		dependMissions.add(m);
	}

	public void setDone() {
		done = true;
	}

	public boolean isDone() {
		return done;
	}
	
	public boolean hasDependMissions() {
		if (dependMissions.isEmpty()) {
			return false;
		}
		
		boolean hasDepend = false;
		for (AbstractMission am : dependMissions) {
			if (am.done == false) {
				hasDepend = true;
				break;
			}
		}
		return hasDepend;
	}

}

class FoundColonyMission extends AbstractMission {
	Tile destTile;
	Unit unit;
	
	boolean isUnitInDestination() {
		Tile ut = unit.getTileLocationOrNull();
		return ut != null && destTile != null && ut.equalsCoordinates(destTile.x, destTile.y);
	}
	
	public String toString() {
		return "FoundColonyMission";
	}
}

class RellocationMission extends AbstractMission {
	Tile rellocationDestination;
	
	Unit unit;
	Tile unitDestination;
	
	Unit carrier;
	Tile carrierDestination;

	public boolean isUnitOnCarrier() {
		return carrier.getUnits().containsId(unit);
	}
	
	public boolean isUnitOnRellocationDestination() {
		Tile tile = unit.getTileLocationOrNull();
		return tile != null && tile.equalsCoordinates(rellocationDestination);
	}
	
	public boolean isUnitOnStepDestination() {
		Tile tile = unit.getTileLocationOrNull();
		return tile != null && tile.equalsCoordinates(unitDestination);
	}
	
	public boolean isCarrierOnStepDestination() {
		if (carrier == null || carrierDestination == null) {
			return false;
		}
		Tile tile = carrier.getTileLocationOrNull();
		return tile != null && tile.equalsCoordinates(carrierDestination);
	}
	
	public boolean needCarrierMove() {
		if (carrier == null || carrierDestination == null) {
			return false;
		}
		Tile carrierTile = carrier.getTileLocationOrNull();
		if (carrierTile == null) {
			// carrier can be in europe
			return false;
		}
		if (carrierTile.equalsCoordinates(carrierDestination)) {
			return false;
		}
		return carrier.hasMovesPoints();
	}
	
	public boolean needUnitMove() {
		Tile unitTile = unit.getTileLocationOrNull();
		if (unitTile == null) {
			// maybe in carrier
			return false;
		}
		if (unitTile.equalsCoordinates(unitDestination)) {
			return false;
		}
		return true;
	}
	
	public String toString() {
		return "RellocationMission";
	}

	public void initNextStepDestinationFromPath(Path path) {
		unitDestination = null;
		carrierDestination = null;
		
		boolean unitOnLand = unit.getTileLocationOrNull() != null;
		
    	Tile preview = path.tiles.get(0);
    	for (int i = 1; i < path.tiles.size; i++) {
    		Tile t = path.tiles.get(i);
    		if (preview.getType().isLand() && t.getType().isWater()) {
    			//System.out.println("  wait for ship");
    			if (unitOnLand) {
    				unitDestination = preview;
    				carrierDestination = t;
    				return;
    			}
    		}
    		if (preview.getType().isWater() && t.getType().isLand()) {
    			//System.out.println("  disembark");
    			if (!unitOnLand) {
    				unitDestination = t;
    				carrierDestination = preview;
    				return;
    			}
    		}
    		//System.out.println("t = " + t);
    		preview = t;
    	}
    	
    	// all path tiles are on the same land type
    	if (unitOnLand && unitDestination == null && carrierDestination == null) {
    		unitDestination = path.tiles.get(path.tiles.size-1);
    	} else {
    		carrierDestination = path.tiles.get(path.tiles.size-1);
    	}
	}

	public void showDebugOnMap(String[][] tilesStr) {
		tilesStr[rellocationDestination.y][rellocationDestination.x] = "DEST";
		if (unitDestination != null) {
			tilesStr[unitDestination.y][unitDestination.x] = "UNIT dest";
		}
		if (carrierDestination != null) {
			tilesStr[carrierDestination.y][carrierDestination.x] = "CARRIER dest";
		}
	}

	public boolean requireCarrierToHandleMission() {
		return carrierDestination != null;
	}
}

class TransportUnitMission extends AbstractMission {
	Unit carrier;
	Tile dest;
	final MapIdEntities<Unit> units = new MapIdEntities<Unit>();
	
	public String toString() {
		return "TransportUnitMission";
	}
}

class AIMissionsContainer {
	final List<AbstractMission> missions = new ArrayList<AbstractMission>();

	public void addMission(AbstractMission m) {
		missions.add(m);
	}

	public void clearDoneMissions() {
		List<AbstractMission> l = new ArrayList<AbstractMission>(missions);
		for (AbstractMission am : l) {
			if (am.isDone() && !am.hasDependMissions()) {
				missions.remove(am);
			}
		}
	}
}

public class AILogicDebugRun {

    private final PathFinder pathFinder = new PathFinder();
    private final ExplorerMissionHandler explorerMissionHandler;
    private final BuildColony buildColony;
    private GUIGameModel gameModel;
    private final MoveLogic moveLogic;
    private final MapActor mapActor;
    
    public static AIMissionsContainer missionsContainer;
    
    public AILogicDebugRun(GUIGameModel gameModel, MoveLogic moveLogic, MapActor mapActor) {
    	this.moveLogic = moveLogic;
        this.gameModel = gameModel;
        this.mapActor = mapActor;
        
        explorerMissionHandler = new ExplorerMissionHandler(gameModel.game, pathFinder, moveLogic);
        buildColony = new BuildColony(this.gameModel.game.map);
    }
    
    public void run() {
        Unit unit = gameModel.getActiveUnit();
//        ExplorerMission explorerMission = new ExplorerMission(unit);
//        explorerMissionHandler.executeMission(explorerMission);
//        explorerMissionHandler.exploreByOneMove(unit);
        //explorerMissionHandler.exploreByAllMoves(unit);
        
        if (missionsContainer == null) {
        	staticSimulation();
        	
			//createStartGameMissions(unit.getOwner());
        }
        
    	executeMissions(unit);
    }

	private void staticSimulation() {
		Player player = gameModel.game.players.getById("player:1");
        Unit unit = player.units.getById("unit:810");
        Unit potentialTransporter = player.units.getById("unit:811");
        
        if (!potentialTransporter.getUnits().containsId(unit)) {
        	unit.changeUnitLocation(potentialTransporter);
        }
        Tile destTile = gameModel.game.map.getSafeTile(32, 40);

        RellocationMission rellocationMission = new RellocationMission();
        rellocationMission.rellocationDestination = destTile;
        rellocationMission.unit = unit;

        Path pathToDestination = generatePath(rellocationMission, potentialTransporter);
        if (rellocationMission.requireCarrierToHandleMission()) {
        	rellocationMission.carrier = potentialTransporter;
        }
        
		missionsContainer = new AIMissionsContainer();
		missionsContainer.addMission(rellocationMission);
        
        mapActor.mapDrawModel().unitPath = pathToDestination;
        String[][] debugPathRange = new String[gameModel.game.map.height][gameModel.game.map.width];
        //transportPath.toStringArrays(debugPathRange);
        //pathFinder.totalCostToStringArrays(debugPathRange);
        
        rellocationMission.showDebugOnMap(debugPathRange);
        
		mapActor.showTileDebugStrings(debugPathRange);
	}

	/**
	 * {@link RellocationMission} should have already set unit and destination
	 * @param mission {@link RellocationMission}
	 * @param carrier {@link Unit}
	 */
	private Path generatePath(RellocationMission mission, Unit carrier) {
        Tile sourceTile = null;
        if (mission.unit.getTileLocationOrNull() == null) {
        	sourceTile = carrier.getTile();
        } else {
        	sourceTile = mission.unit.getTile();
        }
        
        pathFinder.generateRangeMap(
    		gameModel.game.map, 
    		carrier.getTile(), 
    		carrier, 
    		false
		);
        TransportPathFinder transportPath = new TransportPathFinder();
        Path pathToDestination = transportPath.findToTile(
    		gameModel.game.map, 
    		sourceTile, 
    		mission.rellocationDestination, 
    		mission.unit, 
    		carrier, 
    		pathFinder
		);
        
        // assumption start from land 
        if (pathToDestination.tiles.size <= 1) {
        	throw new IllegalStateException("can not find path do destination " + mission.rellocationDestination);
        }
        mission.initNextStepDestinationFromPath(pathToDestination);
        
        return pathToDestination;
	}
	
	private void executeMissions(Unit unit) {
        for (AbstractMission am : missionsContainer.missions) {
        	if (am.isDone() || am.hasDependMissions()) {
        		continue;
        	}
        	System.out.println("execute mission: " + am);
        	
        	if (am instanceof FoundColonyMission) {
        		foundColonyMissionHandler((FoundColonyMission)am);
        	}
//        	if (am instanceof TransportUnitMission) {
//        		transportUnitMissionHandler((TransportUnitMission)am);
//        	}
        	if (am instanceof RellocationMission) {
        		rellocationMissionHandler((RellocationMission)am);
        	}
        }
        missionsContainer.clearDoneMissions();
        
		mapActor.resetMapModel();
	}

	private void foundColonyMissionHandler(FoundColonyMission mission) {
    	if (!mission.isUnitInDestination()) {
    		// TODO: go to destination, exacly reallocation
    		return;
    	}
		BuildColonyOrder buildColonyOrder = new BuildColonyOrder(gameModel.game.map);
		OrderStatus check = buildColonyOrder.check(mission.unit, mission.destTile);
		if (check == OrderStatus.OK) {
    		String colonyName = Settlement.generateSettlmentName(mission.unit.getOwner());
    		Settlement.buildColony(gameModel.game.map, mission.unit, mission.destTile, colonyName);
    		
    		mission.setDone();
		} else {
			if (check == OrderStatus.NO_MOVE_POINTS) {
				// do nothing wait on next turn for move points
			} else {
				// TODO: move to global strategy manager
				Unit ship = null;
				for (Unit u : mission.unit.getOwner().units.entities()) {
					if (u.isCarrier()) {
						ship = u;
					}
				}
				Tile tileToBuildColony = findTileToBuildColony(ship);
				// TODO: find other tile to build colony
				// arg ship to transport goods,
				// arg source 
			}
		}
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
    	
    	Tile tileToBuildColony = findTileToBuildColony(ship);

    	for (Unit colonist : colonists.entities()) {
    		RellocationMission rellocationMission = new RellocationMission();
    		rellocationMission.unit = colonist;
    		rellocationMission.carrier = ship;
    		missionsContainer.addMission(rellocationMission);
    		
    		FoundColonyMission foundColonyMission = new FoundColonyMission();
    		foundColonyMission.destTile = tileToBuildColony;
    		foundColonyMission.unit = colonist;
    		foundColonyMission.addDependMission(rellocationMission);
    		missionsContainer.addMission(foundColonyMission);
    	}
    	
//		TransportUnitMission tm = new TransportUnitMission();
//		tm.carrier = ship;
//		tm.dest = tileToBuildColony;
//		tm.units.addAll(colonists);
//		missionsContainer.addMission(tm);
//    	
//    	for (Unit colonist : colonists.entities()) {
//			FoundColonyMission m = new FoundColonyMission();
//			m.destTile = findTileToBuildColony(ship);
//			m.unit = colonist;
//			missionsContainer.addMission(m);
//			m.addDependMission(tm);
//    	}
    }
	
    private void rellocationMissionHandler(RellocationMission mission) {
    	// unit and carrier can move in parallel. Sometimes it's necessary 
    	
    	if (mission.needCarrierMove()) {
    		Path path = pathFinder.findToTile(gameModel.game.map, mission.carrier.getTile(), mission.carrierDestination, mission.carrier);
    		MoveContext moveContext = new MoveContext(path);
    		moveLogic.forAiMoveViaPathOnlyReallocation(moveContext);
    	}

    	if (mission.isCarrierOnStepDestination()) {
    		if (mission.isUnitOnCarrier()) {
    			// disembark
    			if (mission.unit.hasMovesPoints()) {
    				MoveContext mc = new MoveContext(
						mission.carrier.getTile(), 
						mission.unitDestination, 
						mission.unit
					);
					moveLogic.forAiMoveOnlyReallocation(mc);
					
					if (!mission.isUnitOnRellocationDestination()) {
						generatePath(mission, mission.carrier);
						if (!mission.requireCarrierToHandleMission()) {
							mission.carrier = null;
							mission.carrierDestination = null;
						}
					}
    			}
    		} else {
    			// do nothing, wait for unit
    		}
    	}
    	
    	if (mission.needUnitMove()) {
    		Path path = pathFinder.findToTile(gameModel.game.map, mission.unit.getTile(), mission.unitDestination, mission.unit);
    		MoveContext moveContext = new MoveContext(path);
    		moveLogic.forAiMoveViaPathOnlyReallocation(moveContext);
    	}
    	if (mission.isUnitOnRellocationDestination()) {
    		mission.setDone();
    	} else {
    		if (mission.isUnitOnStepDestination()) {
    			if (mission.isCarrierOnStepDestination()) {
    				// embark
    				MoveContext moveContext = MoveContext.embarkUnit(mission.unit, mission.carrier);
    				moveLogic.forAiMoveViaPathOnlyReallocation(moveContext);
    				
    				generatePath(mission, mission.carrier);
    				if (!mission.requireCarrierToHandleMission()) {
    					mission.carrier = null;
    				}
    			} else {
    				// do nothing, wait for carrier
    			}
    		}
    	}
	}

    public void transportUnitMissionHandler(TransportUnitMission mission) {
    	Path path = pathFinder.findToTile(gameModel.game.map, mission.carrier.getTile(), mission.dest, mission.carrier);
    	
    	MoveContext moveContext = new MoveContext(path);
    	
    	moveLogic.forAiMoveViaPathOnlyReallocation(moveContext);
    	
		if (moveContext.isMoveType(MoveType.DISEMBARK)) {
			mission.setDone();
			for (Unit unitToDisembark : mission.units.entities()) {
				MoveContext mc = new MoveContext(
					mission.carrier.getTile(), 
					mission.dest, 
					unitToDisembark
				);
				moveLogic.forAiMoveOnlyReallocation(mc);
			}
		}
    }

	private Tile findTileToBuildColony(Unit unit) {
		pathFinder.generateRangeMap(gameModel.game.map, unit.getTile(), unit, false);
		
		
		int maxTurnsRange = 5;
		int[] theBestWeights = new int[maxTurnsRange+1];
		Tile[] theBestTiles = new Tile[maxTurnsRange+1];
		for (int i=0; i<maxTurnsRange; i++) {
			theBestWeights[i] = -1;
			theBestTiles[i] = null;
		}
		
		buildColony.init(unit.getOwner());
		Set<TileSelection> tileFilter = new HashSet<BuildColony.TileSelection>();
		tileFilter.add(TileSelection.ONLY_SEASIDE);
		buildColony.generateWeights(unit.getOwner(), tileFilter);
		
		
		int tileWeight = 0;
		int turnCost;
		for (int cellIndex=0; cellIndex<buildColony.getTileWeights().size(); cellIndex++) {
			tileWeight = buildColony.getTileWeights().get(cellIndex);
			if (tileWeight <= 0) {
				continue;
			}
			turnCost = pathFinder.turnsCost(cellIndex);
			
			if (turnCost > maxTurnsRange) {
				turnCost = maxTurnsRange;
			}
			if (tileWeight > theBestWeights[turnCost]) {
				theBestWeights[turnCost] = tileWeight;
				theBestTiles[turnCost] = gameModel.game.map.getSafeTile(
					buildColony.getTileWeights().toX(cellIndex), 
					buildColony.getTileWeights().toY(cellIndex)
				);
			}
		}
		
		String[][] debugPathRange = new String[gameModel.game.map.height][gameModel.game.map.width];
		for (int i=0; i<theBestTiles.length; i++) {
			System.out.println("the best place for colony");
			System.out.println("i[" + i + "] w: " + theBestWeights[i] + " tile " + theBestTiles[i]);
			
			Tile t = theBestTiles[i];
			if (t == null) {
				continue;
			}
			debugPathRange[t.y][t.x] = Integer.toString(buildColony.getTileWeights().get(t.x, t.y));
		}
		//pathFinder.turnCostToStringArrays(debugPathRange);
		mapActor.showTileDebugStrings(debugPathRange);
		
		for (Tile tile : theBestTiles) {
			if (tile != null) {
				return tile;
			}
		}
		return null;
		
	}
}
