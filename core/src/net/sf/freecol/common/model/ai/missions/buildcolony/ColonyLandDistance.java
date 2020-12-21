package net.sf.freecol.common.model.ai.missions.buildcolony;

import com.badlogic.gdx.utils.IntArray;

import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.Direction;
import promitech.map.Int2dArray;

class ColonyLandDistance {
	private static final int SRC_VAL = 0;
	private static final int DEST_VAL = Integer.MAX_VALUE;
	
    private final Int2dArray influenceMap; 
    private final IntArray poolIndexes;
	private final Map map;
	
    public ColonyLandDistance(Map map) {
    	this.map = map;
    	
        influenceMap = new Int2dArray(map.width, map.height);
        poolIndexes = new IntArray(false, map.width * map.height);
    }

    public void generate(Player player) {
    	influenceMap.set(DEST_VAL);

    	for (Settlement colony : player.settlements.entities()) {
    		addColonySource(colony.tile.x, colony.tile.y);
		}
    	
        generateInfluenceMap();
    }
    
    private void addColonySource(int x, int y) {
        int cellIndex = influenceMap.toIndex(x, y);
        influenceMap.set(cellIndex, SRC_VAL);
        poolIndexes.add(cellIndex);
    }
    
    private void generateInfluenceMap() {
        int cellIndex;
        int x, y, v, i;
        int nv;
        int dx, dy, dv, di;
        
        while (poolIndexes.size != 0) {
            cellIndex = poolIndexes.removeIndex(0);
            x = influenceMap.toX(cellIndex);
            y = influenceMap.toY(cellIndex);
            v = influenceMap.get(x, y);
            
            nv = v + 1;
            for (i=0; i<Direction.values().length; i++) {
                Direction d = Direction.values()[i];
                
                dx = d.stepX(x, y);
                dy = d.stepY(x, y);
                if (!influenceMap.isIndexValid(dx, dy)) {
                    continue;
                }
                di = influenceMap.toIndex(dx, dy);
                dv = influenceMap.get(di);
                if (isLand(dx, dy)) {
                	if (dv != SRC_VAL && dv > nv) {
                		influenceMap.set(di, nv);
                		poolIndexes.add(di);
                	}
                }
            }
        }
	}

	private boolean isLand(int x, int y) {
        return map.getSafeTile(x, y).getType().isLand();
    }
	
    public void populate(Int2dArray destTab) {
    	destTab.set(influenceMap);
    }

	public int getDistance(Tile tile) {
		return influenceMap.get(tile.x, tile.y);
	}
    
}