package promitech.colonization.actors.settlement;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Window;

import net.sf.freecol.common.model.Building;
import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Tile;
import promitech.colonization.GUIGameController;
import promitech.colonization.GameResources;
import promitech.colonization.gdx.Frame;
import promitech.colonization.infrastructure.FontResource;

class BuildingsPanelActor extends Window {

    public static class MyWindowStyle extends WindowStyle {
        MyWindowStyle() {
            titleFont = FontResource.getUnitBoxFont();
            titleFontColor = Color.GREEN; 
        }
    }
    
    BuildingsPanelActor(GUIGameController gameController, GameResources gameResources) {
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
            
            List<BuildingActor> buildingsActors = new ArrayList<BuildingActor>();
            
            for (Building building : colony.buildings.entities()) {
                Frame buildingTypeImage = gameResources.buildingTypeImage(building.buildingType);
                BuildingActor buildingActor = new BuildingActor(building, buildingTypeImage);
                buildingsActors.add(buildingActor);
                
                add(buildingActor);
                i++;
                if (i % 4 == 0) {
                    row();
                }
            }
            pack();
            for (BuildingActor ba : buildingsActors) {
                ba.initWorkers(gameResources, this);
            }
            
        }
    }
    
}
