package promitech.colonization.ui.hud;

import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.TileImprovement;
import promitech.colonization.GameResources;
import promitech.colonization.actors.MapActor;
import promitech.colonization.gdx.Frame;
import promitech.colonization.infrastructure.FontResource;
import promitech.colonization.ui.resources.Messages;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class HudInfoPanel extends Actor {
    private Frame infoPanelSkin;
    private MapActor mapActor;
    private ShapeRenderer shapeRenderer = new ShapeRenderer();
    
    public HudInfoPanel(GameResources gameResources) {
        infoPanelSkin = gameResources.getFrame("InfoPanel.skin");
        
        addListener(new InputListener() {
        	@Override
        	public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        		return true;
        	}
        });
    }

    @Override
    protected void setStage(Stage stage) {
    	super.setStage(stage);
		setBounds(
				stage.getWidth() - infoPanelSkin.texture.getRegionWidth(), 0,
				infoPanelSkin.texture.getRegionWidth(), infoPanelSkin.texture.getRegionHeight()
		);
    }
    
    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(infoPanelSkin.texture, getX(), 0);
        
        mapActor.drawSelectedTile(batch, shapeRenderer, getX() + 30, getY() + 10);
        
        drawTileDescription(batch);
    }

    private void drawTileDescription(Batch batch) {
    	Tile tile = mapActor.mapDrawModel().selectedTile;
    	if (tile == null) {
    		return;
    	}
    	BitmapFont titleFont = FontResource.getInfoPanelTitleFont();
    	
    	String descKey;
    	if (tile.isUnexplored(mapActor.mapDrawModel().playingPlayer)) {
    		descKey = "unexplored";
    	} else {
    		descKey = Messages.nameKey(tile.type.getId());
    	}
    	StringBuilder desc = new StringBuilder(Messages.msg(descKey));
    	
    	for (TileImprovement improvement : tile.tileImprovements) {
    		if (improvement.isComplete()) {
    			descKey = Messages.descriptionKey(improvement.type.id);
    			desc.append(", ");
    			desc.append(Messages.msg(descKey));
    		}
    	}
    	float descWidth = FontResource.strWidth(titleFont, desc.toString());
    	titleFont.draw(batch, desc, getX() + getWidth()/2 - descWidth/2, getY() + 130);
    }
    
	public void setMapActor(MapActor mapActor) {
		this.mapActor = mapActor;
	}
}
