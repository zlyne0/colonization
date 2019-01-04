package net.sf.freecol.common.model.player;

import com.badlogic.gdx.utils.IntArray;

public class MoveExploredTiles {

    public final IntArray removeFogOfWarX = new IntArray(5 * 5);
    public final IntArray removeFogOfWarY = new IntArray(5 * 5);
    public final IntArray exploredTilesX = new IntArray(5 * 5);
    public final IntArray exploredTilesY = new IntArray(5 * 5);
    
    public void clear() {
    	removeFogOfWarX.clear();
    	removeFogOfWarY.clear();
    	exploredTilesX.clear();
    	exploredTilesY.clear();
    }
    
    public void init(MoveExploredTiles src) {
    	clear();
    	this.removeFogOfWarX.addAll(src.removeFogOfWarX);
    	this.removeFogOfWarY.addAll(src.removeFogOfWarY);
    	this.exploredTilesX.addAll(src.exploredTilesX);
    	this.exploredTilesY.addAll(src.exploredTilesY);
    }
    
	public void addExploredTile(int x, int y) {
		exploredTilesX.add(x);
		exploredTilesY.add(y);
	}
	
	public void addRemoveFogOfWar(int x, int y) {
		removeFogOfWarX.add(x);
		removeFogOfWarY.add(y);
	}

	public boolean isExploredNewTiles() {
		return removeFogOfWarX.size > 0 || exploredTilesX.size > 0;
	}
	
}
