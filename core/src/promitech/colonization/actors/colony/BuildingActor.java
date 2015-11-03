package promitech.colonization.actors.colony;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import net.sf.freecol.common.model.Building;
import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.ProductionSummary;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitContainer.NoAddReason;
import promitech.colonization.GameResources;
import promitech.colonization.gdx.Frame;

class BuildingActor extends ImageButton implements DragAndDropSourceContainer<UnitActor>, DragAndDropTargetContainer<UnitActor> {
	
	final Colony colony;
    final Building building;
    private final ProductionQuantityDrawModel productionQuantityDrawModel = new ProductionQuantityDrawModel();
    private ProductionQuantityDrawer productionQuantityDrawer;
    private final ChangeColonyStateListener changeColonyStateListener;

    private static TextureRegionDrawable getBuildingTexture(Building building) {
    	Frame img = GameResources.instance.buildingTypeImage(building.buildingType);
    	return new TextureRegionDrawable(img.texture);
    }
    
    BuildingActor(Colony colony, Building building, ChangeColonyStateListener changeColonyStateListener) {
        super(getBuildingTexture(building));
        this.changeColonyStateListener = changeColonyStateListener;
        this.building = building;
        this.colony = colony;
    }
    
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        
        if (productionQuantityDrawer == null) {
        	productionQuantityDrawer = new ProductionQuantityDrawer(getWidth() - 20, getHeight());
        }
        productionQuantityDrawer.draw(batch, productionQuantityDrawModel, getX() + 10, getY());
    }
    
    void initWorkers(DragAndDrop dragAndDrop) {
        int offsetX = 0; 
        for (Unit worker : building.workers.entities()) {
            UnitActor unitActor = new UnitActor(worker);
            addActor(unitActor);
            unitActor.dragAndDropSourceContainer = this;
            
            dragAndDrop.addSource(new UnitDragAndDropSource(unitActor));
            
            unitActor.moveBy(offsetX, 0);
            offsetX += unitActor.getWidth();
        }
        resetProductionDesc();
    }

	@Override
	public void takePayload(UnitActor unitActor, float x, float y) {
		System.out.println("take unit [" + unitActor.unit + "] from building " + building);
		
		unitActor.dragAndDropSourceContainer = null;
		
		building.workers.removeId(unitActor.unit);
		removeActor(unitActor);
		resetUnitActorPlacement();
		resetProductionDesc();
		
		changeColonyStateListener.changeUnitAllocation(colony);
	}

	@Override
	public void putPayload(UnitActor unitActor, float x, float y) {
		System.out.println("put unit [" + unitActor.unit + "] to building " + building);

		unitActor.dragAndDropSourceContainer = this;
		
		unitActor.setX(0);
		unitActor.setY(0);
		building.workers.add(unitActor.unit);
		
		addActor(unitActor);
		resetUnitActorPlacement();
		resetProductionDesc();
	}

	@Override
	public boolean canPutPayload(UnitActor unitActor, float x, float y) {
		NoAddReason reason = building.getNoAddReason(unitActor.unit);
//		if (NoAddReason.NONE != reason) {
//			System.out.println("can not add unit to " + building.buildingType + " because " + reason);
//		}
		return NoAddReason.NONE == reason;
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

    private void resetProductionDesc() {
    	ProductionSummary summary = new ProductionSummary();
        colony.productionSummaryForBuilding(summary, building);
    	productionQuantityDrawModel.init(summary);
    }
}
