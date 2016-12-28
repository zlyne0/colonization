package promitech.colonization.actors.colony;

public interface DragAndDropPreHandlerTargetContainer<T> {

	boolean isPrePutPayload(T payload, float x, float y);

	void prePutPayload(T payload, float x, float y, DragAndDropSourceContainer<T> sourceContainer);
}
