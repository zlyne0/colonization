package net.sf.freecol.common.model;

public enum MoveType {
    MOVE(null, true),
    MOVE_HIGH_SEAS(null, true),
    EXPLORE_LOST_CITY_RUMOUR(null, true),
    ATTACK_UNIT(null, false),
    ATTACK_SETTLEMENT(null, false),
    EMBARK(null, false),
    DISEMBARK(null, false),
    ENTER_INDIAN_SETTLEMENT_WITH_FREE_COLONIST(null, false),
    ENTER_INDIAN_SETTLEMENT_WITH_SCOUT(null, false),
    ENTER_INDIAN_SETTLEMENT_WITH_MISSIONARY(null, false),
    ENTER_FOREIGN_COLONY_WITH_SCOUT(null, false),
    ENTER_SETTLEMENT_WITH_CARRIER_AND_GOODS(null, false),
    MOVE_NO_MOVES("Attempt to move without moves left"),
    MOVE_NO_ACCESS_LAND("Attempt to move a naval unit onto land"),
    MOVE_NO_ACCESS_BEACHED("Attempt to move onto foreign beached ship"),
    MOVE_NO_ACCESS_EMBARK("Attempt to embark onto absent or foreign carrier"),
    MOVE_NO_ACCESS_FULL("Attempt to embark onto full carrier"),
    MOVE_NO_ACCESS_GOODS("Attempt to trade without goods"),
    MOVE_NO_ACCESS_CONTACT("Attempt to interact with natives before contact"),
    MOVE_NO_ACCESS_MISSION_BAN("Attempt to use missionary at banned settlement"),
    MOVE_NO_ACCESS_SETTLEMENT("Attempt to move into foreign settlement"),
    MOVE_NO_ACCESS_SKILL("Attempt to learn skill with incapable unit"),
    MOVE_NO_ACCESS_TRADE("Attempt to trade without authority"),
    MOVE_NO_ACCESS_WAR("Attempt to trade while at war"),
    MOVE_NO_ACCESS_WATER("Attempt to move into a settlement by water"),
    MOVE_NO_ATTACK_CIVILIAN("Attempt to attack with civilian unit"),
    MOVE_NO_ATTACK_MARINE("Attempt to attack from on board ship"),
    MOVE_NO_EUROPE("Attempt to move to Europe by incapable unit"),
    MOVE_NO_REPAIR("Attempt to move a unit that is under repair"),
    MOVE_NO_TILE("Attempt to move when not on a tile"),
    MOVE_ILLEGAL("Unspecified illegal move");

    /**
     * The reason why this move type is illegal.
     */
    private final String reason;

    /**
     * Does this move type imply progress towards a destination.
     */
    private final boolean progress;

    MoveType(String reason) {
        this.reason = reason;
        this.progress = false;
    }

    MoveType(String reason, boolean progress) {
        this.reason = reason;
        this.progress = progress;
    }

    public boolean isLegal() {
        return this.reason == null;
    }

    public String whyIllegal() {
        return (reason == null) ? "(none)" : reason;
    }

    public boolean isProgress() {
        return progress;
    }

    public boolean isAttack() {
        return this == ATTACK_UNIT || this == ATTACK_SETTLEMENT;
    }
}
