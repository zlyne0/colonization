package promitech.colonization.actors.colony;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;

import net.sf.freecol.common.model.Building;
import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.ObjectWithId;

class BuildingsPanelActor extends Table {
    BuildingsPanelActor() {
    }
    
    void initBuildings(Colony colony, DragAndDrop dragAndDrop) {
        clear();
        
        List<Building> buildings = new ArrayList<Building>(colony.buildings.entities());
        Collections.sort(buildings, ObjectWithId.INSERT_ORDER_ASC_COMPARATOR);
        
        int i = 0;
        for (Building building : buildings) {
            BuildingActor buildingActor = new BuildingActor(colony, building);
            buildingActor.initWorkers(dragAndDrop);
            dragAndDrop.addTarget(new BuildingDragAndDropTarget(buildingActor));
            
            add(buildingActor);
            i++;
            if (i % 4 == 0) {
                row();
            }
        }
    }
    
}
