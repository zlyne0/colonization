package promitech.colonization.actors.colony;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Target;

import promitech.colonization.Validation;

class UnitDragAndDropTarget extends Target {

	private DragAndDropTargetContainer<UnitActor> targetContainer;
	
	public UnitDragAndDropTarget(Actor actor, DragAndDropTargetContainer<UnitActor> actorContainer) {
		super(actor);
		this.targetContainer = actorContainer;
	}

	@Override
	public boolean drag(Source source, Payload payload, float x, float y, int pointer) {
    	Validation.instanceOf(source.getActor(), UnitActor.class);
    	return targetContainer.canPutPayload((UnitActor)source.getActor(), x, y);
	}

	@Override
	public void drop(Source source, Payload payload, float x, float y, int pointer) {
    	Validation.instanceOf(source.getActor(), UnitActor.class);
		
		UnitActor actor = (UnitActor)source.getActor();
		DragAndDropSourceContainer<UnitActor> sourceContainer = actor.dragAndDropSourceContainer;
		sourceContainer.takePayload(actor, x, y);
		targetContainer.putPayload(actor, x, y);
	}

}
