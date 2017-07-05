package promitech.colonization.ai;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;

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
        //Unit unit = gameModel.getActiveUnit();
//        ExplorerMission explorerMission = new ExplorerMission(unit);
//        explorerMissionHandler.executeMission(explorerMission);
//        explorerMissionHandler.exploreByOneMove(unit);
        //explorerMissionHandler.exploreByAllMoves(unit);
        
        Player player = gameModel.game.players.getById("player:1");
        Unit unit = player.units.getById("unit:810");
        Unit potentialTransporter = player.units.getById("unit:811");
        
        Tile sourceTile = unit.getTile();
        
		Tile destTile = gameModel.game.map.getSafeTile(32, 40);
        
        //ai(unit);
        
        pathFinder.generateRangeMap(gameModel.game.map, potentialTransporter.getTile(), potentialTransporter, false);
        
        TransportPathFinder transportPath = new TransportPathFinder();
        Path findToTile = transportPath.findToTile(gameModel.game.map, sourceTile, destTile, unit, potentialTransporter, pathFinder);
        
        if (findToTile.tiles.size > 1) {
        	Tile preview = findToTile.tiles.get(0);
        	for (int i = 1; i < findToTile.tiles.size; i++) {
        		Tile t = findToTile.tiles.get(i);
        		if (preview.getType().isLand() && t.getType().isWater()) {
        			System.out.println("  wait for ship");
        		}
        		if (preview.getType().isWater() && t.getType().isLand()) {
        			System.out.println("  disembark");
        		}
        		System.out.println("t = " + t);
        		preview = t;
        	}
        }
        
        mapActor.mapDrawModel().unitPath = findToTile;

        String[][] debugPathRange = new String[gameModel.game.map.height][gameModel.game.map.width];
        transportPath.toStringArrays(debugPathRange);
        //pathFinder.totalCostToStringArrays(debugPathRange);
        
		mapActor.showTileDebugStrings(debugPathRange);
        
    }

	private void ai(Unit unit) {
		Player player = unit.getOwner();
        
        if (missionsContainer == null) {
			createStartGameMissions(player);
        }
        
        for (AbstractMission am : missionsContainer.missions) {
        	if (am.isDone() || am.hasDependMissions()) {
        		continue;
        	}
        	System.out.println("execute mission: " + am);
        	
        	if (am instanceof FoundColonyMission) {
        		foundColonyMissionHandler((FoundColonyMission)am);
        	}
        	if (am instanceof TransportUnitMission) {
        		transportUnitMissionHandler((TransportUnitMission)am);
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
    		if (unit.isCarrier()) {
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

		TransportUnitMission tm = new TransportUnitMission();
		tm.carrier = ship;
		tm.dest = tileToBuildColony;
		tm.units.addAll(colonists);
		missionsContainer.addMission(tm);
    	
    	for (Unit colonist : colonists.entities()) {
			FoundColonyMission m = new FoundColonyMission();
			m.destTile = findTileToBuildColony(ship);
			m.unit = colonist;
			missionsContainer.addMission(m);
			m.addDependMission(tm);
    	}
    }
    
    public void transportUnitMissionHandler(TransportUnitMission m) {
    	Path path = pathFinder.findToTile(gameModel.game.map, m.carrier.getTile(), m.dest, m.carrier);
    	
    	MoveContext moveContext = new MoveContext(path);
    	
    	moveLogic.forAiMoveViaPathOnlyReallocation(moveContext);
    	
		if (moveContext.isMoveType(MoveType.DISEMBARK)) {
			m.setDone();
			for (Unit unitToDisembark : m.units.entities()) {
				MoveContext mc = new MoveContext(
					m.carrier.getTile(), 
					m.dest, 
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
