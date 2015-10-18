package promitech.colonization.actors.colony;

import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Target;

public class OutsideUnitsDragAndDropTarget extends Target {

	public OutsideUnitsDragAndDropTarget(OutsideUnitsPanel outsideUnitsPanel) {
		super(outsideUnitsPanel);
	}

	@Override
	public boolean drag(Source source, Payload payload, float x, float y, int pointer) {
		return true;
	}

	@Override
	public void drop(Source source, Payload payload, float x, float y, int pointer) {
        if (source.getActor() instanceof UnitActor) {
            UnitActor unitActor = (UnitActor)source.getActor();
            if (source.getActor().getParent() instanceof BuildingActor) {
                BuildingActor sourceBuildingActor = (BuildingActor)source.getActor().getParent();
                sourceBuildingActor.takeUnit(unitActor);
                
				System.out.println("move unit " 
					+ "[" + unitActor.unit + "] from " 
					+ "[" + sourceBuildingActor.building + "] to "
					+ "[ outside units panel ]"
				);
                
                OutsideUnitsPanel targetOutsideUnitsPanel = (OutsideUnitsPanel)getActor();
                targetOutsideUnitsPanel.putUnit(unitActor);
            }
            
            if (source.getActor().getParent() instanceof TerrainPanel) {
            	TerrainPanel sourceTerrainPanel = (TerrainPanel)source.getActor().getParent();

                OutsideUnitsPanel targetOutsideUnitsPanel = (OutsideUnitsPanel)getActor();
                sourceTerrainPanel.takeWorker(unitActor);
                targetOutsideUnitsPanel.putUnit(unitActor);
            }
        }
	}

}
