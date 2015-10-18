package promitech.colonization.actors.colony;

import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Target;

import net.sf.freecol.common.model.Building;
import net.sf.freecol.common.model.ColonyTile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitContainer.NoAddReason;

class BuildingDragAndDropTarget extends Target {
    
    public BuildingDragAndDropTarget(BuildingActor actor) {
        super(actor);
    }

    @Override
    public boolean drag(Source source, Payload payload, float x, float y, int pointer) {
    	if (source.getActor() instanceof UnitActor) {
            Unit unit = (Unit)payload.getObject();
            Building targetBuilding = ((BuildingActor)getActor()).building;
            return canMoveUnitToBuilding(unit, targetBuilding);
        } 
        if (source.getActor() instanceof TerrainPanel) {
        	ColonyTile colonyTile = (ColonyTile)payload.getObject();
        	Building targetBuilding = ((BuildingActor)getActor()).building;
        	return canMoveUnitToBuilding(colonyTile.getWorker(), targetBuilding);
        }
        return false;
    }

    private boolean canMoveUnitToBuilding(Unit worker, Building building) {
        NoAddReason reason = building.getNoAddReason(worker);
        if (NoAddReason.NONE != reason) {
            //System.out.println("can not add unit to " + building.buildingType + " because " + reason);
        }
        return NoAddReason.NONE == reason;
    }
    
    @Override
    public void drop(Source source, Payload payload, float x, float y, int pointer) {
        if (source.getActor() instanceof UnitActor) {
            UnitActor unitActor = (UnitActor)source.getActor();
            
            System.out.println("drop ActorUnit from container " + source.getActor().getParent());
            
            if (source.getActor().getParent() instanceof BuildingActor) {
                BuildingActor sourceBuildingActor = (BuildingActor)source.getActor().getParent();
                
                BuildingActor targetBuildingActor = (BuildingActor)getActor();
                System.out.println("move unit "
                    + "[" + unitActor.unit + "] from "
                    + "[" + sourceBuildingActor.building + "] to "
                    + "[" + targetBuildingActor.building + "]");
                
                sourceBuildingActor.takeUnit(unitActor);
                targetBuildingActor.putUnit(unitActor);
            }
            if (source.getActor().getParent() instanceof TerrainPanel) {
            	TerrainPanel sourceTerrainPanel = (TerrainPanel)source.getActor().getParent();
            	BuildingActor targetBuilding = (BuildingActor)getActor();
            	
            	sourceTerrainPanel.takeWorker(unitActor);
            	targetBuilding.putUnit(unitActor);
            }
            if (source.getActor().getParent() instanceof OutsideUnitsPanel) {
            	OutsideUnitsPanel sourceContainer = (OutsideUnitsPanel)source.getActor().getParent();
            	BuildingActor targetContainer = (BuildingActor)getActor();
            	
            	sourceContainer.takeUnit(unitActor);
            	targetContainer.putUnit(unitActor);
            }
        }
    }
}
