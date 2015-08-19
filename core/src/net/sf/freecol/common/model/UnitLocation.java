package net.sf.freecol.common.model;

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

class UnitLocation {
	private MapIdEntities<Unit> units = new MapIdEntities<Unit>();
	private int spaceTaken = 0;
	
    public boolean canAdd(Unit unit) {
        return getNoAddReason(unit) == NoAddReason.NONE;
    }
	
    public NoAddReason getNoAddReason(Unit unit) {
        return (unit == null)
            ? NoAddReason.WRONG_TYPE
            : (units.isNotEmpty() && units.first().getOwner().notEqualsId(unit.getOwner()))
            ? NoAddReason.OCCUPIED_BY_ENEMY
            : (units.containsId(unit))
            ? NoAddReason.ALREADY_PRESENT
            : (unit.getSpaceTaken() + spaceTaken > getUnitCapacity())
            ? NoAddReason.CAPACITY_EXCEEDED
            : NoAddReason.NONE;
    }
    
    public int getUnitCapacity() {
        return Integer.MAX_VALUE; // ;-)
    }
}
