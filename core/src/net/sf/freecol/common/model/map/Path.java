package net.sf.freecol.common.model.map;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;

import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;

public class Path {

	public final Array<Tile> tiles;
	public final IntArray turns;
	final Tile startTile;
	final Tile endTile;
	public final Unit unit;
	
	public Path(Unit unit, Tile startTile, Tile endTile, int length) {
	    this.unit = unit;
		this.tiles = new Array<Tile>(length);
		this.turns = new IntArray(length);
		
		this.startTile = startTile;
		this.endTile = endTile;
	}
	
	public void add(Tile tile, int turn) {
		this.tiles.add(tile);
		this.turns.add(turn);
	}
	
	public String toString() {
		String st = "";
		for (int i=0; i<tiles.size; i++) {
		    Tile tile = tiles.get(i);
		    String tileStr = tile.getId() + ", x: " + tile.x + ", y: " + tile.y + ", " + tile.type;
			st += "tile: [" + tileStr + "], turn: [" + turns.get(i) + "]\r\n";
		}
		return st;
	}
	
}
