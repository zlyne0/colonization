package net.sf.freecol.common.model;

import java.util.Collection;

import net.sf.freecol.common.model.Unit.UnitState;

enum NoAddReason {
    /**
     * No reason why Locatable can not be added.
     */
    NONE,
    /**
     * Unit is already in the location.
     */
    ALREADY_PRESENT,
    /**
     * Locatable can not be added because it has the wrong
     * type. E.g. a {@link Building} can not be added to a
     * {@link Unit}.
     */
    WRONG_TYPE,
    /**
     * Locatable can not be added because the Location is already
     * full.
     */
    CAPACITY_EXCEEDED,
    /**
     * Locatable can not be added because the Location is
     * occupied by objects belonging to another player.
     */
    OCCUPIED_BY_ENEMY,
    /**
     * Locatable can not be added because the Location belongs
     * to another player and does not admit foreign objects.
     */
    OWNED_BY_ENEMY,
    // Enums can not be extended, so ColonyTile-specific failure reasons
    // have to be here.
    /**
     * Claimed and in use by another of our colonies.
     */
    ANOTHER_COLONY,
    /**
     * Can not add to settlement center tile.
     */
    COLONY_CENTER,
    /**
     * Missing ability to work colony tile or building.
     * Currently only produceInWater, which is assumed by the error message
     */
    MISSING_ABILITY,
    /**
     * The unit has no skill.
     */
    MISSING_SKILL,
    /**
     * The unit does not have the minimum skill required.
     */
    MINIMUM_SKILL,
    /**
     * The unit exceeds the maximum skill of this type.
     */
    MAXIMUM_SKILL,
    /**
     * Either unclaimed or claimed but could be acquired.
     */
    CLAIM_REQUIRED,
}

public class UnitContainer {
	private MapIdEntities<Unit> units = new MapIdEntities<Unit>();
	
	private final Unit containerUnit;
	
	public UnitContainer(Unit containerUnit) {
		this.containerUnit = containerUnit;
	}
	
    public boolean canAdd(Unit unit) {
        return getNoAddReason(unit) == NoAddReason.NONE;
    }
	
    public void addUnit(Unit unit) {
        this.units.add(unit);
    }
    
    int getSpaceTakenByUnits() {
        int space = 0;
    	for (Unit u : units.entities()) {
    		space += u.unitType.getSpaceTaken();
    	}
        return space;
    }
    
    public NoAddReason getNoAddReason(Unit unit) {
        return (unit == null)
            ? NoAddReason.WRONG_TYPE
            : (units.isNotEmpty() && units.first().getOwner().notEqualsId(unit.getOwner()))
            ? NoAddReason.OCCUPIED_BY_ENEMY
            : (units.containsId(unit))
            ? NoAddReason.ALREADY_PRESENT
            : (unit.unitType.getSpaceTaken() + containerUnit.getSpaceTaken() > containerUnit.unitType.getSpace())
            ? NoAddReason.CAPACITY_EXCEEDED
            : NoAddReason.NONE;
    }

    public void setStateToAllChildren(UnitState state) {
        for (Unit u : units.entities()) {
            u.setState(state);
        }
    }

    public MapIdEntities<Unit> getUnits() {
        return units;
    }
}
