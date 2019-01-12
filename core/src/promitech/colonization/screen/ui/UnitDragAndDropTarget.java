package promitech.colonization.screen.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Target;

import promitech.colonization.Validation;
import promitech.colonization.screen.colony.DragAndDropPreHandlerTargetContainer;
import promitech.colonization.screen.colony.DragAndDropSourceContainer;
import promitech.colonization.screen.colony.DragAndDropTargetContainer;

public class UnitDragAndDropTarget extends Target {

	private DragAndDropTargetContainer<UnitActor> targetContainer;
	
	public UnitDragAndDropTarget(Actor actor, DragAndDropTargetContainer<UnitActor> actorContainer) {
		super(actor);
		this.targetContainer = actorContainer;
	}

	@Override
	public boolean drag(Source source, Payload payload, float x, float y, int pointer) {
    	Validation.instanceOf(source.getActor(), UnitActor.class);
    	
    	if (targetContainer.canPutPayload((UnitActor)source.getActor(), x, y)) {
    		targetContainer.onDragPayload(x, y);
    		return true;
    	}
    	return false;
	}

	@Override
	public void reset(Source source, Payload payload) {
		targetContainer.onLeaveDragPayload();
	}
	
	@Override
	public void drop(Source source, Payload payload, float x, float y, int pointer) {
    	Validation.instanceOf(source.getActor(), UnitActor.class);
		
		UnitActor actor = (UnitActor)source.getActor();
		DragAndDropSourceContainer<UnitActor> sourceContainer = actor.dragAndDropSourceContainer;
		
		boolean regularTakePut = false;
		if (targetContainer instanceof DragAndDropPreHandlerTargetContainer) {
			@SuppressWarnings("unchecked")
			DragAndDropPreHandlerTargetContainer<UnitActor> preTargetHandler = (DragAndDropPreHandlerTargetContainer<UnitActor>)targetContainer;
			
			if (preTargetHandler.isPrePutPayload(actor, x, y)) {
				preTargetHandler.prePutPayload(actor, x, y, sourceContainer);
			} else {
				regularTakePut = true;
			}
		} else {
			regularTakePut = true;
		}
		if (regularTakePut) {
			sourceContainer.takePayload(actor, x, y);
			targetContainer.putPayload(actor, x, y);
			
		}
	}

}
