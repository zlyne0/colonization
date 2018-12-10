package net.sf.freecol.common.model.map;

import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

public class AutoFreePoolableTileIterable extends PoolableTileIterable {

    private static final Pool<AutoFreePoolableTileIterable> autoFreePool = Pools.get(AutoFreePoolableTileIterable.class);    
    
    public static AutoFreePoolableTileIterable obtain() {
        return autoFreePool.obtain();
    }
    
    @Override
    public boolean hasNext() {
        boolean hn = spiralIterator2.hasNext();
        if (!hn) {
            autoFreePool.free(this);
        }
        return hn;
    }
    
}
