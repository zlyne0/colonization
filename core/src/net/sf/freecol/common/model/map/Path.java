package net.sf.freecol.common.model.map;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;

import net.sf.freecol.common.model.Tile;

public class Path {

	final Array<Tile> tiles;
	final IntArray turns;
	
	public Path(int lenght) {
		this.tiles = new Array<Tile>(lenght);
		this.turns = new IntArray(lenght);
	}
	
	public void add(Tile tile, int turn) {
		this.tiles.add(tile);
		this.turns.add(turn);
	}
	
	public String toString() {
		String st = "";
		for (int i=0; i<tiles.size; i++) {
			st += "tile: [" + tiles.get(i).toString() + "], turn: [" + turns.get(i) + "]\r\n";
		}
		return st;
	}
	
}
