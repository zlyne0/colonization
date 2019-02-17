package net.sf.freecol.common.model;

public interface UnitLocation {

    void addUnit(Unit unit);
    
    void removeUnit(Unit unit);
    
    MapIdEntitiesReadOnly<Unit> getUnits();
	
    /**
     * Should be implemented in moveService
     * @return
     */
    @Deprecated
	boolean canAutoLoadUnit();
}
