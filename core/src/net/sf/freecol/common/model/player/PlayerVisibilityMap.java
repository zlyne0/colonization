package net.sf.freecol.common.model.player;

import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.Tile;
import promitech.colonization.SpiralIterator;
import promitech.map.Byte2dArray;

public class PlayerVisibilityMap {

	private static final byte UNEXPLORED = 0;
	private static final byte ACTUAL_SEEN = 1;
	private static final byte FOG_OF_WAR = 2;
	
	private final Byte2dArray array;
	private final SpiralIterator spiralIterator;
	
	public PlayerVisibilityMap(Map map) {
		array = new Byte2dArray(map.width, map.height);
		array.set(UNEXPLORED);
		spiralIterator = new SpiralIterator(map.width, map.height);
	}
	
    public boolean hasFogOfWar(Tile tile) {
    	return array.get(tile.x, tile.y) == FOG_OF_WAR; 
    }
    
    public boolean hasFogOfWar(int x, int y) {
    	return array.get(x, y) == FOG_OF_WAR; 
    }
	
    public void removeFogOfWar() {
    	array.set(ACTUAL_SEEN);
    }
	
    public void putFogOfWar() {
    	for (int i=0; i<array.cellLength(); i++) {
    		if (array.get(i) != UNEXPLORED) {
    			array.set(i, FOG_OF_WAR);
    		}
    	}
    }
    
    private boolean setAsExplored(int x, int y) {
    	int cellIndex = array.toIndex(x, y);
    	boolean explored = false;
    	if (array.get(cellIndex) == UNEXPLORED) {
    		explored = true;
    	}
    	array.set(cellIndex, ACTUAL_SEEN);
    	return explored;
    }
    
	public boolean isUnExplored(Tile tile) {
		return array.get(tile.x, tile.y) == UNEXPLORED;
	}
	
	public boolean isExplored(int x, int y) {
		return array.get(x, y) != UNEXPLORED;
	}
    
    public boolean revealMap(int x, int y, int radius) {
    	boolean revealed = false;
    	
    	if (setAsExplored(x, y)) {
    		revealed = true;
    	}
    	
    	spiralIterator.reset(x, y, true, radius);
    	while (spiralIterator.hasNext()) {
    		if (setAsExplored(spiralIterator.getX(), spiralIterator.getY())) {
    			revealed = true;
    		}
    		spiralIterator.next();
    	}
    	return revealed;
    }
}
