package promitech.colonization.screen.colony;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import net.sf.freecol.common.model.Building;
import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.ProductionConsumption;
import net.sf.freecol.common.model.Unit;
import promitech.colonization.GameResources;
import promitech.colonization.gdx.Frame;
import promitech.colonization.screen.ui.ChangeColonyStateListener;
import promitech.colonization.screen.ui.UnitActor;
import promitech.colonization.screen.ui.UnitDragAndDropSource;
import promitech.colonization.ui.DoubleClickedListener;

class BuildingActor extends ImageButton implements DragAndDropSourceContainer<UnitActor>, DragAndDropTargetContainer<UnitActor> {
	private static final Color DRAG_UNIT_FOCUS_COLOR = new Color(1f, 1f, 1f, 0.3f);
	
	final Colony colony;
    final Building building;
    private final ProductionQuantityDrawModel productionQuantityDrawModel = new ProductionQuantityDrawModel();
    private ProductionQuantityDrawer productionQuantityDrawer;
    private final ChangeColonyStateListener changeColonyStateListener;
    private final DoubleClickedListener unitActorDoubleClickListener;
    private final ShapeRenderer shapeRenderer;
	private boolean showDragPayloadFocus = false;

    private static TextureRegionDrawable getBuildingTexture(Building building) {
    	Frame img = GameResources.instance.buildingTypeImage(building.buildingType);
    	return new TextureRegionDrawable(img.texture);
    }
    
    BuildingActor(ShapeRenderer shapeRenderer, Colony colony, Building building, ChangeColonyStateListener changeColonyStateListener, DoubleClickedListener unitActorDoubleClickListener) {
        super(getBuildingTexture(building));
        this.shapeRenderer = shapeRenderer;
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

        if (showDragPayloadFocus) {
        	drawDragFocus(batch);
        }
    }

	private void drawDragFocus(Batch batch) {
		batch.end();
		
		shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
		shapeRenderer.setTransformMatrix(batch.getTransformMatrix());
		
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		
		shapeRenderer.begin(ShapeType.Filled);
		shapeRenderer.setColor(DRAG_UNIT_FOCUS_COLOR);
		shapeRenderer.rect(getX(), getY(), getWidth(), getHeight());
		shapeRenderer.end();
		
		batch.begin();
	}
    
    void initWorkers(DragAndDrop dragAndDrop) {
        int offsetX = 0; 
        for (Unit worker : building.getUnits().entities()) {
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
		
		unitActor.unit.removeFromLocation();
		removeActor(unitActor);
		resetUnitActorPlacement();
		colony.updateModelOnWorkerAllocationOrGoodsTransfer();
	}

	@Override
	public void putPayload(UnitActor unitActor, float x, float y) {
		System.out.println("put unit [" + unitActor.unit + "] to building " + building);

		unitActor.disableFocus();
		unitActor.disableUnitChip();
		unitActor.dragAndDropSourceContainer = this;
		
		unitActor.setX(0);
		unitActor.setY(0);
		
		colony.addWorkerToBuilding(building, unitActor.unit);
		colony.updateModelOnWorkerAllocationOrGoodsTransfer();
		unitActor.updateTexture();
		
		addActor(unitActor);
		resetUnitActorPlacement();
		updateProductionDesc();
		
		changeColonyStateListener.changeUnitAllocation();
	}

	@Override
	public boolean canPutPayload(UnitActor unitActor, float x, float y) {
		return building.canAddWorker(unitActor.unit);
	}

	@Override
	public void onDragPayload(float x, float y) {
		showDragPayloadFocus = true;
	}

	@Override
	public void onLeaveDragPayload() {
		showDragPayloadFocus = false;
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
    
    public String toString() {
    	return "BuildingActor " + building.getId() + " " + super.toString();
    }
}
