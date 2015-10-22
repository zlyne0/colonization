package promitech.colonization.actors.colony;

public interface DragAndDropSourceContainer<T> {
	public void takePayload(T payload, float x, float y);
}
