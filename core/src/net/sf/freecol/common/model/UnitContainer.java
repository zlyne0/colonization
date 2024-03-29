package net.sf.freecol.common.model;

import net.sf.freecol.common.model.Unit.UnitState;
import net.sf.freecol.common.model.player.Player;

public class UnitContainer {
	public static enum NoAddReason {
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
	};

	private MapIdEntities<Unit> units = new MapIdEntities<Unit>();
	
    public boolean canAdd(Unit containerUnit, Player unitOwner, UnitType unitType) {
		NoAddReason reason = (unitType == null)
			? NoAddReason.WRONG_TYPE
			: (units.isNotEmpty() && units.first().getOwner().notEqualsId(unitOwner))
			? NoAddReason.OCCUPIED_BY_ENEMY
			: (!containerUnit.hasSpaceForAdditionalUnit(unitType))
			? NoAddReason.CAPACITY_EXCEEDED
			: NoAddReason.NONE;
        return reason == NoAddReason.NONE;
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
    
    public void setStateToAllChildren(UnitState state) {
        for (Unit u : units.entities()) {
            u.setState(state);
        }
    }

    public boolean isNotEmpty() {
    	return units.isNotEmpty();
    }

    public boolean isContainUnit(Unit unit) {
    	return units.containsId(unit);
	}

    public MapIdEntities<Unit> getUnits() {
        return units;
    }

	public boolean hasUnitWithMovePoints() {
		if (units.isEmpty()) {
			return false;
		}
		for (Unit u : units.entities()) {
			if (u.hasMovesPoints()) {
				return true;
			}
		}
		return false;
	}

	public void clear() {
		units.clear();
	}
}
