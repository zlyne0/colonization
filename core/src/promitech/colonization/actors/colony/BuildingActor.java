package promitech.colonization.actors.colony;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import net.sf.freecol.common.model.Building;
import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.ProductionConsumption;
import net.sf.freecol.common.model.Unit;
import promitech.colonization.GameResources;
import promitech.colonization.actors.ChangeColonyStateListener;
import promitech.colonization.actors.UnitActor;
import promitech.colonization.actors.UnitDragAndDropSource;
import promitech.colonization.gdx.Frame;
import promitech.colonization.ui.DoubleClickedListener;

class BuildingActor extends ImageButton implements DragAndDropSourceContainer<UnitActor>, DragAndDropTargetContainer<UnitActor> {
	
	final Colony colony;
    final Building building;
    private final ProductionQuantityDrawModel productionQuantityDrawModel = new ProductionQuantityDrawModel();
    private ProductionQuantityDrawer productionQuantityDrawer;
    private final ChangeColonyStateListener changeColonyStateListener;
    private final DoubleClickedListener unitActorDoubleClickListener;

    private static TextureRegionDrawable getBuildingTexture(Building building) {
    	Frame img = GameResources.instance.buildingTypeImage(building.buildingType);
    	return new TextureRegionDrawable(img.texture);
    }
    
    BuildingActor(Colony colony, Building building, ChangeColonyStateListener changeColonyStateListener, DoubleClickedListener unitActorDoubleClickListener) {
        super(getBuildingTexture(building));
        this.changeColonyStateListener = changeColonyStateListener;
        this.unitActorDoubleClickListener = unitActorDoubleClickListener;
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
            UnitActor unitActor = new UnitActor(worker, unitActorDoubleClickListener);
            addActor(unitActor);
            unitActor.dragAndDropSourceContainer = this;
            
            dragAndDrop.addSource(new UnitDragAndDropSource(unitActor));
            
            unitActor.moveBy(offsetX, 0);
            offsetX += unitActor.getWidth();
        }
        updateProductionDesc();
    }

	@Override
	public void takePayload(UnitActor unitActor, float x, float y) {
		System.out.println("take unit [" + unitActor.unit + "] from building " + building);
		
		unitActor.dragAndDropSourceContainer = null;
		
		building.workers.removeId(unitActor.unit);
		removeActor(unitActor);
		resetUnitActorPlacement();
		colony.updateModelOnWorkerAllocationOrGoodsTransfer();
	}

	@Override
	public void putPayload(UnitActor unitActor, float x, float y) {
		System.out.println("put unit [" + unitActor.unit + "] to building " + building);

		unitActor.dragAndDropSourceContainer = this;
		
		unitActor.setX(0);
		unitActor.setY(0);
		
		colony.addWorkerToBuilding(building, unitActor.unit);
		unitActor.updateTexture();
		
		addActor(unitActor);
		resetUnitActorPlacement();
		colony.updateModelOnWorkerAllocationOrGoodsTransfer();
		updateProductionDesc();
		
		changeColonyStateListener.changeUnitAllocation();
	}

	@Override
	public boolean canPutPayload(UnitActor unitActor, float x, float y) {
		return building.canAddWorker(unitActor.unit);
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

    void updateProductionDesc() {
    	ProductionConsumption productionSummary = colony.productionSummary(building);
    	productionQuantityDrawModel.init(productionSummary.realProduction);
    }
}
