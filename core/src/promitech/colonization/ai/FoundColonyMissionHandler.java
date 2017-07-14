package promitech.colonization.ai;

import java.util.HashSet;
import java.util.Set;

import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.GUIGameModel;
import promitech.colonization.ai.BuildColony.TileSelection;
import promitech.colonization.gamelogic.BuildColonyOrder;
import promitech.colonization.gamelogic.BuildColonyOrder.OrderStatus;

class FoundColonyMissionHandler implements MissionHandler<FoundColonyMission> {
    
    private final PathFinder pathFinder;
    private final GUIGameModel gameModel;
    private final BuildColony buildColony;
    private final TileDebugView tileDebugView;
    
    public FoundColonyMissionHandler(PathFinder pathFinder, GUIGameModel gameModel, TileDebugView tileDebugView) {
        this.pathFinder = pathFinder;
        this.gameModel = gameModel;
        this.tileDebugView = tileDebugView;
        this.buildColony = new BuildColony(this.gameModel.game.map);
    }
    
    @Override
    public void handle(FoundColonyMission mission) {
        if (!mission.isUnitInDestination()) {
            // do nothing, wait when unit will be on destination
            return;
        }
        BuildColonyOrder buildColonyOrder = new BuildColonyOrder(gameModel.game.map);
        OrderStatus check = buildColonyOrder.check(mission.unit, mission.destTile);
        if (check == OrderStatus.OK) {
            String colonyName = Settlement.generateSettlmentName(mission.unit.getOwner());
            Settlement.buildColony(gameModel.game.map, mission.unit, mission.destTile, colonyName);
            
            GlobalStrategyPlaner.unblockUnitsFromMission(mission);
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
                    GlobalStrategyPlaner.unblockUnitsFromMission(mission);
                }
            }
        }
    }
 
    public Tile findTileToBuildColony(Player player, Unit unit, Tile rangeFromTile) {
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
        
        debugShowBestTileWeights(theBestTiles, theBestWeights);
        
        for (Tile tile : theBestTiles) {
            if (tile != null) {
                return tile;
            }
        }
        return null;
        
    }
    
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
    
}