package promitech.colonization.screen.colony;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;

import net.sf.freecol.common.model.Building;
import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.specification.BuildingType;

import promitech.colonization.screen.ui.ChangeColonyStateListener;
import promitech.colonization.screen.ui.UnitActor;
import promitech.colonization.screen.ui.UnitDragAndDropTarget;
import promitech.colonization.ui.DoubleClickedListener;

class BuildingsPanelActor extends Table {
    private final ChangeColonyStateListener changeColonyStateListener;
    private final DoubleClickedListener unitActorDoubleClickListener;
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    
    BuildingsPanelActor(ChangeColonyStateListener changeColonyStateListener, DoubleClickedListener unitActorDoubleClickListener) {
        this.changeColonyStateListener = changeColonyStateListener;
        this.unitActorDoubleClickListener = unitActorDoubleClickListener;
    }
    
    void initBuildings(Colony colony, DragAndDrop dragAndDrop) {
        clear();
        
        int i = 0;
        for (Building building : colony.buildings.entities()) {
            BuildingActor buildingActor = new BuildingActor(shapeRenderer, colony, building, changeColonyStateListener, unitActorDoubleClickListener);
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

	void putWorkerOnBuilding(UnitActor unitActor, BuildingType buildingType) {
        for (Actor child : getChildren()) {
            if (child instanceof BuildingActor) {
            	BuildingActor ba = (BuildingActor)child;
            	if (ba.building.buildingType.equalsId(buildingType)) {
            		ba.putPayload(unitActor, -1, -1);
            		break;
            	}
            }
        }
	}
    
}
