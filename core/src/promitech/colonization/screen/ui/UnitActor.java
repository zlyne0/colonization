package promitech.colonization.screen.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;

import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.specification.AbstractGoods;
import promitech.colonization.GameResources;
import promitech.colonization.screen.colony.DragAndDropPreHandlerTargetContainer;
import promitech.colonization.screen.colony.DragAndDropSourceContainer;
import promitech.colonization.screen.colony.DragAndDropTargetContainer;
import promitech.colonization.screen.map.UnitDrawer;
import promitech.colonization.ui.DoubleClickedListener;

public class UnitActor extends Widget implements 
	DragAndDropTargetContainer<AbstractGoods>,
	DragAndDropPreHandlerTargetContainer<AbstractGoods>
{
    public final Unit unit;
    private boolean drawUnitChip = false;
    private boolean drawFocus = false;
    private ShapeRenderer shapeRenderer;
    private TextureRegion texture;
    
    private ChangeColonyStateListener changeColonyStateListener;
    private CargoPanel cargoPanel;
    
    public DragAndDropSourceContainer<UnitActor> dragAndDropSourceContainer;
    
    private static TextureRegion getTexture(Unit unit) {
        return GameResources.instance.getFrame(unit.resourceImageKey()).texture;
    }

    public UnitActor(final Unit unit) {
    	this(unit, null);
    }
    
    public UnitActor(final Unit unit, DoubleClickedListener unitActorDoubleClickListener) {
    	this.texture = getTexture(unit);
        this.drawUnitChip = false;
        this.unit = unit;
        setSize(getPrefWidth(), getPrefHeight());
        if (unitActorDoubleClickListener != null) {
        	addListener(unitActorDoubleClickListener);
        }
    }

    public UnitActor withCargoPanel(CargoPanel cargoPanel, ChangeColonyStateListener changeColonyStateListener) {
    	this.changeColonyStateListener = changeColonyStateListener;
    	this.cargoPanel = cargoPanel;
    	return this;
    }
    
    public void updateTexture() {
    	this.texture = getTexture(unit);
    	invalidate();
    	pack();
    }
    
    @Override
    public float getPrefWidth() {
    	if (drawUnitChip) {
    		return UnitDrawer.BOX_WIDTH + texture.getRegionWidth();
    	} else {
    		return texture.getRegionWidth();
    	}
    }
    
    @Override
    public float getPrefHeight() {
    	return texture.getRegionHeight();
    }
    
    @Override
    public float getWidth() {
    	return getPrefWidth();
    }
    
    @Override
    public float getHeight() {
    	return getPrefHeight();
    }
    
	public void draw(Batch batch, float parentAlpha) {
		if (drawFocus || drawUnitChip) {
			shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
			shapeRenderer.setTransformMatrix(batch.getTransformMatrix());
		}
		
		if (drawFocus) {
			UnitDrawer.drawColonyUnitFocus(batch, shapeRenderer, 
					getX(), getY(), texture.getRegionWidth(), drawUnitChip
			);
		}
		if (drawUnitChip) {
			batch.draw(texture, getX() + UnitDrawer.BOX_WIDTH, getY());
			
			UnitDrawer.drawColonyUnitChip(batch, shapeRenderer, 
				unit, 
				getX(), getY(), getHeight()
			);
		} else {
			batch.draw(texture, getX(), getY());
		}
	}

	public void enableUnitChip(ShapeRenderer shapeRenderer) {
		this.drawUnitChip = true;
		this.shapeRenderer = shapeRenderer;
        setSize(getPrefWidth(), getPrefHeight());
	}

	public void disableUnitChip() {
		this.drawUnitChip = false;
		this.shapeRenderer = null;
        setSize(getPrefWidth(), getPrefHeight());
	}
	
	public TextureRegion getTexture() {
		return texture;
	}

    public void disableFocus() {
        drawFocus = false;
    }

    public void enableFocus(ShapeRenderer aShapeRenderer) {
        drawFocus = true;
        this.shapeRenderer = aShapeRenderer;
    }

	@Override
	public void putPayload(AbstractGoods anAbstractGood, float x, float y) {
		System.out.println("carrierPanel: carrierId[" + unit.getId() + "] put goods " + anAbstractGood);
		
		if (anAbstractGood.isNotEmpty()) {
			unit.loadCargo(anAbstractGood);

			cargoPanel.updateCargoPanelData();
			changeColonyStateListener.transfereGoods();
		}
	}

	@Override
	public boolean canPutPayload(AbstractGoods anAbstractGood, float x, float y) {
		boolean canPut = unit.hasSpaceForAdditionalCargo(anAbstractGood);
		if (canPut) {
			return true;
		}
		int maxGoodsAmountToFillFreeSlots = unit.maxGoodsAmountToFillFreeSlots(anAbstractGood.getTypeId());
		if (maxGoodsAmountToFillFreeSlots <= 0) {
			return false;
		}
		return true;
	}
	
	@Override
	public void onDragPayload(float x, float y) {
	}

	@Override
	public void onLeaveDragPayload() {
	}

	@Override
	public boolean isPrePutPayload(AbstractGoods anAbstractGood, float x, float y) {
		int maxGoodsAmountToFillFreeSlots = unit.maxGoodsAmountToFillFreeSlots(anAbstractGood.getTypeId());
		return maxGoodsAmountToFillFreeSlots > 0;
	}

	@Override
	public void prePutPayload(AbstractGoods anAbstractGood, float x, float y,
			DragAndDropSourceContainer<AbstractGoods> sourceContainer) {
		int maxGoodsAmountToFillFreeSlots = unit.maxGoodsAmountToFillFreeSlots(anAbstractGood.getTypeId());
		if (maxGoodsAmountToFillFreeSlots > 0) {
			anAbstractGood.setQuantity(Math.min(anAbstractGood.getQuantity(), maxGoodsAmountToFillFreeSlots));
		}
	}
	
}
