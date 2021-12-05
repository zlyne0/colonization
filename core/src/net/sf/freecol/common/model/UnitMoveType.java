package net.sf.freecol.common.model;

import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.GameOptions;
import net.sf.freecol.common.model.specification.UnitTypeChange;

import promitech.colonization.Direction;

public class UnitMoveType {

    private Player owner;
    private UnitType unitType;
    private UnitRole unitRole;
    private int hitPoints;
    private UnitContainer unitContainer = null;
    private GoodsContainer goodsContainer = null;

    private UnitContainer emptyUnitContainer = null;
    private GoodsContainer emptyGoodsContainer = null;

    public void reset() {
        this.owner = null;
        this.unitType = null;
        this.unitRole = null;
        this.hitPoints = 0;
        this.unitContainer = null;
        this.goodsContainer = null;
    }

    public UnitMoveType init(Unit unit) {
        this.owner = unit.getOwner();
        this.unitType = unit.unitType;
        this.unitRole = unit.unitRole;
        this.hitPoints = unit.getHitPoints();
        this.unitContainer = unit.getUnitContainer();
        this.goodsContainer = unit.getGoodsContainer();
        return this;
    }

    public UnitMoveType init(Player owner, UnitType unitType) {
        this.owner = owner;
        this.unitType = unitType;
        this.unitRole = Specification.instance.unitRoles.getById(UnitRole.DEFAULT_ROLE_ID);
        this.hitPoints = unitType.getHitPoints();

        if (unitType.canCarryUnits()) {
            if (emptyUnitContainer == null) {
                emptyUnitContainer = new UnitContainer();
            } else {
                emptyUnitContainer.clear();
            }
            unitContainer = emptyUnitContainer;
        }
        if (unitType.hasAbility(Ability.CARRY_GOODS)) {
            if (emptyGoodsContainer == null) {
                emptyGoodsContainer = new GoodsContainer();
            } else {
                emptyGoodsContainer.clear();
            }
            goodsContainer = emptyGoodsContainer;
        }
        return this;
    }

    public MoveType calculateMoveType(Unit unit, Tile from, Tile target) {
        init(unit);
        return calculateMoveType(from, target);
    }

    public MoveType calculateMoveType(Tile from, Tile target) {
        if (from == null || target == null) {
            return MoveType.MOVE_NO_TILE;
        }
        if (unitType.isNaval()) {
            return getNavalMoveType(target);
        } else {
            return getLandMoveType(from, target);
        }
    }

    private MoveType getNavalMoveType(Tile target) {
        if (target == null) {
            return (owner.canMoveToEurope()) ? MoveType.MOVE_HIGH_SEAS : MoveType.MOVE_NO_EUROPE;
        }
        if (isDamaged()) {
            return MoveType.MOVE_NO_REPAIR;
        }

        if (target.getType().isLand()) {
            Settlement settlement = target.getSettlement();
            if (settlement == null) {
                if (target.hasLostCityRumour()) {
                    return MoveType.MOVE_NO_ACCESS_LAND;
                }
                if (unitContainer != null && unitContainer.hasUnitWithMovePoints()) {
                    if (!hasTileEnemyUnits(target)) {
                        return MoveType.DISEMBARK;
                    }
                }
                return MoveType.MOVE_NO_ACCESS_LAND;
            } else if (settlement.getOwner().equalsId(owner)) {
                return MoveType.MOVE;
            } else if (isTradingUnit()) {
                return getTradeMoveType(settlement);
            } else {
                return MoveType.MOVE_NO_ACCESS_SETTLEMENT;
            }
        } else { // target at sea
            if (hasTileEnemyUnits(target)) {
                return (Unit.isOffensiveUnit(unitType, unitRole)) ? MoveType.ATTACK_UNIT : MoveType.MOVE_NO_ATTACK_CIVILIAN;
            }
            return (target.isDirectlyHighSeasConnected()) ? MoveType.MOVE_HIGH_SEAS : MoveType.MOVE;
        }
    }

    private MoveType getLandMoveType(Tile from, Tile target) {
        if (target == null) {
            return MoveType.MOVE_ILLEGAL;
        }

        Unit defender = target.getUnits().first();

        if (target.getType().isLand()) {
            Settlement settlement = target.getSettlement();
            if (settlement == null) {
                if (defender != null && owner.notEqualsId(defender.getOwner())) {
                    if (defender.isNaval()) {
                        return MoveType.ATTACK_UNIT;
                    } else if (!Unit.isOffensiveUnit(unitType, unitRole)) {
                        return MoveType.MOVE_NO_ATTACK_CIVILIAN;
                    } else {
                        return (allowMoveFrom(from)) ? MoveType.ATTACK_UNIT : MoveType.MOVE_NO_ATTACK_MARINE;
                    }
                } else if (target.hasLostCityRumour() && owner.nationType().isEuropean()) {
                    // Natives do not explore rumours, see:
                    // server/control/InGameInputHandler.java:move()
                    return MoveType.EXPLORE_LOST_CITY_RUMOUR;
                } else {
                    return MoveType.MOVE;
                }
            } else if (owner.equalsId(settlement.getOwner())) {
                if (hasAbility(Ability.CARRY_TREASURE) && Unit.canCashInTreasureInLocation(owner, target)) {
                    return MoveType.MOVE_CASH_IN_TREASURE;
                }
                return MoveType.MOVE;
            } else if (isTradingUnit()) {
                return getTradeMoveType(settlement);
            } else if (Unit.isColonist(unitType, owner)) {
                if (settlement instanceof Colony && hasAbility(Ability.NEGOTIATE)) {
                    return (allowMoveFrom(from)) ? MoveType.ENTER_FOREIGN_COLONY_WITH_SCOUT : MoveType.MOVE_NO_ACCESS_WATER;
                } else if (settlement instanceof IndianSettlement && hasAbility(Ability.SPEAK_WITH_CHIEF)) {
                    return (allowMoveFrom(from)) ? MoveType.ENTER_INDIAN_SETTLEMENT_WITH_SCOUT : MoveType.MOVE_NO_ACCESS_WATER;
                } else if (Unit.isOffensiveUnit(unitType, unitRole)) {
                    return (allowMoveFrom(from)) ? MoveType.ATTACK_SETTLEMENT : MoveType.MOVE_NO_ATTACK_MARINE;
                } else if (hasAbility(Ability.ESTABLISH_MISSION)) {
                    return getMissionaryMoveType(from, settlement);
                } else {
                    return getLearnMoveType(from, settlement);
                }
            } else if (Unit.isOffensiveUnit(unitType, unitRole)) {
                return (allowMoveFrom(from)) ? MoveType.ATTACK_SETTLEMENT : MoveType.MOVE_NO_ATTACK_MARINE;
            } else {
                return MoveType.MOVE_NO_ACCESS_SETTLEMENT;
            }
        } else { // moving to sea, check for embarkation
            if (defender == null || !defender.isOwner(owner)) {
                return MoveType.MOVE_NO_ACCESS_EMBARK;
            }
            for (Unit u : target.getUnits().entities()) {
                if (u.canAddUnit(owner, unitType)) {
                    return MoveType.EMBARK;
                }
            }
            return MoveType.MOVE_NO_ACCESS_FULL;
        }
    }

    private MoveType getTradeMoveType(Settlement settlement) {
        if (settlement instanceof Colony) {
            return (owner.atWarWith(settlement.getOwner()))
                ? MoveType.MOVE_NO_ACCESS_WAR
                : (!owner.getFeatures().hasAbility(Ability.TRADE_WITH_FOREIGN_COLONIES))
                ? MoveType.MOVE_NO_ACCESS_TRADE
                : MoveType.ENTER_SETTLEMENT_WITH_CARRIER_AND_GOODS;
        } else if (settlement instanceof IndianSettlement) {
            // Do not block for war, bringing gifts is allowed
            return (!allowContact(settlement))
                ? MoveType.MOVE_NO_ACCESS_CONTACT
                : (hasGoodsCargo() || Specification.options.getBoolean(GameOptions.EMPTY_TRADERS))
                ? MoveType.ENTER_SETTLEMENT_WITH_CARRIER_AND_GOODS
                : MoveType.MOVE_NO_ACCESS_GOODS;
        } else {
            return MoveType.MOVE_ILLEGAL; // should not happen
        }
    }

    private boolean hasGoodsCargo() {
        return getGoodsSpaceTaken() > 0;
    }

    private int getGoodsSpaceTaken() {
        if (goodsContainer == null) {
            return 0;
        }
        return goodsContainer.getCargoSpaceTaken();
    }

    private MoveType getLearnMoveType(Tile from, Settlement settlement) {
        if (settlement instanceof Colony) {
            return MoveType.MOVE_NO_ACCESS_SETTLEMENT;
        } else if (settlement instanceof IndianSettlement) {
            return (!allowContact(settlement))
                ? MoveType.MOVE_NO_ACCESS_CONTACT
                : (!allowMoveFrom(from))
                ? MoveType.MOVE_NO_ACCESS_WATER
                : (!unitType.canBeUpgraded(UnitTypeChange.ChangeType.NATIVES))
                ? MoveType.MOVE_NO_ACCESS_SKILL
                : MoveType.ENTER_INDIAN_SETTLEMENT_WITH_FREE_COLONIST;
        } else {
            return MoveType.MOVE_ILLEGAL; // should not happen
        }
    }

    private MoveType getMissionaryMoveType(Tile from, Settlement settlement) {
        if (settlement instanceof Colony) {
            return MoveType.MOVE_NO_ACCESS_SETTLEMENT;
        } else if (settlement instanceof IndianSettlement) {
            return (!allowContact(settlement))
                ? MoveType.MOVE_NO_ACCESS_CONTACT
                : (!allowMoveFrom(from))
                ? MoveType.MOVE_NO_ACCESS_WATER
                : (settlement.getOwner().missionsBanned(owner))
                ? MoveType.MOVE_NO_ACCESS_MISSION_BAN
                : MoveType.ENTER_INDIAN_SETTLEMENT_WITH_MISSIONARY;
        } else {
            return MoveType.MOVE_ILLEGAL; // should not happen
        }
    }

    private boolean isDamaged() {
        return hitPoints < unitType.getHitPoints();
    }

    private boolean hasTileEnemyUnits(Tile target) {
        Unit defender = target.getUnits().first();
        return defender != null && !defender.isOwner(owner);
    }

    private boolean isTradingUnit() {
        return unitType.hasAbility(Ability.CARRY_GOODS) && owner.nationType().isEuropean();
    }

    /**
     * Is this unit allowed to contact a settlement?
     *
     * @param settlement The <code>Settlement</code> to consider.
     * @return True if the contact is allowed.
     */
    private boolean allowContact(Settlement settlement) {
        return owner.hasContacted(settlement.getOwner());
    }

    private boolean allowMoveFrom(Tile from) {
        return from.getType().isLand() || (!owner.isRoyal() && Specification.options.getBoolean(GameOptions.AMPHIBIOUS_MOVES));
    }

    private boolean hasAbility(String code) {
        if (unitType.hasAbility(code)) {
            return true;
        }
        if (unitRole.hasAbility(code)) {
            return true;
        }
        if (owner.getFeatures().hasAbility(code)) {
            return true;
        }
        return false;
    }

    /**
     * Gets the cost of moving this <code>Unit</code> from the given
     * <code>Tile</code> onto the given <code>Tile</code>. A call to
     * {@link #getMoveType(Tile, Tile)} will return
     * <code>MOVE_NO_MOVES</code>, if {@link #getMoveCost} returns a move cost
     * larger than the {@link #getMovesLeft moves left}.
     *
     * @param from The <code>Tile</code> this <code>Unit</code> will move from.
     * @param target The <code>Tile</code> this <code>Unit</code> will move onto.
     * @param actualMovesLeft The amount of moves this Unit has left.
     * @param unitInitialMoves Unit initial moves
     * @return The cost of moving this unit onto the given <code>Tile</code>.
     */
    public int caclulateMoveCost(Tile from, Tile target, Direction moveDirection, int actualMovesLeft, int unitInitialMoves) {
        int cost = target.getType().getBasicMoveCost();
        if (target.getType().isLand() && !unitType.isNaval()) {
            cost = target.getMoveCost(moveDirection, cost);
        }

        if (Unit.isBeached(from, unitType)) {
            // Ship on land due to it was in a colony which was abandoned
            cost = actualMovesLeft;
        } else if (cost > actualMovesLeft) {
            // Using +2 in order to make 1/3 and 2/3 move count as
            // 3/3, only when getMovesLeft > 0
            if ((actualMovesLeft + 2 >= unitInitialMoves
                || cost <= actualMovesLeft + 2
                || target.hasSettlement()) && actualMovesLeft != 0) {
                cost = actualMovesLeft;
            }
        }
        return cost;
    }

    public int initialMoves() {
        return Unit.initialMoves(owner, unitType, unitRole);
    }

    public UnitType getUnitType() {
        return unitType;
    }

    public Player getOwner() {
        return owner;
    }

}
