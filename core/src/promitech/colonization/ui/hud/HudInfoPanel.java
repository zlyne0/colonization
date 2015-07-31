package promitech.colonization.ui.hud;

import promitech.colonization.GameResources;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;

class HudInfoPanel extends Actor {
    private Texture infoPanelSkin;
    
    public HudInfoPanel(GameResources gameResources) {
        infoPanelSkin = gameResources.getImage("InfoPanel.skin");
    }
    
    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(infoPanelSkin, getStage().getWidth() - infoPanelSkin.getWidth(), 0);
    }
}
