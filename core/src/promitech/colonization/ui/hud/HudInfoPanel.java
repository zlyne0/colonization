package promitech.colonization.ui.hud;

import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.TileImprovement;
import net.sf.freecol.common.model.Unit;
import promitech.colonization.GameController;
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
    
    private final GameResources gameResources;
    private final GameController gameController;
    
    public HudInfoPanel(GameController gameController, GameResources gameResources) {
    	this.gameController = gameController;
    	this.gameResources = gameResources;
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
        
    	Unit unit = mapActor.mapDrawModel().selectedUnit;
    	if (unit != null) {
    		drawUnitDescription(batch, unit);
    	} else {
    		Tile tile = mapActor.mapDrawModel().selectedTile;
    		if (tile != null) {
    			mapActor.drawSelectedTile(batch, shapeRenderer, getX() + 30, getY() + 10);
    			drawTileDescription(batch, tile);
    		}
    	}
    }

    private void drawUnitDescription(Batch batch, Unit unit) {
    	Frame frame = gameResources.getCenterAdjustFrameTexture(unit.resourceImageKey());
    	
		batch.draw(frame.texture, 
				getX() + frame.offsetX, getY() + frame.offsetY + 30
		);
	}

	private void drawTileDescription(Batch batch, Tile tile) {
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
