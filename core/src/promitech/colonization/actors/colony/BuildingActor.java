package promitech.colonization.actors.colony;

import com.badlogic.gdx.graphics.g2d.Batch;
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

class BuildingActor extends ImageButton {
	
    final Building building;
    private Frame resourceProductionImage;
    private int resourceProductionAmount = 0;

    private static TextureRegionDrawable getBuildingTexture(Building building) {
    	Frame img = GameResources.instance.buildingTypeImage(building.buildingType);
    	return new TextureRegionDrawable(img.texture);
    }
    
    BuildingActor(Building building) {
        super(getBuildingTexture(building));
        this.building = building;
    }
    
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        
        if (resourceProductionAmount <= 5) {
            float startOffsetX = getWidth() / 2  - (resourceProductionAmount * resourceProductionImage.texture.getRegionWidth()) / 2;
            for (int i=0; i<resourceProductionAmount; i++) {
                batch.draw(resourceProductionImage.texture, getX() + startOffsetX, getY() + getHeight() - resourceProductionImage.texture.getRegionHeight());
                startOffsetX += resourceProductionImage.texture.getRegionWidth()/2;
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
    }

    void putUnit(UnitActor unitActor) {
        building.workers.add(unitActor.unit);
        
        addActor(unitActor);
        resetUnitActorPlacement();
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

    void initProductionDesc(Colony colony) {
        BuildingProductionInfo productionInfo = colony.productionInfo(building);
        //GameResources.instance.getFrame(key)
        resourceProductionImage = GameResources.instance.getFrame("model.goods.hammers.image");
        resourceProductionAmount = 5;
    }
}
