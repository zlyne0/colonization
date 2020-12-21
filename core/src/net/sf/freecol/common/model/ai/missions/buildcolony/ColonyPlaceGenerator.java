package net.sf.freecol.common.model.ai.missions.buildcolony;

import java.util.HashSet;
import java.util.Set;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.ai.MapTileDebugInfo;
import net.sf.freecol.common.model.ai.missions.buildcolony.BuildColony.TileSelection;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.player.Player;

public class ColonyPlaceGenerator {

	private final PathFinder pathFinder;
	private final Game game;
	private final BuildColony buildColony;

    private int maxTurnsRange = 5;
    private int[] theBestWeights = new int[maxTurnsRange+1];
    private Tile[] theBestTiles = new Tile[maxTurnsRange+1];
	
	public ColonyPlaceGenerator(PathFinder pathFinder, Game game) {
		this.pathFinder = pathFinder;
		this.game = game;
		
		buildColony = new BuildColony(game.map);
	}

	public void generateWeights(Player player) {
        Set<TileSelection> tileFilter = new HashSet<BuildColony.TileSelection>();
        tileFilter.add(TileSelection.ONLY_SEASIDE);
        buildColony.generateWeights(player, tileFilter);
    }

	public Tile[] theBestTiles(Unit unit, Tile rangeSourceTile) {
        pathFinder.generateRangeMap(game.map, rangeSourceTile, unit, PathFinder.includeUnexploredTiles);

	    generateWeights(unit.getOwner());
		
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
        return theBestTiles;
	}
	
    public Tile findTileToBuildColony(Unit unit, Tile rangeSourceTile) {
    	theBestTiles(unit, rangeSourceTile);
        
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
