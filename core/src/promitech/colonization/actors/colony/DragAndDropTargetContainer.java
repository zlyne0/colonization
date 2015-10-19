package promitech.colonization.actors.colony;

public interface DragAndDropTargetContainer {
	public void putPayload(UnitActor payload, float x, float y);
	
	public boolean canPutPayload(UnitActor unitActor, float x, float y);
}
