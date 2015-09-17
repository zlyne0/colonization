package promitech.colonization.actors.settlement;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

import net.sf.freecol.common.model.Building;
import net.sf.freecol.common.model.Unit;
import promitech.colonization.GameResources;
import promitech.colonization.gdx.Frame;

class UnitActor extends Image {
    final Unit unit;
    
    private static TextureRegion getTexture(Unit unit) {
    	return GameResources.instance.getFrame(unit.resourceImageKey()).texture;
    }
    
    UnitActor(final Unit unit) {
		super(getTexture(unit));
        this.unit = unit;
    }
    
}

class UnitDragAndDropSource extends com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source {
	private final UnitActor sourceActor;
	
	public UnitDragAndDropSource(UnitActor actor) {
		super(actor);
		this.sourceActor = actor;
	}

	@Override
	public Payload dragStart(InputEvent event, float x, float y, int pointer) {
		Payload payload = new Payload();
		payload.setObject(sourceActor.unit);
		
		UnitActor unit = (UnitActor)getActor();
		
		Image dragActor = new Image(unit.getDrawable(), Scaling.none, Align.center);
		
		BuildingActor.dragAndDrop.setDragActorPosition(0, 0);
		
		payload.setDragActor(dragActor);
		payload.setInvalidDragActor(dragActor);
		payload.setValidDragActor(dragActor);
		return payload;
	}
}

class BuildingActor extends ImageButton {
	public static DragAndDrop dragAndDrop = new DragAndDrop();
	
    final Building building;

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
    }
    
    void initWorkers() {
        int offsetX = 0; 
        for (Unit worker : building.workers.entities()) {
            UnitActor unitActor = new UnitActor(worker);
            addActor(unitActor);
            
            dragAndDrop.addSource(new UnitDragAndDropSource(unitActor));
            
            unitActor.moveBy(offsetX, 0);
            offsetX += unitActor.getWidth();
        }
    }
}
