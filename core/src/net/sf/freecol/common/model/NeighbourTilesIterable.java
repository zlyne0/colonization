package net.sf.freecol.common.model;

import java.util.Iterator;

import net.sf.freecol.common.util.Predicate;
import promitech.colonization.Direction;

class NeighbourTilesIterable implements Iterable<NeighbourIterableTile>, Iterator<NeighbourIterableTile> {
    
    public static final Predicate<Tile> ALL = new Predicate<Tile>() {
        @Override
        public boolean test(Tile tile) {
            return true;
        }
    };

    public static final Predicate<Tile> LAND_TILES = new Predicate<Tile>() {
        @Override
        public boolean test(Tile tile) {
            return tile.getType().isLand();
        }
    };

    public static final Predicate<Tile> WATER_TILES = new Predicate<Tile>() {
        @Override
        public boolean test(Tile tile) {
            return tile.getType().isWater();
        }
    };
    
    private final Map map;
    private final int sourceX;
    private final int sourceY; 
    private final Predicate<Tile> tileFilter;

    private int cursor = 0;
    private int foundFirstIndex = 0;
    private final NeighbourIterableTile iterableTile = new NeighbourIterableTile();
    
    NeighbourTilesIterable(Map map, int sourceX, int sourceY, Predicate<Tile> tileFilter) {
    	this.sourceX = sourceX;
    	this.sourceY = sourceY;
        this.map = map;
        this.tileFilter = tileFilter;
    }
    
    @Override
    public Iterator<NeighbourIterableTile> iterator() {
        return this;
    }
        
    @Override
    public boolean hasNext() {
        boolean found = found(cursor);
        cursor = foundFirstIndex;
        if (!found) {
            return false;
        }
        return cursor < Direction.allDirections.size();
    }

    private boolean found(int ic) {
        for (foundFirstIndex = ic; foundFirstIndex < Direction.allDirections.size(); foundFirstIndex++) {
            iterableTile.direction = Direction.allDirections.get(foundFirstIndex);
            iterableTile.tile = map.getTile(sourceX, sourceY, iterableTile.direction);
            if (iterableTile.tile == null || !tileFilter.test(iterableTile.tile)) {
                continue;
            }
            return true;
        }
        return false;
    }
    
    @Override
    public NeighbourIterableTile next() {
        cursor++;
        return iterableTile;
    }

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}