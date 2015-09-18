package promitech.colonization.actors.colony;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;

import net.sf.freecol.common.model.Building;
import net.sf.freecol.common.model.Colony;

class BuildingsPanelActor extends Table {
    public static final Comparator<Building> BUILDINGS_COMPARATOR = new Comparator<Building>() {
        @Override
        public int compare(Building o1, Building o2) {
            return o1.buildingType.getInsertOrder() - o2.buildingType.getInsertOrder();
        }
    };
    
    BuildingsPanelActor() {
        setFillParent(true);
    }
    
    void initBuildings(Colony colony, DragAndDrop dragAndDrop) {
        clear();
        
        List<Building> buildings = new ArrayList<Building>(colony.buildings.entities());
        Collections.sort(buildings, BUILDINGS_COMPARATOR);
        
        int i = 0;
        for (Building building : buildings) {
            BuildingActor buildingActor = new BuildingActor(building);
            buildingActor.initWorkers(dragAndDrop);
            buildingActor.initProductionDesc(colony);
            dragAndDrop.addTarget(new BuildingDragAndDropTarget(buildingActor));
            
            add(buildingActor);
            i++;
            if (i % 4 == 0) {
                row();
            }
        }
    }
    
}
