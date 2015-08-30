package promitech.colonization.ui.hud;

import java.util.ArrayList;
import java.util.List;

import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.TileImprovement;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitLabel;
import promitech.colonization.GameController;
import promitech.colonization.GameResources;
import promitech.colonization.actors.ChangeSelectedUnitListener;
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

public class HudInfoPanel extends Actor implements ChangeSelectedUnitListener {
    private Frame infoPanelSkin;
    private MapActor mapActor;
    private ShapeRenderer shapeRenderer = new ShapeRenderer();
    
    private final GameResources gameResources;
    private final GameController gameController;
    
    private Unit selectedUnit;
    private final List<String> selectedUnitDescriptions = new ArrayList<String>(5);
	private final UnitLabel unitLabel;
    
    public HudInfoPanel(GameController gameController, GameResources gameResources) {
    	this.gameController = gameController;
    	this.gameResources = gameResources;
        infoPanelSkin = gameResources.getFrame("InfoPanel.skin");
        unitLabel = new UnitLabel(gameController.getSpecification());
        
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
        
    	if (selectedUnit != null) {
    		drawUnitDescription(batch, selectedUnit);
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
		
		for (int i=0; i<selectedUnitDescriptions.size(); i++) {
			drawUnitDescriptionLine(batch, i, selectedUnitDescriptions.get(i));
		}
		String unitMovesLabel = Messages.msg("moves") + " " + unitLabel.getMovesAsString(selectedUnit);
		drawUnitDescriptionLine(batch, selectedUnitDescriptions.size()+1, unitMovesLabel);
	}

	private void drawUnitDescriptionLine(Batch batch, int lineIndex, String line) {
		BitmapFont titleFont = FontResource.getInfoPanelTitleFont();
		titleFont.draw(batch, line, 
				getX() + getWidth()/2, 
				getY() + 100 - lineIndex*(titleFont.getCapHeight() + 10)
		);
	}
    
	private void drawTileDescription(Batch batch, Tile tile) {
    	BitmapFont titleFont = FontResource.getInfoPanelTitleFont();
    	
    	String descKey;
    	if (mapActor.mapDrawModel().playingPlayer.isTileExplored(tile.x, tile.y)) {
    		descKey = Messages.nameKey(tile.type.getId());
    	} else {
    		descKey = "unexplored";
    	}
    	StringBuilder desc = new StringBuilder(Messages.msg(descKey));
    	
    	for (TileImprovement improvement : tile.getTileImprovements()) {
    		if (improvement.isComplete()) {
    			descKey = Messages.descriptionKey(improvement.type.id);
    			desc.append(", ");
    			desc.append(Messages.msg(descKey));
    		}
    	}
    	float descWidth = FontResource.strWidth(titleFont, desc.toString());
    	titleFont.draw(batch, desc, getX() + getWidth()/2 - descWidth/2, getY() + 130);
    }
    
	@Override
	public void changeSelectedUnitAction(Unit newSelectedUnit) {
		this.selectedUnit = newSelectedUnit;
		selectedUnitDescriptions.clear();
		if (newSelectedUnit == null) {
			return;
		}
		
		String label = unitLabel.getName(selectedUnit);
		if (label != null) {
			selectedUnitDescriptions.add(label);
		}
		label = unitLabel.getUnitType(selectedUnit);
		if (label != null) {
			selectedUnitDescriptions.add(label);
		}
		
		label = unitLabel.getUnitEquipment(selectedUnit);
		if (label != null) {
			selectedUnitDescriptions.add(label);
		}
	}
	
	public void setMapActor(MapActor mapActor) {
		this.mapActor = mapActor;
		this.mapActor.mapDrawModel().addChangeSelectedUnitListener(this);
	}

}
