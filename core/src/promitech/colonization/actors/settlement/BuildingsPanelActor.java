package promitech.colonization.actors.settlement;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Target;

import net.sf.freecol.common.model.Building;
import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitContainer.NoAddReason;
import promitech.colonization.GUIGameController;
import promitech.colonization.infrastructure.FontResource;

class BuildingDragAndDropTarget extends Target {

	private final BuildingActor targetActor;
	
	public BuildingDragAndDropTarget(BuildingActor actor) {
		super(actor);
		this.targetActor = actor;
	}

	@Override
	public boolean drag(Source source, Payload payload, float x, float y, int pointer) {		
		if (payload.getObject() instanceof Unit) {
			Unit unit = (Unit)payload.getObject();
			NoAddReason reason = targetActor.building.getNoAddReason(unit);
			if (reason != NoAddReason.NONE) {
				System.out.println("can not add unit to building: " + ("noAddReason." + reason.toString()));
				return false;
			} else {
				return true;
			}
		} 
		return false;
	}

	@Override
	public void drop(Source source, Payload payload, float x, float y, int pointer) {
		System.out.println("targetActor.building.buildingType " + targetActor.building.buildingType);
	}
	
}

class BuildingsPanelActor extends Window {

    public static class MyWindowStyle extends WindowStyle {
        MyWindowStyle() {
            titleFont = FontResource.getUnitBoxFont();
            titleFontColor = Color.GREEN; 
        }
    }
    
    BuildingsPanelActor(GUIGameController gameController) {
        super("buildings panel", new MyWindowStyle());
        
        setFillParent(true);
//        defaults().expandX();
//        defaults().expandY();
//        defaults().fillX();
//        defaults().fillY();
        
        Tile tile = gameController.getGame().map.getTile(24, 78);
        Settlement settlement = tile.getSettlement();
        if (settlement.isColony()) {
            Colony colony = (Colony) settlement;
            int i = 0;
            
            for (Building building : colony.buildings.entities()) {
                BuildingActor buildingActor = new BuildingActor(building);
                buildingActor.initWorkers();
                
                BuildingActor.dragAndDrop.addTarget(new BuildingDragAndDropTarget(buildingActor));
                
                add(buildingActor);
                i++;
                if (i % 4 == 0) {
                    row();
                }
            }
            pack();
            
        }
    }
    
}
