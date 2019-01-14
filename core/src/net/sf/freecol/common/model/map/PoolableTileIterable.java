package net.sf.freecol.common.model.map;

import java.util.Iterator;

import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.Tile;

public class PoolableTileIterable implements Iterable<Tile>, Iterator<Tile> {
    private static final Pool<PoolableTileIterable> pool = Pools.get(PoolableTileIterable.class);
    
    protected final SpiralIterator2 spiralIterator2 = new SpiralIterator2();

    public static PoolableTileIterable obtain() {
        return pool.obtain();
    }
    
    public void reset(Map map, int centerX, int centerY, int radius) {
        spiralIterator2.reset(map, centerX, centerY, radius);
    }

    @Override
    public Iterator<Tile> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        return spiralIterator2.hasNext();
    }

    @Override
    public Tile next() {
        return spiralIterator2.next();
    }

	@Override
	public void remove() {
		throw new IllegalStateException("not implemented");
	}

}