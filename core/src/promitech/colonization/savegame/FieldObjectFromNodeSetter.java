package promitech.colonization.savegame;

import java.lang.reflect.Field;
import java.util.List;

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
	public List<R> get(T source) {
		throw new RuntimeException("not implemented");
	}
    
}
