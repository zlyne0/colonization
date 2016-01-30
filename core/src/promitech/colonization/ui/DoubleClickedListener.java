package promitech.colonization.ui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class DoubleClickedListener extends ClickListener {
    private long clickedDelay = 250;
    private long lastClicked = 0;
    
    @Override
    public void clicked(InputEvent event, float x, float y) {
        if (System.currentTimeMillis() - lastClicked <= clickedDelay) {
            doubleClicked(event, x, y);
        } 
        lastClicked = System.currentTimeMillis();
    }
    
    public void doubleClicked(InputEvent event, float x, float y) {
    }
}
