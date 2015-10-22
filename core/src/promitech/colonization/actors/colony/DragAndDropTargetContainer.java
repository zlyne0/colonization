package promitech.colonization.actors.colony;

public interface DragAndDropTargetContainer<T> {
	public void putPayload(T payload, float x, float y);
	
	public boolean canPutPayload(T unitActor, float x, float y);
}
