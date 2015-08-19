package net.sf.freecol.common.model;

public abstract class ObjectWithId implements Identifiable {

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
	
	public boolean equalsId(ObjectWithId obj) {
		return id.equals(obj.id);
	}

    public boolean notEqualsId(ObjectWithId obj) {
    	return !id.equals(obj.id);
    }
	
	public int getInsertOrder() {
		return insertOrder;
	}

	public void setInsertOrder(int insertOrder) {
		this.insertOrder = insertOrder;
	}
	
}
