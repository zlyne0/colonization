package promitech.map.isometric;

import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

class AutoFreePoolableTileIterable extends PoolableTileIterable {

    private static final Pool<AutoFreePoolableTileIterable> autoFreePool = Pools.get(AutoFreePoolableTileIterable.class);    
    
    public static AutoFreePoolableTileIterable obtain() {
        return autoFreePool.obtain();
    }
    
    public static void release(AutoFreePoolableTileIterable obj) {
        autoFreePool.free(obj);
    }
    
    @Override
    public boolean hasNext() {
        boolean hn = spiralIterator2.hasNext();
        if (!hn) {
            release(this);
        }
        return hn;
    }
    
}
