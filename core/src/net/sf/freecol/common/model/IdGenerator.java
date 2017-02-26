package net.sf.freecol.common.model;

import java.lang.reflect.Method;

public class IdGenerator {
	protected int idSequence;
	
	public IdGenerator(int initId) {
		idSequence = initId;
	}
	
	public String nextId(Class<? extends Identifiable> entityClass) {
		idSequence++;
		return tagName(entityClass) + ":" + Integer.toString(idSequence);
	}
	
	private String tagName(Class<? extends Identifiable> entityClass) {
		try {
			Class xmlClazz = getXmlClassFromEntityClass(entityClass);
	        Method tagNameMethod = xmlClazz.getDeclaredMethod("tagName");
	        return (String)tagNameMethod.invoke(null);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
    protected Class getXmlClassFromEntityClass(Class<? extends Identifiable> entityClass) {
    	for (Class cz : entityClass.getDeclaredClasses()) {
    		if (cz.getSimpleName().equals("Xml")) {
    			return cz;
    		}
    	}
        throw new IllegalStateException("can not find inner Xml class for " + entityClass);
    }
	
}
