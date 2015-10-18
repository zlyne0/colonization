package promitech.colonization.actors.colony;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload;

class UnitDragAndDropSource extends com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source {
    public UnitDragAndDropSource(UnitActor actor) {
        super(actor);
    }

    @Override
    public Payload dragStart(InputEvent event, float x, float y, int pointer) {
        UnitActor unitActor = (UnitActor)getActor();
        
        Image dragImg = new Image(unitActor.getTexture());
        
        Payload payload = new Payload();
        payload.setObject(unitActor.unit);
        payload.setDragActor(dragImg);
        payload.setInvalidDragActor(dragImg);
        payload.setValidDragActor(dragImg);
        return payload;
    }
}
