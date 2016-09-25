package net.sf.freecol.common.model;

public interface UnitLocation {

	MapIdEntities<Unit> getUnits();
	
	boolean canAutoLoadUnit();

	boolean canAutoUnloadUnits();
}
