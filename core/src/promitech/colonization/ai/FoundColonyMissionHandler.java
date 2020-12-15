package promitech.colonization.ai;

import java.util.HashSet;
import java.util.Set;

import net.sf.freecol.common.model.ColonyFactory;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.SettlementFactory;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.ai.missions.FoundColonyMission;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;
import net.sf.freecol.common.model.ai.missions.RellocationMission;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.ai.BuildColony.TileSelection;
import promitech.colonization.orders.BuildColonyOrder;
import promitech.colonization.orders.BuildColonyOrder.OrderStatus;

class FoundColonyMissionHandler implements MissionHandler<FoundColonyMission> {
    
    private final PathFinder pathFinder;
    private final Game game;
    private final BuildColony buildColony;
    
    public FoundColonyMissionHandler(PathFinder pathFinder, Game game) {
        this.pathFinder = pathFinder;
        this.game = game;
        this.buildColony = new BuildColony(this.game.map);
    }
    
    @Override
    public void handle(PlayerMissionsContainer playerMissionsContainer, FoundColonyMission mission) {
    	if (mission.unit == null || mission.unit.isDisposed()) {
    		mission.setDone();
    		return;
    	}
        if (!mission.isUnitInDestination()) {
            // do nothing, wait when unit will be on destination
            return;
        }
        BuildColonyOrder buildColonyOrder = new BuildColonyOrder(game.map);
        OrderStatus check = buildColonyOrder.check(mission.unit, mission.destTile);
        if (check == OrderStatus.OK) {
            ColonyFactory colonyFactory = new ColonyFactory(game, pathFinder);
            colonyFactory.buildColonyByAI(mission.unit, mission.destTile);
            
            playerMissionsContainer.unblockUnitsFromMission(mission);
            mission.setDone();
        } else {
            if (check == OrderStatus.NO_MOVE_POINTS) {
                // do nothing wait on next turn for move points
            } else {
                Tile tileToBuildColony = findTileToBuildColony(mission.unit.getOwner(), mission.unit, mission.unit.getTile());
                if (tileToBuildColony != null) {
                    mission.destTile = tileToBuildColony;
                    RellocationMission rellocationMission = new RellocationMission(tileToBuildColony, mission.unit);
                    mission.addDependMission(rellocationMission);
                } else {
                    // can not find tile to build colony, do nothing
                	playerMissionsContainer.unblockUnitsFromMission(mission);
                }
            }
        }
    }
 
    public Tile findTileToBuildColony(Player player, Unit unit, Tile rangeFromTile) {
        pathFinder.generateRangeMap(game.map, unit.getTile(), unit, PathFinder.includeUnexploredTiles);
        
        int maxTurnsRange = 5;
        int[] theBestWeights = new int[maxTurnsRange+1];
        Tile[] theBestTiles = new Tile[maxTurnsRange+1];
        for (int i=0; i<maxTurnsRange; i++) {
            theBestWeights[i] = -1;
            theBestTiles[i] = null;
        }
        
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
                theBestTiles[turnCost] = game.map.getSafeTile(
                    buildColony.getTileWeights().toX(cellIndex), 
                    buildColony.getTileWeights().toY(cellIndex)
                );
            }
        }
        
        //debugShowBestTileWeights(theBestTiles, theBestWeights);
        
        for (Tile tile : theBestTiles) {
            if (tile != null) {
                return tile;
            }
        }
        return null;
        
    }
    
    /*
    private void debugShowBestTileWeights(Tile[] theBestTiles, int[] theBestWeights) {
        if (!tileDebugView.isDebug()) {
            return;
        }
        
        for (int i=0; i<theBestTiles.length; i++) {
            System.out.println("the best place for colony");
            System.out.println("i[" + i + "] w: " + theBestWeights[i] + " tile " + theBestTiles[i]);
            
            Tile t = theBestTiles[i];
            if (t == null) {
                continue;
            }
            tileDebugView.debug(t.x, t.y, "t" + i + " " + Integer.toString(buildColony.getTileWeights().get(t.x, t.y)));
        }
    }
    */
}