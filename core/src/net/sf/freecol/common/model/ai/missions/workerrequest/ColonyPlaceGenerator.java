package net.sf.freecol.common.model.ai.missions.workerrequest;

import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.ai.MapTileDebugInfo;
import net.sf.freecol.common.model.ai.missions.workerrequest.BuildColony.TileSelection;
import net.sf.freecol.common.model.player.Player;

import java.util.HashSet;
import java.util.Set;

class ColonyPlaceGenerator {

	public static final int TILES_NUMBER = 5;
	
	private final EntryPointTurnRange entryPointTurnRange;
	private final Map map;
	private final BuildColony buildColony;

    private int maxTurnsRange = TILES_NUMBER;
    private int[] theBestWeights = new int[maxTurnsRange+1];
    private Tile[] theBestTiles = new Tile[maxTurnsRange+1];
	
	public ColonyPlaceGenerator(EntryPointTurnRange entryPointTurnRange, Map map) {
	    this.entryPointTurnRange = entryPointTurnRange;
		this.map = map;
		
		buildColony = new BuildColony(map);
	}

	public void generateWeights(Player player) {
        Set<TileSelection> tileFilter = new HashSet<BuildColony.TileSelection>();
        tileFilter.add(TileSelection.ONLY_SEASIDE);
        buildColony.generateWeights(player, tileFilter);
    }

	public Tile[] theBestTiles(Player player) {
	    generateWeights(player);
		
        for (int i=0; i<maxTurnsRange; i++) {
            theBestWeights[i] = -1;
            theBestTiles[i] = null;
        }

        int tileWeight = 0;
        int turnCost;
        for (int cellIndex=0; cellIndex<buildColony.getTileWeights().size(); cellIndex++) {
            tileWeight = buildColony.getTileWeights().get(cellIndex);
            if (tileWeight <= 0) {
                continue;
            }
            turnCost = entryPointTurnRange.turnsCost(cellIndex);
            
            if (turnCost > maxTurnsRange) {
                turnCost = maxTurnsRange;
            }
            if (tileWeight > theBestWeights[turnCost]) {
                theBestWeights[turnCost] = tileWeight;
                theBestTiles[turnCost] = map.getSafeTile(
                    buildColony.getTileWeights().toX(cellIndex), 
                    buildColony.getTileWeights().toY(cellIndex)
                );
            }
        }
        return theBestTiles;
	}
	
    public Tile findTileToBuildColony(Player player) {
    	theBestTiles(player);
        
        for (Tile tile : theBestTiles) {
            if (tile != null) {
                return tile;
            }
        }
        return null;
        
    }

    public void tilesWeights(MapTileDebugInfo mapTileInfo) {
    	buildColony.toStringValues(mapTileInfo);
    }
    
    public void theBestTilesWeight(MapTileDebugInfo mapTileInfo) {
    	System.out.println("the best place for colony");
        for (int i=0; i<theBestTiles.length; i++) {
            System.out.println("i[" + i + "] w: " + theBestWeights[i] + " tile " + theBestTiles[i]);
            
            Tile t = theBestTiles[i];
            if (t == null) {
                continue;
            }
            mapTileInfo.str(t.x, t.y, "t" + i + " " + Integer.toString(buildColony.getTileWeights().get(t.x, t.y)));
        }
    }
}
