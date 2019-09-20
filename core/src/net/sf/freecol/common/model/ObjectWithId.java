package net.sf.freecol.common.model;

import java.util.Locale;

import net.sf.freecol.common.util.StringUtils;

public abstract class ObjectWithId implements Identifiable {
    public static final int INFINITY = Integer.MAX_VALUE;
    public static final int UNDEFINED = Integer.MIN_VALUE;
	public static final int UNLIMITED = -1;
    
	protected final String id;

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

	public String toString() {
	    return id;
	}
	
	public String getSuffix() {
		return StringUtils.lastPart(getId(), ".").toLowerCase(Locale.US);
	}
	
}
