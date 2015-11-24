package promitech.colonization.actors.colony;

import java.util.List;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;

import net.sf.freecol.common.model.Building;
import net.sf.freecol.common.model.Colony;
import promitech.colonization.ui.DoubleClickedListener;

class BuildingsPanelActor extends Table {
    private final ChangeColonyStateListener changeColonyStateListener;
    private final DoubleClickedListener unitActorDoubleClickListener;
    
    BuildingsPanelActor(ChangeColonyStateListener changeColonyStateListener, DoubleClickedListener unitActorDoubleClickListener) {
        this.changeColonyStateListener = changeColonyStateListener;
        this.unitActorDoubleClickListener = unitActorDoubleClickListener;
    }
    
    void initBuildings(Colony colony, DragAndDrop dragAndDrop) {
        clear();
        
        List<Building> buildings = colony.buildings.sortedEntities();
        
        int i = 0;
        for (Building building : buildings) {
            BuildingActor buildingActor = new BuildingActor(colony, building, changeColonyStateListener, unitActorDoubleClickListener);
            buildingActor.initWorkers(dragAndDrop);
            dragAndDrop.addTarget(new UnitDragAndDropTarget(buildingActor, buildingActor));
            
            add(buildingActor);
            i++;
            if (i % 4 == 0) {
                row();
            }
        }
    }
    
    void updateProductionDesc() {
        for (Actor child : getChildren()) {
            if (child instanceof BuildingActor) {
                BuildingActor ba = (BuildingActor)child;
                ba.updateProductionDesc();
            }
        }
    }
    
}
