package net.sf.freecol.common.model.ai.missions.workerrequest;

import com.badlogic.gdx.utils.IntArray;

import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.Tile;
import promitech.colonization.Direction;
import promitech.map.Int2dArray;

class HighSeaDistanceGenerator {
	
	private static final int SRC_VAL = 0;
	public static final int LACK_CONNECTION = Integer.MAX_VALUE;
	
    private final Int2dArray highseaInfluenceMap; 
    private final IntArray poolIndexes;
	private final Map map;
	
    public HighSeaDistanceGenerator(Map map) {
    	this.map = map;
    	
        highseaInfluenceMap = new Int2dArray(map.width, map.height);
        poolIndexes = new IntArray(false, map.width * map.height);
    }
    
    /**
     * Generate influence map from group of highsea tiles to group of land tiles. 
     */
    public void generate() {
    	highseaInfluenceMap.set(LACK_CONNECTION);
        for (int y=0; y<map.height; y++) {
            for (int x=0; x<map.width; x++) {
                Tile t = map.getSafeTile(x, y);
                if (t.getType().isHighSea()) {
                	highseaAddSource(x, y);
                }
            }
        }
        generateInfluenceMap();
    }
    
    private void highseaAddSource(int x, int y) {
        int cellIndex = highseaInfluenceMap.toIndex(x, y);
        highseaInfluenceMap.set(cellIndex, SRC_VAL);
        poolIndexes.add(cellIndex);
    }
    
    private void generateInfluenceMap() {
        int cellIndex;
        int x, y, v, i;
        int nv;
        int dx, dy, dv, di;
        
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
                di = highseaInfluenceMap.toIndex(dx, dy);
                dv = highseaInfluenceMap.get(di);
                if (isLand(dx, dy)) {
                	if (dv != SRC_VAL && dv > nv) {
                		highseaInfluenceMap.set(di, nv);
                	}
                    continue;
                }
                
                if (dv != SRC_VAL && dv > nv) {
                    highseaInfluenceMap.set(di, nv);
                    poolIndexes.add(di);
                }
            }
        }
    }

    private boolean isLand(int x, int y) {
        return map.getSafeTile(x, y).getType().isLand();
    }
	
    public void populate(Int2dArray destTab) {
    	destTab.set(highseaInfluenceMap);
    }

	public int getDistance(Tile tile) {
		return highseaInfluenceMap.get(tile.x, tile.y);
	}
}