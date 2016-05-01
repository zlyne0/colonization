package net.sf.freecol.common.model;

import java.util.Comparator;

public abstract class ObjectWithId implements Identifiable {
    public static final Comparator<ObjectWithId> INSERT_ORDER_ASC_COMPARATOR = new Comparator<ObjectWithId>() {
        @Override
        public int compare(ObjectWithId o1, ObjectWithId o2) {
            return o1.insertOrder - o2.insertOrder;
        }
    };
    
    public static final int INFINITY = Integer.MAX_VALUE;
    public static final int UNDEFINED = Integer.MIN_VALUE;
    
	protected final String id;
	private int insertOrder = 0;

	public ObjectWithId(String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}

	public boolean equalsId(String id) {
		return this.id.equals(id);
	}
	
	public boolean equalsId(Identifiable obj) {
		return obj != null && id.equals(obj.getId());
	}

    public boolean notEqualsId(Identifiable obj) {
    	return !id.equals(obj.getId());
    }
	
	public int getInsertOrder() {
		return insertOrder;
	}

	public void setInsertOrder(int insertOrder) {
		this.insertOrder = insertOrder;
	}

	public String toString() {
	    return id;
	}
}
