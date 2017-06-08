package promitech.colonization.ai;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;

import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.map.PathFinder;
import promitech.colonization.Direction;
import promitech.colonization.GUIGameModel;
import promitech.colonization.MoveLogic;
import promitech.colonization.actors.map.MapActor;
import promitech.colonization.ai.BuildColony.TileBCWeight;
import promitech.colonization.ai.BuildColony.TileSelection;
import promitech.colonization.ai.ExplorerMissionHandler.ExploreStatus;
import promitech.colonization.gamelogic.MoveContext;

class FoundColonyMission {
	
}

public class AILogicDebugRun {

    private final PathFinder pathFinder = new PathFinder();
    private final ExplorerMissionHandler explorerMissionHandler;
    private final BuildColony buildColony;
    private GUIGameModel gameModel;
    private final MoveLogic moveLogic;
    private final MapActor mapActor;
    
    public static FoundColonyMission foundColonyMission = null;
    
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
        
        //if (foundColonyMission == null) 
        {
        	foundColonyMission = new FoundColonyMission();
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
				
				if (turnCost >= maxTurnsRange) {
					turnCost = maxTurnsRange;
				}
				if (turnCost <= 3) {
					System.out.println("turnCost = " + turnCost);
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
				debugPathRange[t.y][t.x] = Integer.toString(buildColony.getTileWeights().get(t.x, t.y));
			}
			//pathFinder.turnCostToStringArrays(debugPathRange);
			mapActor.showTileDebugStrings(debugPathRange);
        }
    }
}
