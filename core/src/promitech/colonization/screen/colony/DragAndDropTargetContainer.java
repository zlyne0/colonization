package promitech.colonization.screen.colony;

public interface DragAndDropTargetContainer<T> {
	public void putPayload(T payload, float x, float y);
	
	public boolean canPutPayload(T payload, float x, float y);

	public void onDragPayload(float x, float y);

	public void onLeaveDragPayload();
}
