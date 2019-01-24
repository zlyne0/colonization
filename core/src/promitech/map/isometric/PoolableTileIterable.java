package promitech.map.isometric;

import java.util.Iterator;

import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

class PoolableTileIterable<TILE_TYPE> implements Iterable<TILE_TYPE>, Iterator<TILE_TYPE> {
    @SuppressWarnings("rawtypes")
    private static final Pool<PoolableTileIterable> pool = Pools.get(PoolableTileIterable.class);
    
    protected final SpiralIterator2<TILE_TYPE> spiralIterator2 = new SpiralIterator2<TILE_TYPE>();

    @SuppressWarnings("unchecked")
    public static <T> PoolableTileIterable<T> obtain() {
        return pool.obtain();
    }
    
    public void reset(IsometricMap<TILE_TYPE> map, int centerX, int centerY, int radius) {
        spiralIterator2.reset(map, centerX, centerY, radius);
    }

    @Override
    public Iterator<TILE_TYPE> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        return spiralIterator2.hasNext();
    }

    @Override
    public TILE_TYPE next() {
        return spiralIterator2.next();
    }

	@Override
	public void remove() {
		throw new IllegalStateException("not implemented");
	}

}