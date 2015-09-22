package promitech.colonization.actors.colony;

import java.util.Map.Entry;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import net.sf.freecol.common.model.Building;
import net.sf.freecol.common.model.BuildingProductionInfo;
import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Unit;
import promitech.colonization.GameResources;
import promitech.colonization.gdx.Frame;
import promitech.colonization.infrastructure.FontResource;

class BuildingActor extends ImageButton {
	
	final Colony colony;
    final Building building;
    private Frame resourceProductionImage;
    private int resourceProductionAmount = 0;

    private static TextureRegionDrawable getBuildingTexture(Building building) {
    	Frame img = GameResources.instance.buildingTypeImage(building.buildingType);
    	return new TextureRegionDrawable(img.texture);
    }
    
    BuildingActor(Colony colony, Building building) {
        super(getBuildingTexture(building));
        this.building = building;
        this.colony = colony;
    }
    
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        
        if (resourceProductionAmount > 0) {
	        float imageDistance = (getWidth() - 40) / resourceProductionAmount;
	        if (imageDistance > resourceProductionImage.texture.getRegionWidth()) {
	            imageDistance = resourceProductionImage.texture.getRegionWidth();
	        }

	        float y = getY() + getHeight() - resourceProductionImage.texture.getRegionHeight();
	        float startOffsetX = 10;
	        for (int i=0; i<resourceProductionAmount; i++) {
	            batch.draw(resourceProductionImage.texture, getX() + startOffsetX, y) ;
	            startOffsetX += imageDistance;
	        }
	        if (resourceProductionAmount > 5) {
	        	BitmapFont font = FontResource.getBuildingGoodsQuantityFont();
	        	y = getY() + getHeight() - resourceProductionImage.texture.getRegionHeight()/2 + font.getCapHeight()/2;
	        	font.draw(batch, Integer.toString(resourceProductionAmount), getX() + getWidth()/2, y);
	        }
        }
    }
    
    void initWorkers(DragAndDrop dragAndDrop) {
        int offsetX = 0; 
        for (Unit worker : building.workers.entities()) {
            UnitActor unitActor = new UnitActor(worker);
            addActor(unitActor);
            
            dragAndDrop.addSource(new UnitDragAndDropSource(unitActor));
            
            unitActor.moveBy(offsetX, 0);
            offsetX += unitActor.getWidth();
        }
    }

    void takeUnit(UnitActor unitActor) {
        building.workers.removeId(unitActor.unit);
        removeActor(unitActor);
        resetUnitActorPlacement();
        resetProductionDesc();
    }

    void putUnit(UnitActor unitActor) {
        building.workers.add(unitActor.unit);
        
        addActor(unitActor);
        resetUnitActorPlacement();
        resetProductionDesc();
    }

    void resetUnitActorPlacement() {
        float unitOffsetX = 0;
        for (Actor actor : getChildren()) {
            if (actor instanceof UnitActor) {
                actor.setX(unitOffsetX);
                unitOffsetX += actor.getWidth();
            }
        }
    }

    void resetProductionDesc() {
        BuildingProductionInfo productionInfo = colony.productionInfo(building);
        if (productionInfo.goods.size() == 0) {
        	resourceProductionAmount = 0;
        	resourceProductionImage = null;
        } else {
        	if (productionInfo.goods.size() == 1) {
        		for (Entry<String, Integer> prodEntry : productionInfo.goods.entrySet()) {
        			resourceProductionImage = GameResources.instance.getFrame(prodEntry.getKey() + ".image");
        			resourceProductionAmount = prodEntry.getValue();
        		}
        	} else {
        		throw new IllegalStateException("there is more then one production type in building " + building + ", prodInfo: " + productionInfo);
        	}
        }
    }
}
