package promitech.colonization.savegame;

import java.io.IOException;
import java.util.Collection;

public interface ObjectFromNodeSetter<T,R> {
	
	interface ChildObject2XmlCustomeHandler<R> {
		public void generateXml(R obj) throws IOException;
		public void generateXmlFromCollection(Collection<R> objs) throws IOException;
	}
	
    public void set(T target, R entity);
    public void generateXml(T source, ChildObject2XmlCustomeHandler<R> xmlGenerator) throws IOException;
}
