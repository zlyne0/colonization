package promitech.colonization.actors.settlement;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import net.sf.freecol.common.model.Building;
import net.sf.freecol.common.model.Unit;
import promitech.colonization.GameResources;
import promitech.colonization.gdx.Frame;

class UnitActor extends Image {
    
    private final Unit unit;
    
    UnitActor(final Unit unit, GameResources gameResources) {
        super( gameResources.getFrame(unit.resourceImageKey()).texture );
        this.unit = unit;
        
        addListener(new InputListener() {
            int draggedPointer = -1;
            float orginX = 0;
            float orginY = 0;
            
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                System.out.println("touchDown " + unit);
                draggedPointer = pointer;
                orginX = x;
                orginY = y;
                return true;
            }
            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                System.out.println("touchDragged" + unit + ", xy: " + x + ", " + y);
                if (draggedPointer != -1) {
                    //setPosition(orginX + x - getWidth(), orginY + y);
                    setPosition(x, y);
                }
                super.touchDragged(event, x, y, pointer);
            }
            @Override
            public boolean mouseMoved(InputEvent event, float x, float y) {
//                if (draggedPointer != -1) {
//                    System.out.println("mouseMoved " + unit + ", xy: " + x + ", " + y + ", pointer: " + draggedPointer);
//                    setPosition(x, y);
//                    return true;
//                }
//                return false;
                return false;
            }
            
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                System.out.println("touchUp " + unit + ", xy: " + x + ", " + y);
                if (draggedPointer == pointer) {
                    draggedPointer = -1;
                }
            }
        });
    }
    
}

class BuildingActor extends ImageButton {
    private final Building building;
    
    BuildingActor(Building building, Frame frame) {
        super(new TextureRegionDrawable(frame.texture));
        this.building = building;
    }
    
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
    }
    
    void initWorkers(GameResources gameResources, BuildingsPanelActor buildingsPanelActor) {
        int offsetX = 0; 
        for (Unit worker : building.workers.entities()) {
            UnitActor unitActor = new UnitActor(worker, gameResources);
            buildingsPanelActor.add(unitActor);
            unitActor.setPosition(getX(), getY());
            unitActor.moveBy(offsetX, 0);
            offsetX += unitActor.getWidth();
        }
    }
}
