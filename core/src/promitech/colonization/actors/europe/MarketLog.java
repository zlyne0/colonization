package promitech.colonization.actors.europe;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.utils.Align;

import promitech.colonization.GameResources;

public class MarketLog extends ScrollPane {
    private final Label label; 
    
    public MarketLog() {
        super(null, GameResources.instance.getUiSkin());

        label = new Label("", GameResources.instance.getUiSkin());
        label.setAlignment(Align.top | Align.left);
        
        setWidget(label);
        
        setForceScroll(false, false);
        setFadeScrollBars(false);
        setOverscroll(true, true);
        setScrollBarPositions(false, true);
    }

    public void appendLine(String str) {
        if (label.getText().length > 0) {
            label.setText(label.getText() + "\n");
        }
        label.setText(label.getText() + str);
        
        setScrollPercentY(100);
        layout();
    }
    
    @Override
    public float getPrefHeight() {
        return 300;
    }
    
    @Override
    public float getPrefWidth() {
        return 200;
    }
}

