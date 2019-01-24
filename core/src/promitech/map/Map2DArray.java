package promitech.map;

import java.lang.reflect.Array;

import net.sf.freecol.common.util.Consumer;

public class Map2DArray<TILE_TYPE> extends AbstractArray2D {

    protected TILE_TYPE[][] tiles;
    
    public Map2DArray(Class<TILE_TYPE> tileTypeClass, int width, int height) {
        super(width, height);
        tiles = (TILE_TYPE[][])Array.newInstance(tileTypeClass, height, width);
    }

    public void createTile(int x, int y, TILE_TYPE tile) {
        tiles[y][x] = tile;
    }
    
    public TILE_TYPE getTile(int x, int y) {
        if (isIndexValid(x, y)) {
            return tiles[y][x];
        }
        return null;
    }
    
    public TILE_TYPE getSafeTile(int x, int y) {
        return tiles[y][x];
    }
    
    public void runOnAllTiles(Consumer<TILE_TYPE> tileConsumer) {
        int x, y;
        for (y = 0; y < height; y++) {
            TILE_TYPE row[] = tiles[y];
            for (x = 0; x < width; x++) {
                TILE_TYPE t = row[x];
                tileConsumer.consume(t);
            }
        }
    }
    
}
