package promitech.map.isometric;

import net.sf.freecol.common.util.Predicate;
import promitech.colonization.Direction;
import promitech.map.Map2DArray;

public class IsometricMap<TILE_TYPE> extends Map2DArray<TILE_TYPE> {

    public IsometricMap(Class<TILE_TYPE> tileTypeClass, int width, int height) {
        super(tileTypeClass, width, height);
    }

    public TILE_TYPE getTile(int x, int y, Direction direction) {
        return getTile(direction.stepX(x, y), direction.stepY(x, y));
    }
    
    public Iterable<NeighbourIterableTile<TILE_TYPE>> neighbourTiles(int x, int y) {
        return new NeighbourTilesIterable<TILE_TYPE>(
            IsometricMap.this, 
            x, y,
            (Predicate<TILE_TYPE>) Predicate.AlwaysTrue
        );
    }

    public Iterable<NeighbourIterableTile<TILE_TYPE>> neighbourTiles(int x, int y, Predicate<TILE_TYPE> predicate) {
        return new NeighbourTilesIterable<TILE_TYPE>(IsometricMap.this, x, y, predicate);
    }

    /**
     * Important: Do not break loop. Iterable is put back to pool when Iterator.hasNext return false 
     * @param x
     * @param y
     * @param radius
     * @return
     */
    @SuppressWarnings("unchecked")
    public Iterable<TILE_TYPE> neighbourTiles(int x, int y, int radius) {
        AutoFreePoolableTileIterable iterable = AutoFreePoolableTileIterable.obtain();
        iterable.reset(this, x, y, radius);
        return iterable;
    }
    
    public boolean isTileExists(int x, int y, Predicate<TILE_TYPE> predicate) {
        Direction direction;
        TILE_TYPE tile;
        for (int i = 0; i < Direction.allDirections.size(); i++) {
            direction = Direction.allDirections.get(i);
            tile = getTile(x, y, direction);
            if (tile != null && predicate.test(tile)) {
                return true;
            }
        }
        return false;
    }
    
    @SuppressWarnings("unchecked")
    public boolean isTileExists(int x, int y, int radius, Predicate<TILE_TYPE> predicate) {
        AutoFreePoolableTileIterable iterable = AutoFreePoolableTileIterable.obtain();
        iterable.reset(this, x, y, radius);
        
        for (TILE_TYPE tile : (Iterable<TILE_TYPE>)iterable) {
            if (predicate.test(tile)) {
                AutoFreePoolableTileIterable.release(iterable);
                return true;
            }
        }
        return false;
    }
    
    public TILE_TYPE findFirst(int x, int y, Predicate<TILE_TYPE> predicate) {
        Direction direction;
        TILE_TYPE tile;
        for (int i = 0; i < Direction.allDirections.size(); i++) {
            direction = Direction.allDirections.get(i);
            tile = getTile(x, y, direction);
            if (tile != null && predicate.test(tile)) {
                return tile;
            }
        }
        return null;
    }
}
