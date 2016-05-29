package promitech.colonization.ui.hud;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;

import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.TileImprovement;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitLabel;
import net.sf.freecol.common.model.specification.AbstractGoods;
import promitech.colonization.GameResources;
import promitech.colonization.actors.map.ChangeSelectedUnitListener;
import promitech.colonization.actors.map.MapActor;
import promitech.colonization.gdx.Frame;
import promitech.colonization.infrastructure.FontResource;
import promitech.colonization.ui.resources.Messages;

public class HudInfoPanel extends Actor implements ChangeSelectedUnitListener {
    private static final int GOODS_IMAGE_WIDTH = 32;
    private static final int UNITS_IMAGE_WIDTH = 35;
    
    private Frame infoPanelSkin;
    private MapActor mapActor;
    private ShapeRenderer shapeRenderer = new ShapeRenderer();
    
    private final GameResources gameResources;
    
    private Unit selectedUnit;
    private final List<String> selectedUnitDescriptions = new ArrayList<String>(5);
	private final UnitLabel unitLabel;
	private List<AbstractGoods> carrierGoods;
    
    public HudInfoPanel(GameResources gameResources) {
    	this.gameResources = gameResources;
        infoPanelSkin = gameResources.getFrame("InfoPanel.skin");
        unitLabel = new UnitLabel();
        
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
    	if (stage == null) {
    		return;
    	}
		setBounds(
				stage.getWidth() - infoPanelSkin.texture.getRegionWidth(), 0,
				infoPanelSkin.texture.getRegionWidth(), infoPanelSkin.texture.getRegionHeight()
		);
    }
    
    @Override
    public void draw(Batch batch, float parentAlpha) {
    	batch.setColor(Color.WHITE);
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
		
		drawUnitContener(batch, unit);
	}

	private void drawUnitContener(Batch batch, Unit unit) {
		int containerWidth = 0;
		if (carrierGoods != null) {
			containerWidth += carrierGoods.size() * GOODS_IMAGE_WIDTH;
		}
		if (unit.getUnitContainer() != null) {
			containerWidth += unit.getUnitContainer().getUnits().size() * UNITS_IMAGE_WIDTH;
		}
		
		int x = (int)getWidth() / 2 - containerWidth/2 + 40;
		if (carrierGoods != null) {
			BitmapFont font = FontResource.getInfoPanelTitleFont();
			
			for (AbstractGoods good : carrierGoods) {
                Frame goodsImage = gameResources.goodsImage(good.getTypeId());
				batch.draw(goodsImage.texture, getX() + x, getY() + 30);
				font.draw(batch, Integer.toString(good.getQuantity()), getX() + x + 5, getY() + 30);
				
				x += GOODS_IMAGE_WIDTH;
			}
		}
		if (unit.getUnitContainer() != null) {
			for (Unit u : unit.getUnitContainer().getUnits().entities()) {
				Frame unitImage = gameResources.getFrame(u.resourceImageKey());
				batch.draw(unitImage.texture, getX() + x, getY() + 10);
				x += UNITS_IMAGE_WIDTH;
			}
		}
	}

	private void drawUnitDescriptionLine(Batch batch, int lineIndex, String line) {
		BitmapFont titleFont = FontResource.getInfoPanelTitleFont();
		titleFont.draw(batch, line, 
				getX() + getWidth()/2, 
				getY() + 150 - lineIndex*(titleFont.getCapHeight() + 10)
		);
	}
    
	private void drawTileDescription(Batch batch, Tile tile) {
    	BitmapFont titleFont = FontResource.getInfoPanelTitleFont();
    	
    	String descKey;
    	if (mapActor.mapDrawModel().playingPlayer.isTileExplored(tile.x, tile.y)) {
    		descKey = Messages.nameKey(tile.getType().getId());
    	} else {
    		descKey = "unexplored";
    	}
    	StringBuilder desc = new StringBuilder(Messages.msg(descKey));
    	
    	for (TileImprovement improvement : tile.getTileImprovements()) {
    		if (improvement.isComplete()) {
    			descKey = Messages.descriptionKey(improvement.type.getId());
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
		
		String label = UnitLabel.getName(selectedUnit);
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
		
		carrierGoods = null;
		if (newSelectedUnit.getGoodsContainer() != null) {
		    carrierGoods = newSelectedUnit.getGoodsContainer().carrierGoods();
		}
	}
	
	public void setMapActor(MapActor mapActor) {
		this.mapActor = mapActor;
		this.mapActor.mapDrawModel().addChangeSelectedUnitListener(this);
	}

}
