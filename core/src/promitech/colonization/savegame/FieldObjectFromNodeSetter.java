package promitech.colonization.savegame;

import java.io.IOException;
import java.lang.reflect.Field;

public class FieldObjectFromNodeSetter<T,R> implements ObjectFromNodeSetter<T,R> {
    private String targetFieldName;
    
    public FieldObjectFromNodeSetter(String targetFieldName) {
        this.targetFieldName = targetFieldName;
    }
    
    @Override
    public void set(T target, R entity) {
        try {
            Field field = target.getClass().getDeclaredField(targetFieldName);
            field.setAccessible(true);
            field.set(target, entity);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

	@Override
	public void generateXml(T source, ChildObject2XmlCustomeHandler<R> xmlGenerator) throws IOException {
		throw new RuntimeException("not implemented");
	}
    
}
