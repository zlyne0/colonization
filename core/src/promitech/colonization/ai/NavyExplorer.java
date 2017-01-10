package promitech.colonization.ai;

import com.badlogic.gdx.utils.IntArray;

import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.map.Path;
import net.sf.freecol.common.model.map.PathFinder;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.Direction;
import promitech.colonization.Randomizer;
import promitech.map.Int2dArray;

public class NavyExplorer {
    private static final int UNSETED_BORDER_VALUE = -1;

    private static int SRC = 0;
    private static int DEST = Integer.MAX_VALUE;
    
    // exactly reverseInfluenceMap
    private final Int2dArray highseaInfluenceMap; 
    private final IntArray poolIndexes;
    
    private IntArray exploredBordersIndexes = new IntArray();
    private final Int2dArray exploredBorders;
    
    private final Map map;
    private Player player;
    private PathFinder pathFinder;
    
    private int theBestX = 0;
    private int theBestY = 0;
    private int theBestCost = -1;
    
    public NavyExplorer(final Map map) {
        this.map = map;
        highseaInfluenceMap = new Int2dArray(map.width, map.height);
        poolIndexes = new IntArray(false, map.width * map.height);
        exploredBorders = new Int2dArray(map.width, map.height);
        reset();
    }
    
    public void reset() {
        exploredBorders.set(UNSETED_BORDER_VALUE);
        exploredBordersIndexes.clear();
        highseaInfluenceMap.set(DEST);
        poolIndexes.clear();
        
        theBestX = 0;
        theBestY = 0;
        theBestCost = -1;
    }
    
    public void highseaAddSource(int x, int y) {
        int cellIndex = highseaInfluenceMap.toIndex(x, y);
        highseaInfluenceMap.set(cellIndex, SRC);
        poolIndexes.add(cellIndex);
    }
    
    void generateHighseaInfluenceMap() {
        int cellIndex;
        int x, y, v, i;
        int nv;
        int dx, dy, dv;
        
        while (poolIndexes.size != 0) {
            cellIndex = poolIndexes.removeIndex(0);
            x = highseaInfluenceMap.toX(cellIndex);
            y = highseaInfluenceMap.toY(cellIndex);
            v = highseaInfluenceMap.get(x, y);
            
            nv = v + 1;
            for (i=0; i<Direction.values().length; i++) {
                Direction d = Direction.values()[i];
                
                dx = d.stepX(x, y);
                dy = d.stepY(x, y);
                if (!highseaInfluenceMap.isIndexValid(dx, dy)) {
                    continue;
                }
                if (isLand(dx, dy)) {
                    continue;
                }
                dv = highseaInfluenceMap.get(dx, dy);
                
                if (dv != SRC && dv > nv) {
                    if (isTileUnexplored(dx, dy)) {
                        collectBorderInfluenceMapTile(x, y, v);
                    } else {
                        highseaInfluenceMap.set(dx, dy, nv);
                        poolIndexes.add(highseaInfluenceMap.toIndex(dx, dy));
                    }
                }
            }
        }
    }
    
    private void collectBorderInfluenceMapTile(int x, int y, int v) {
        if (v == 0) {
            return;
        }
        int cellIndex = exploredBorders.toIndex(x, y);
        if (exploredBorders.get(cellIndex) == UNSETED_BORDER_VALUE) {
            exploredBordersIndexes.add(cellIndex);
        }
        exploredBorders.set(cellIndex, v);
    }

    private boolean isLand(int x, int y) {
        return map.getSafeTile(x, y).getType().isLand();
    }
    
    private boolean isTileUnexplored(int x, int y) {
        return !player.isTileExplored(x, y);
    }
    
    public void generateExploreDestination(PathFinder pathFinder, Player player) {
        for (int y=0; y<map.height; y++) {
            for (int x=0; x<map.width; x++) {
                Tile t = map.getSafeTile(x, y);
                if (player.isTileExplored(x, y)) {
                    if (t.getType().isHighSea()) {
                        highseaAddSource(t.x, t.y);
                    }
                }
            }
        }
        
        this.player = player;
        this.pathFinder = pathFinder;
        generateHighseaInfluenceMap();
        determineTheBestTileToExplore();
    }
    
    private void determineTheBestTileToExplore() {
        int x, y, cost, index;
        
        for (int i=0; i<exploredBordersIndexes.size; i++) {
            index = exploredBordersIndexes.get(i);
            x = exploredBorders.toX(index);
            y = exploredBorders.toY(index);
            cost = explorationCost(x, y);
            if (cost >= theBestCost) {
                if (cost > theBestCost) {
                    theBestCost = cost;
                    theBestX = x;
                    theBestY = y;
                } else {
                    // the same cost
                    if (Randomizer.instance().isHappen(50)) {
                        theBestCost = cost;
                        theBestX = x;
                        theBestY = y;
                    }
                }
            }
        }
    }
    
    public int explorationCost(int x, int y) {
        return (100 - pathFinder.turnsCost(x, y)) * 1000 
                - pathFinder.totalCost(x, y) 
                + highseaInfluenceMap.get(x, y) 
                + numberOfLandNeighbour(x, y);
    }
    
    private int numberOfLandNeighbour(int x, int y) {
        int sum = 0;
        for (int i=0; i<Direction.values().length; i++) {
            Direction d = Direction.values()[i];
            Tile tile = map.getTile(x, y, d);
            if (tile != null && tile.getType().isLand()) {
                sum++;
            }
        }
        return sum;
    }
    
    public boolean isFoundExploreDestination() {
        return theBestCost != -1;
    }
    
    public boolean isExploreDestinationInOneTurn() {
        return pathFinder.turnsCost(theBestX, theBestY) == 0;
    }
    
    public Direction getExploreDestinationAsDirection() {
        return pathFinder.getDirectionInto(theBestX, theBestY);
    }
    
    public Path getExploreDestinationAsPath() {
        return pathFinder.getPathInto(theBestX, theBestY);
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int x, y, v, cost;
        for (int i=0; i<exploredBordersIndexes.size; i++) {
            x = exploredBorders.toX(exploredBordersIndexes.get(i));
            y = exploredBorders.toY(exploredBordersIndexes.get(i));
            v = exploredBorders.get(x, y);
            cost = explorationCost(x, y);
            
            sb.append("xy [" + x + "," + y + "] " + v +
                    ", turns\t" + pathFinder.turnsCost(x, y) +
                    ", totalC\t" + pathFinder.totalCost(x, y) +
                    ", v\t" + v +
                    ", land\t" + numberOfLandNeighbour(x, y) +
                    " = cost " + cost);
            sb.append("\n");
        }
        return sb.toString();
    }

    public void toStringsHighseaValues(String strings[][]) {
        int v;
        for (int y=0; y<highseaInfluenceMap.height; y++) {
            for (int x=0; x<highseaInfluenceMap.width; x++) {
                v = highseaInfluenceMap.get(x, y);
                if (v != DEST) {
                    strings[y][x] = Integer.toString(v);
                }
            }
        }
    }
    
    public void toStringsBorderValues(String strings[][]) {
        int x, y, cost;
        for (int i=0; i<exploredBordersIndexes.size; i++) {
            x = exploredBorders.toX(exploredBordersIndexes.get(i));
            y = exploredBorders.toY(exploredBordersIndexes.get(i));
            cost = explorationCost(x, y);
            
            strings[y][x] = Integer.toString(cost);
        }
    }
    
}
