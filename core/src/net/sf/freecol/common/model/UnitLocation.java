package net.sf.freecol.common.model;

public interface UnitLocation {

    void addUnit(Unit unit);
    
    void removeUnit(Unit unit);
    
    MapIdEntitiesReadOnly<Unit> getUnits();
	
	boolean canAutoLoadUnit();

	boolean canAutoUnloadUnits();
}
