package promitech.colonization.actors.colony;

import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Target;

import net.sf.freecol.common.model.Building;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitContainer.NoAddReason;

class BuildingDragAndDropTarget extends Target {
    private final BuildingActor targetBuildingActor;
    
    public BuildingDragAndDropTarget(BuildingActor actor) {
        super(actor);
        this.targetBuildingActor = actor;
    }

    @Override
    public boolean drag(Source source, Payload payload, float x, float y, int pointer) {        
        if (payload.getObject() instanceof Unit) {
            Unit unit = (Unit)payload.getObject();
            Building building = targetBuildingActor.building;
            
            NoAddReason reason = building.getNoAddReason(unit);
            if (NoAddReason.NONE != reason) {
                System.out.println("can not add unit to " + building.buildingType + " because " + reason);
            }
            return NoAddReason.NONE == reason;
        }
        return false;
    }

    @Override
    public void drop(Source source, Payload payload, float x, float y, int pointer) {
        if (source.getActor() instanceof UnitActor) {
            UnitActor unitActor = (UnitActor)source.getActor();
            if (source.getActor().getParent() instanceof BuildingActor) {
                BuildingActor sourceBuildingActor = (BuildingActor)source.getActor().getParent();
                
                System.out.println("move unit "
                    + "[" + unitActor.unit + "] from "
                    + "[" + sourceBuildingActor.building + "] to "
                    + "[" + targetBuildingActor.building + "]");
                
                sourceBuildingActor.takeUnit(unitActor);
                targetBuildingActor.putUnit(unitActor);
            }
        }
    }
}
