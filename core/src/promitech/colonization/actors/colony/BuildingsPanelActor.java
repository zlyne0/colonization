package promitech.colonization.actors.colony;

import java.util.List;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;

import net.sf.freecol.common.model.Building;
import net.sf.freecol.common.model.Colony;

class BuildingsPanelActor extends Table {
    private final ChangeColonyStateListener changeColonyStateListener;
    
    BuildingsPanelActor(ChangeColonyStateListener changeColonyStateListener) {
        this.changeColonyStateListener = changeColonyStateListener;
    }
    
    void initBuildings(Colony colony, DragAndDrop dragAndDrop) {
        clear();
        
        List<Building> buildings = colony.buildings.sortedEntities();
        
        int i = 0;
        for (Building building : buildings) {
            BuildingActor buildingActor = new BuildingActor(colony, building, changeColonyStateListener);
            buildingActor.initWorkers(dragAndDrop);
            dragAndDrop.addTarget(new UnitDragAndDropTarget(buildingActor, buildingActor));
            
            add(buildingActor);
            i++;
            if (i % 4 == 0) {
                row();
            }
        }
    }
    
}
