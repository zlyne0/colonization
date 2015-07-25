package promitech.colonization.infrastructure;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;

public class CenterSizableViewport extends ScalingViewport {
    private final int maxWidth;
    private final int maxHeight;
   
    public CenterSizableViewport(float worldWidth, float worldHeight, int maxWidth, int maxHeight) {
        super(Scaling.fit, worldWidth, worldHeight);
       
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }
   
    @Override
    public void update(int screenWidth, int screenHeight, boolean centerCamera) {
        int newScreenWidth = screenWidth;
        int newScreenHeight = screenHeight;
       
        if (newScreenWidth > maxWidth) {
            newScreenWidth = maxWidth;
        }
        if (newScreenHeight > maxHeight) {
            newScreenHeight = maxHeight;
        }
       
        Vector2 scaled = getScaling().apply(getWorldWidth(), getWorldHeight(), newScreenWidth, newScreenHeight);
        int viewportWidth = Math.round(scaled.x);
        int viewportHeight = Math.round(scaled.y);
       
        // Center.
        setScreenBounds((screenWidth - viewportWidth) / 2, (screenHeight - viewportHeight) / 2, viewportWidth, viewportHeight);
        apply(centerCamera);
    }
}    

