package promitech.map.isometric;

import java.util.Iterator;

import net.sf.freecol.common.util.Predicate;
import promitech.colonization.Direction;

class NeighbourTilesIterable<TILE_TYPE> implements Iterable<NeighbourIterableTile<TILE_TYPE>>, Iterator<NeighbourIterableTile<TILE_TYPE>> {
    
    private final IsometricMap<TILE_TYPE> map;
    private final int sourceX;
    private final int sourceY; 
    private final Predicate<TILE_TYPE> tileFilter;

    private int cursor = 0;
    private int foundFirstIndex = 0;
    private final NeighbourIterableTile<TILE_TYPE> iterableTile = new NeighbourIterableTile<TILE_TYPE>();
    
    NeighbourTilesIterable(IsometricMap<TILE_TYPE> map, int sourceX, int sourceY, Predicate<TILE_TYPE> tileFilter) {
        this.sourceX = sourceX;
        this.sourceY = sourceY;
        this.map = map;
        this.tileFilter = tileFilter;
    }
    
    @Override
    public Iterator<NeighbourIterableTile<TILE_TYPE>> iterator() {
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
    public NeighbourIterableTile<TILE_TYPE> next() {
        cursor++;
        return iterableTile;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}