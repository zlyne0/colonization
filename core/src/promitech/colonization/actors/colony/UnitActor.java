package promitech.colonization.actors.colony;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;

import net.sf.freecol.common.model.Unit;
import promitech.colonization.GameResources;
import promitech.colonization.actors.map.UnitDrawer;

class UnitActor extends Widget {
    final Unit unit;
    private boolean drawUnitChip;
    private ShapeRenderer shapeRenderer;
    private TextureRegion texture;
    
    private static TextureRegion getTexture(Unit unit) {
        return GameResources.instance.getFrame(unit.resourceImageKey()).texture;
    }
    
    UnitActor(final Unit unit) {
    	this.texture = getTexture(unit);
    	
        this.drawUnitChip = false;
        this.unit = unit;
        
        setSize(getPrefWidth(), getPrefHeight());
    }

    UnitActor(final Unit unit, boolean drawUnitChip, ShapeRenderer shapeRenderer) {
    	this.texture = getTexture(unit);
    	
        this.shapeRenderer = shapeRenderer;
        this.drawUnitChip = drawUnitChip;
        this.unit = unit;
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
		if (drawUnitChip) {
			batch.draw(texture, getX() + UnitDrawer.BOX_WIDTH, getY());
			
			shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
			shapeRenderer.setTransformMatrix(batch.getTransformMatrix());
			
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
	}

	public void disableUnitChip() {
		this.drawUnitChip = false;
		this.shapeRenderer = null;
	}
	
	public TextureRegion getTexture() {
		return texture;
	}
}
