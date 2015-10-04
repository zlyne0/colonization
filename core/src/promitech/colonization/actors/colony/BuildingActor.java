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
import promitech.colonization.GameResources;
import promitech.colonization.gdx.Frame;

class BuildingActor extends ImageButton {
	
	final Colony colony;
    final Building building;
    private final ProductionQuantityDrawModel productionQuantityDrawModel = new ProductionQuantityDrawModel();
    private ProductionQuantityDrawer productionQuantityDrawer;

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
            
            dragAndDrop.addSource(new UnitDragAndDropSource(unitActor));
            
            unitActor.moveBy(offsetX, 0);
            offsetX += unitActor.getWidth();
        }
        resetProductionDesc();
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

    private void resetProductionDesc() {
    	ProductionSummary productionSummary = colony.productionSummaryForBuilding(building);
    	productionQuantityDrawModel.init(productionSummary);
    }
}
