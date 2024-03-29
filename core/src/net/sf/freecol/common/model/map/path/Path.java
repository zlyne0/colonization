package net.sf.freecol.common.model.map.path;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;

import net.sf.freecol.common.model.Tile;

public class Path {

	public final Array<Tile> tiles;
	public final IntArray turns;
	final Tile startTile;
	public final Tile endTile;
	boolean toEurope = false;
	private final boolean reachDestination;

	public Path(Tile startTile, Tile endTile, int length, boolean reachDestination) {
		this.tiles = new Array<Tile>(length);
		this.turns = new IntArray(length);
		
		this.startTile = startTile;
		this.endTile = endTile;
		this.reachDestination = reachDestination;
	}
	
	public void add(Tile tile, int turn) {
		this.tiles.add(tile);
		this.turns.add(turn);
	}
	
	public String toString() {
		String st = "";
		if (reachDestination) {
			st = "reachDestination ";
		} else {
			st = "not reachDestination ";
		}
		if (tiles.size == 0) {
			st = "empty path";
		}
		for (int i=0; i<tiles.size; i++) {
		    Tile tile = tiles.get(i);
		    String tileStr = tile.getId() + ", x: " + tile.x + ", y: " + tile.y + ", " + tile.getType();
			st += "tile: [" + tileStr + "], turn: [" + turns.get(i) + "]\r\n";
		}
		return st;
	}

	public void removeFirst() {
		if (tiles.size > 0) {
			tiles.removeIndex(0);
		}
		if (turns.size > 0) {
			turns.removeIndex(0);
		}
	}

	public boolean hasNotTilesToMove() {
		return tiles.size < 2;
	}

	public Tile moveStepSource() {
		return tiles.get(0);
	}

	public Tile moveStepDest() {
		return tiles.get(1);
	}

	public boolean reachTile(Tile tile) {
		for (int i = 0; i < tiles.size; i++) {
	        if (tiles.get(i).equalsCoordinates(tile)) {
	            return true;
	        }
	    }
	    return false;
	}
	
	public boolean isReachedDestination() {
		return reachDestination;
	}

	public int totalTurns() {
		return turns.get(turns.size-1);
	}
	
	public boolean isQuickestThan(Path path) {
		return this.tiles.size < path.tiles.size;
	}
	
	public boolean isPathToEurope() {
		return toEurope;
	}

	public boolean isStartEqualsEnd() {
		return startTile.equalsCoordinates(endTile);
	}
}
