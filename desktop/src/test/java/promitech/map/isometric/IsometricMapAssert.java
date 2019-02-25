package promitech.map.isometric;

import static org.assertj.core.api.Assertions.assertThat;

import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

public class IsometricMapAssert {

    
    public static void hasAutoFreePoolableTileIterablePoolSize(int size) {
        Pool<AutoFreePoolableTileIterable> pool = Pools.get(AutoFreePoolableTileIterable.class);
        assertThat(pool.peak).isEqualTo(size);
    }
    
}
