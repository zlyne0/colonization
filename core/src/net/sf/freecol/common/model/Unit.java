package net.sf.freecol.common.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import com.badlogic.gdx.utils.ObjectIntMap.Entry;

import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.AbstractGoods;
import net.sf.freecol.common.model.specification.GameOptions;
import net.sf.freecol.common.model.specification.Modifier;
import net.sf.freecol.common.model.specification.ScopeAppliable;
import net.sf.freecol.common.model.specification.UnitTypeChange.ChangeType;
import promitech.colonization.Direction;
import promitech.colonization.savegame.ObjectFromNodeSetter;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class Unit extends ObjectWithId implements UnitLocation, ScopeAppliable {

    /** A state a Unit can have. */
    public static enum UnitState {
        ACTIVE,
        FORTIFIED,
        SENTRY,
        IN_COLONY,
        IMPROVING,
        // @compat 0.10.0
        TO_EUROPE,
        TO_AMERICA,
        // end @compat
        FORTIFYING,
        SKIPPED
    }
	
    public static enum MoveDestinationType {
    	TILE, EUROPE;
    }
	
    protected String name;
	private Player owner;
    public UnitType unitType;
    public UnitRole unitRole;
    
    protected UnitLocation location;

    private UnitState state = UnitState.ACTIVE;
    private int movesLeft;
    private int hitPoints;
    
    private int workLeft = -1;
    private TileImprovementType tileImprovementType;
    
    private boolean disposed = false;
    private int visibleGoodsCount = -1;
    private int treasureAmount = 0;
    private int experience = 0;
    private MoveDestinationType destinationType;
    private int destinationX;
    private int destinationY;
    
    private String indianSettlement;

    /**
     * The amount of role-equipment this unit carries, subject to
     * role.getMaximumCount().  Currently zero or one except for pioneers.
     */
    protected int roleCount = -1;
    
    private UnitContainer unitContainer = null;
    private GoodsContainer goodsContainer = null;
    
    protected Unit(String id) {
    	super(id);
    }
    
    public Unit(String id, UnitType aUnitType, UnitRole aUnitRole, Player anOwner) {
    	super(id);
    	this.unitType = aUnitType;
    	this.unitRole = aUnitRole;
    	this.owner = anOwner;
    	this.owner.units.add(this);
    	
    	this.movesLeft = getInitialMovesLeft();
    	this.hitPoints = unitType.getHitPoints();
    	
        if (unitType.canCarryUnits()) {
            unitContainer = new UnitContainer(this);
        }
        if (unitType.hasAbility(Ability.CARRY_GOODS)) {
        	goodsContainer = new GoodsContainer();
        }
        roleCount = unitRole.getMaximumCount();
    }

	public String toString() {
        return "id = " + id + ", unitType = " + unitType + ", workLeft = " + workLeft;
    }
    
    public String resourceImageKey() {
    	if (!owner.nationType().isEuropean()) {
    		if (UnitType.FREE_COLONIST.equals(unitType.getId())) {
    			return unitType.getId() + "." + unitRole.getRoleSuffix() + ".native.image";
    		}
    	}
    	
    	if (unitRole.isDefaultRole()) {
    	    return unitType.getId() + unitRole.getRoleSuffix() + ".image";
    	} else {
            return unitType.getId() + "." + unitRole.getRoleSuffix() + ".image"; 
    	}
    }

	public Tile getTile() {
		if (location == null) {
			throw new IllegalStateException("unit[" + getId() + "] location is null unit: " + this);
		}
		if (!(location instanceof Tile)) {
			throw new IllegalStateException("unit[" + getId() + "] location is not tile but it's " + location.getClass());
		}
		return (Tile)location;
	}
    
	public Tile getTileLocationOrNull() {
		if (location instanceof Tile) {
			return (Tile)location;
		}
		return null;
	}
	
	public boolean isAtLocation(Class<? extends UnitLocation> unitLocationClass) {
	    return location != null && location.getClass().equals(unitLocationClass);
	}
	
	@SuppressWarnings("unchecked")
    public <T extends UnitLocation> T getLocationOrNull(Class<T> unitLocationClass) {
	    if (location != null && location.getClass().equals(unitLocationClass)) {
	        return (T)location;
	    }
	    return null;
	}
	
	public void changeUnitLocation(UnitLocation newUnitLocation) {
	    UnitLocation oldLocation = location;
		if (oldLocation != null) {
		    removeFromLocation();
			
			if (oldLocation.canAutoLoadUnit()) {
				embarkUnitsFromLocation(oldLocation);
			}
		}
		newUnitLocation.addUnit(this);
		if (newUnitLocation.canAutoUnloadUnits()) {
			disembarkUnitsToLocation(newUnitLocation);
		}
		location = newUnitLocation;
	}
	
	public void removeFromLocation() {
	    if (location != null) {
	        location.removeUnit(this);
	        location = null;
	    }
	}
	
	public void remove() {
	    removeFromLocation();
		disposed = true;
	}
	
    public boolean canAddUnit(Unit unit) {
    	if (unitContainer == null) {
    		throw new IllegalStateException("unit " + this.toString() + " does not have unit container. Unit container not initialized");
    	}
    	return unitContainer.canAdd(unit);
    }
    
    public GoodsContainer getGoodsContainer() {
        return goodsContainer;
    }
    
	public Player getOwner() {
		return owner;
	}

	public void setOwner(Player player) {
        this.owner = player;
	}
	
	public void changeOwner(Player newOwner) {
		this.owner.units.removeId(this);
		this.owner = newOwner;
		this.owner.units.add(this);
	}
	
	public void captureUnit(Unit unit) {
		unit.captureByPlayer(owner);
		
    	unit.changeUnitLocation(this.getTile());
    	unit.reduceMovesLeftToZero();
    	unit.clearDestination();
    	unit.setState(Unit.UnitState.ACTIVE);
	}
	
	public void captureByPlayer(Player player) {
		owner.units.removeId(this);
		owner = player;
		player.units.add(this);
	}
	
	public boolean isOwner(Player player) {
		return owner.equals(player);
	}
	
	public int lineOfSight() {
		return unitType.lineOfSight();
	}
	
    public boolean isDamaged() {
        return hitPoints < unitType.getHitPoints();
    }
	
    public boolean isOnCarrier() {
        return location != null && location instanceof Unit;
    }
    
    private int getSpaceTaken() {
        int space = 0;
        if (unitContainer != null) {
        	space += unitContainer.getSpaceTakenByUnits();
        }
        if (goodsContainer != null) {
        	space += goodsContainer.getCargoSpaceTaken();
        }
        return space;
    }

    public boolean hasSpaceForAdditionalCargo() {
        return getSpaceTaken() < unitType.getSpace();
    }
    
    public boolean hasSpaceForAdditionalCargoSlots(int additionalCargoSlots) {
    	return getSpaceTaken() + additionalCargoSlots <= unitType.getSpace();
    }
    
    public boolean hasSpaceForAdditionalCargo(AbstractGoods additionalCargo) {
        int space = 0;
        if (unitContainer != null) {
            space += unitContainer.getSpaceTakenByUnits();
        }
        if (goodsContainer != null) {
            space += goodsContainer.takenCargoSlotsWithAdditionalCargo(additionalCargo);
        }
        return space <= unitType.getSpace();
    }
    
	public int maxGoodsAmountToFillFreeSlots(String goodsTypeId) {
		int unitSpace = unitType.getSpace();
		if (unitContainer != null) {
			unitSpace -= unitContainer.getSpaceTakenByUnits();
		}
		if (goodsContainer == null) {
			return 0;
		}
		return goodsContainer.maxGoodsAmountToFillFreeSlots(goodsTypeId, unitSpace);
	}
    
    public boolean hasNoSpaceForAdditionalCargoSlots(int additionalCargoSlots) {
    	return !hasSpaceForAdditionalCargoSlots(additionalCargoSlots);
    }
    
    public boolean hasNoSpace() {
    	return unitType.getSpace() == 0 || getSpaceTaken() >= unitType.getSpace();
    }
    
	public boolean couldMove() {
        return state == UnitState.ACTIVE
            && movesLeft > 0
            //&& destination == null // Can not reach next tile
            //&& tradeRoute == null
            && !isDamaged()
            && !disposed
            //&& !isAtSea()
            && !isOnCarrier();
    }

    public boolean isTradingUnit() {
        return unitType.hasAbility(Ability.CARRY_GOODS) && owner.nationType().isEuropean();
    }
    
    public final Object getTradeRoute() {
        return null;
    }
	
    public boolean isDestinationSet() {
    	return destinationType != null;
    }
    
    public boolean isDestinationTile() {
    	return MoveDestinationType.TILE.equals(destinationType);
    }
    
	public boolean isDestinationEurope() {
    	return MoveDestinationType.EUROPE.equals(destinationType);
	}
    
	public int getDestinationX() {
		return destinationX;
	}

	public int getDestinationY() {
		return destinationY;
	}
    
	public void clearDestination() {
		this.destinationType = null;
	}
    
	public void setDestinationEurope() {
		this.destinationType = MoveDestinationType.EUROPE;
	}

	public void setDestination(int x, int y) {
		this.destinationType = MoveDestinationType.TILE;
		this.destinationX = x;
		this.destinationY = y;
	}
	
	public void setDestination(Tile tile) {
		this.destinationType = MoveDestinationType.TILE;
		this.destinationX = tile.x;
		this.destinationY = tile.y;
	}
	
    public UnitState getState() {
        return state;
    }
    
	public TileImprovementType getTileImprovementType() {
		return tileImprovementType;
	}
    
    public int getMovesLeft() {
        return movesLeft;
    }
    
    public boolean hasMovesPoints() {
    	return movesLeft > 0;
    }
    
    public boolean isNaval() {
    	return unitType != null && unitType.isNaval();
    }
    
	public boolean canMoveToHighSeas() {
		return isNaval();
	}
    
    public boolean isCarrier() {
        return unitType.canCarryUnits() || unitType.hasAbility(Ability.CARRY_GOODS);
    }
    
    public boolean isMounted() {
        return unitRole.hasAbility(Ability.MOUNTED);
    }
    
    public boolean isPerson() {
        return unitType.hasAbility(Ability.PERSON)
            || unitType.hasAbility(Ability.BORN_IN_COLONY)
            || unitType.hasAbility(Ability.BORN_IN_INDIAN_SETTLEMENT)
            || unitType.hasAbility(Ability.FOUND_COLONY);
    }
    
    public boolean isOffensiveUnit() {
        return unitType.isOffensive() || unitRole.isOffensive();
    }
    
	public boolean isArmed() {
        return hasAbility(Ability.ARMED);
	}
    
    public boolean isDefensiveUnit() {
        return (unitType.isDefensive() || unitRole.isDefensive()) && !isCarrier();
    }
	
    /**
     * Get the visible amount of goods that is carried by this unit.
     *
     * @return The visible amount of goods carried by this <code>Unit</code>.
     */
    public int getVisibleGoodsCount() {
        return (visibleGoodsCount >= 0) ? visibleGoodsCount : getGoodsSpaceTaken();
    }
    
    public int getGoodsSpaceTaken() {
        if (!canCarryGoods()) {
        	return 0;
        }
        GoodsContainer gc = getGoodsContainer();
        return (gc == null) ? 0 : gc.getCargoSpaceTaken();
    }
    
    public void transferAllGoods(Unit toUnit) {
    	for (Entry<String> transferedGoods : getGoodsContainer().entries()) {
			int max = toUnit.maxGoodsAmountToFillFreeSlots(transferedGoods.key);
			if (max > 0) {
				toUnit.getGoodsContainer().increaseGoodsQuantity(
					transferedGoods.key, max
				);
			}
		}
    }
    
    public boolean canCarryGoods() {
        return unitType.hasAbility(Ability.CARRY_GOODS);
    }
    
    public boolean hasGoodsCargo() {
        return getGoodsSpaceTaken() > 0;
    }
    
    private boolean allowMoveFrom(Tile from) {
        return from.getType().isLand() || (!owner.isRoyal() && Specification.options.getBoolean(GameOptions.AMPHIBIOUS_MOVES));
    }
    
    public String getOccupationKey(Player player) {
        return (player.equalsId(owner))
            ? ((isDamaged())
                ? "model.unit.occupation.underRepair"
                : (getTradeRoute() != null)
                ? "model.unit.occupation.inTradeRoute"
                : (isDestinationSet())
                ? "model.unit.occupation.goingSomewhere"
                : (getState() == Unit.UnitState.IMPROVING && getTileImprovementType() != null)
                ? (getTileImprovementType().getId() + ".occupationString")
                : (getState() == Unit.UnitState.ACTIVE && getMovesLeft() <= 0)
                ? "model.unit.occupation.activeNoMovesLeft"
                : ("model.unit.occupation." + getState().toString().toLowerCase(Locale.US)))
            : (isNaval())
            ? Integer.toString(getVisibleGoodsCount())
            : "model.unit.occupation.activeNoMovesLeft";
    }

    public MoveType getMoveType(Tile from, Tile target) {
		if (from == null || target == null) {
			return MoveType.MOVE_NO_TILE;
		}
    	if (isNaval()) {
    		return getNavalMoveType(target);
    	} else {
    		return getLandMoveType(from, target);
    	}
    }

    public boolean isColonist() {
        return unitType.hasAbility(Ability.FOUND_COLONY) && owner.getFeatures().hasAbility(Ability.FOUNDS_COLONIES);
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
                    } else if (!isOffensiveUnit()) {
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
                return MoveType.MOVE;
            } else if (isTradingUnit()) {
                return getTradeMoveType(settlement);
            } else if (isColonist()) {
                if (settlement instanceof Colony && hasAbility(Ability.NEGOTIATE)) {
                    return (allowMoveFrom(from)) ? MoveType.ENTER_FOREIGN_COLONY_WITH_SCOUT : MoveType.MOVE_NO_ACCESS_WATER;
                } else if (settlement instanceof IndianSettlement && hasAbility(Ability.SPEAK_WITH_CHIEF)) {
                    return (allowMoveFrom(from)) ? MoveType.ENTER_INDIAN_SETTLEMENT_WITH_SCOUT : MoveType.MOVE_NO_ACCESS_WATER;
                } else if (isOffensiveUnit()) {
                    return (allowMoveFrom(from)) ? MoveType.ATTACK_SETTLEMENT : MoveType.MOVE_NO_ATTACK_MARINE;
                } else if (hasAbility(Ability.ESTABLISH_MISSION)) {
                    return getMissionaryMoveType(from, settlement);
                } else {
                    return getLearnMoveType(from, settlement);
                }
            } else if (isOffensiveUnit()) {
                return (allowMoveFrom(from)) ? MoveType.ATTACK_SETTLEMENT : MoveType.MOVE_NO_ATTACK_MARINE;
            } else {
                return MoveType.MOVE_NO_ACCESS_SETTLEMENT;
            }
        } else { // moving to sea, check for embarkation
            if (defender == null || !defender.isOwner(owner)) {
                return MoveType.MOVE_NO_ACCESS_EMBARK;
            }
            for (Unit u : target.getUnits().entities()) {
                if (u.unitContainer != null && u.canAddUnit(this)) {
                	return MoveType.EMBARK;
                }
            }
            return MoveType.MOVE_NO_ACCESS_FULL;
        }
    }
    
    private MoveType getLearnMoveType(Tile from, Settlement settlement) {
        if (settlement instanceof Colony) {
            return MoveType.MOVE_NO_ACCESS_SETTLEMENT;
        } else if (settlement instanceof IndianSettlement) {
            return (!allowContact(settlement))
                ? MoveType.MOVE_NO_ACCESS_CONTACT
                : (!allowMoveFrom(from))
                ? MoveType.MOVE_NO_ACCESS_WATER
                : (!unitType.canBeUpgraded(ChangeType.NATIVES))
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
                : (settlement.getOwner().missionsBanned(getOwner()))
                ? MoveType.MOVE_NO_ACCESS_MISSION_BAN
                : MoveType.ENTER_INDIAN_SETTLEMENT_WITH_MISSIONARY;
        } else {
            return MoveType.MOVE_ILLEGAL; // should not happen
        }
    }
    
	public MoveType getNavalMoveType(Tile target) {
		if (target == null) {
			return (owner.canMoveToEurope()) ? MoveType.MOVE_HIGH_SEAS : MoveType.MOVE_NO_EUROPE;
		} 
		if (isDamaged()) {
			return MoveType.MOVE_NO_REPAIR;
		}

		if (target.getType().isLand()) {
			Settlement settlement = target.getSettlement();
			if (settlement == null) {
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
				return (isOffensiveUnit()) ? MoveType.ATTACK_UNIT : MoveType.MOVE_NO_ATTACK_CIVILIAN;
			}
			return (target.isDirectlyHighSeasConnected()) ? MoveType.MOVE_HIGH_SEAS : MoveType.MOVE;
		}
	}
    
	private boolean hasTileEnemyUnits(Tile target) {
		Unit defender = target.getUnits().first();
		return defender != null && !defender.isOwner(owner);
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

    public int getMoveCost(Tile from, Tile target, Direction moveDirection) {
    	return getMoveCost(from, target, moveDirection, movesLeft);
    }
    
    /**
     * Gets the cost of moving this <code>Unit</code> from the given
     * <code>Tile</code> onto the given <code>Tile</code>. A call to
     * {@link #getMoveType(Tile, Tile, int)} will return
     * <code>MOVE_NO_MOVES</code>, if {@link #getMoveCost} returns a move cost
     * larger than the {@link #getMovesLeft moves left}.
     *
     * @param from The <code>Tile</code> this <code>Unit</code> will move from.
     * @param target The <code>Tile</code> this <code>Unit</code> will move onto.
     * @param movesLeft The amount of moves this Unit has left.
     * @return The cost of moving this unit onto the given <code>Tile</code>.
     */
    public int getMoveCost(Tile from, Tile target, Direction moveDirection, int _movesLeft) {
        int cost = target.getType().getBasicMoveCost();
        if (target.getType().isLand() && !isNaval()) {
        	cost = target.getMoveCost(moveDirection, cost);
        }

        if (isBeached(from)) {
            // Ship on land due to it was in a colony which was abandoned
            cost = _movesLeft;
        } else if (cost > _movesLeft) {
            // Using +2 in order to make 1/3 and 2/3 move count as
            // 3/3, only when getMovesLeft > 0
            if ((_movesLeft + 2 >= getInitialMovesLeft() 
            		|| cost <= _movesLeft + 2
            		|| target.hasSettlement()) && _movesLeft != 0) {
                cost = _movesLeft;
            }
        }
        return cost;
    }
    
    public int getInitialMovesLeft() {
        float m = owner.getFeatures().applyModifier(
            Modifier.MOVEMENT_BONUS, 
            unitType.getMovement(), 
            unitType
        );
    	return (int)unitRole.applyModifier(Modifier.MOVEMENT_BONUS, m);
    }
    
    /**
     * Would this unit be beached if it was on a particular tile?
     *
     * @param tile The <code>Tile</code> to check.
     * @return True if the unit is a beached ship.
     */
    public boolean isBeached(Tile tile) {
        return isNaval() && tile != null && tile.getType().isLand() && !tile.hasSettlement();
    }
    
    public boolean isBeached() {
    	return isBeached(this.getTileLocationOrNull());
    }
    
    public void setState(UnitState newState) {
        if (state == newState) {
            // No need to do anything when the state is unchanged
            return;
        } else if (!canChangeState(newState)) {
            throw new IllegalStateException("Illegal UnitState transition: " + state + " -> " + newState);
        } else {
        	if (UnitState.IMPROVING == state && newState != UnitState.IMPROVING) {
        		workLeft = -1;
        		tileImprovementType = null;
        	}
        	switch (newState) {
				case FORTIFYING:
					workLeft = 1;
					break;
				case FORTIFIED:
					workLeft = -1;
					movesLeft = 0;
					break;
				case ACTIVE:
					workLeft = -1;
					break;
				case IN_COLONY:
					movesLeft = 0;
					break;
				default:
					break;
			}
            this.state = newState;
        }
    }
    
    /**
     * Checks if a <code>Unit</code> can get the given state set.
     *
     * @param newState The new state for this Unit.  Should be one of
     *     {UnitState.ACTIVE, FORTIFIED, ...}.
     * @return True if the <code>Unit</code> state can be changed to
     *     the new value.
     */
    public boolean canChangeState(UnitState newState) {
        if (getState() == newState) return false;
        switch (newState) {
        case ACTIVE:
            return true;
        case IMPROVING: 
        	return true;
        case FORTIFIED:
            return getState() == UnitState.FORTIFYING;
        case FORTIFYING:
            return getMovesLeft() > 0 && getState() != UnitState.FORTIFIED;
        case IN_COLONY:
            return !isNaval();
        case SENTRY:
            return true;
        case SKIPPED:
            return true;
        default:
            return false;
        }
    }
    
    public void setStateToAllChildren(UnitState state) {
        if (unitContainer != null) {
            unitContainer.setStateToAllChildren(state);
        }
    }
    
	public void reduceMovesLeft(int moveCost) {
		int moves = movesLeft - moveCost;
		this.movesLeft = (moves < 0) ? 0 : moves;
	}
    
	public void reduceMovesLeftToZero() {
		this.movesLeft = 0;
	}
	
	public boolean hasMovesPoints(int moveCost) {
		return this.movesLeft >= moveCost;
	}
	
	public void resetMovesLeftOnNewTurn() {
		if (isDamaged()) {
			movesLeft = 0;
		} else {
			movesLeft = getInitialMovesLeft();
		}
	}
	
    public UnitContainer getUnitContainer() {
        return unitContainer;
    }
    
	@Override
	public MapIdEntitiesReadOnly<Unit> getUnits() {
		return unitContainer.getUnits();
	}
    
    @Override
    public void addUnit(Unit unit) {
        unitContainer.addUnit(unit);
    }

    @Override
    public void removeUnit(Unit unit) {
        unitContainer.getUnits().removeId(unit);
    }
	
	@Override
	public boolean canAutoUnloadUnits() {
		return false;
	}
	
	@Override
	public boolean canAutoLoadUnit() {
		return false;
	}

    public boolean canCarryTreasure() {
        return hasAbility(Ability.CARRY_TREASURE);
    }
    
    public int getTreasureAmount() {
        if (!canCarryTreasure()) {
            throw new IllegalStateException("Unit can not carry treasure");
        }
        return treasureAmount;
    }
    
    public void setTreasureAmount(int treasureAmount) {
        if (!canCarryTreasure()) {
            throw new IllegalStateException("Unit can not carry treasure");
        }
    	this.treasureAmount = treasureAmount;
    }
    
	public boolean isExpert() {
		return unitType.getMaximumExperience() == 0;
	}

	public void changeRole(UnitRole newUnitRole) {
		changeRole(newUnitRole, newUnitRole.getMaximumCount());
	}
	
	public void changeRole(UnitRole newUnitRole, int aRoleCount) {
		if (!newUnitRole.isCompatibleWith(unitRole)) {
			experience = 0;
		}
		unitRole = newUnitRole;
		roleCount = aRoleCount;
		reduceMovesLeftToZero();
	}
	
	public void downgradeRole() {
	    if (unitRole.noDowngradeRole()) {
	        changeRole(Specification.instance.unitRoles.getById(UnitRole.DEFAULT_ROLE_ID));
	    } else {
	        changeRole(Specification.instance.unitRoles.getById(unitRole.getDowngradeRoleId()));
	    }
	}
	
	public void changeUnitType(UnitType newUnitType) {
		this.unitType = newUnitType;
		this.experience = 0;
		this.hitPoints = newUnitType.getHitPoints();
	}
	
	public void changeUnitType(ChangeType changeType) {
        UnitType newUnitType = unitType.upgradeByChangeType(changeType, owner);
        changeUnitType(newUnitType);
	}
	
	public List<UnitRole> avaliableRoles(ObjectWithFeatures place) {
	    List<UnitRole> a = new ArrayList<UnitRole>();
        for (UnitRole role : Specification.instance.unitRoles.entities()) {
            if (role.isAvailableTo(unitType, place, owner.getFeatures())) {
                a.add(role);
            }
        }
        return a;
	}

    public UnitRole getUnitRole() {
        return unitRole;
    }
	
	@Override
	public boolean hasAbility(String code) {
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
	
	public boolean isRoleAvailable(UnitRole role) {
		return role.isAvailableTo(unitType, unitRole, owner.getFeatures());
	}
	
	public void getAbilities(String code, List<Ability> abilities) {
		unitType.getAbilities(code, abilities);
		unitRole.getAbilities(code, abilities);
		owner.getFeatures().getAbilities(code, abilities);
	}
	
	public void startImprovement(Tile tile, TileImprovementType improvement) {
		workLeft = tile.getType().getBasicWorkTurns() + improvement.getAddWorkTurns();
		if (getMovesLeft() < 1) {
			workLeft++;
		}
		tileImprovementType = improvement;
		setState(UnitState.IMPROVING);
	}
	
	/**
	 * Return true if end of tile improvement
	 * @return
	 */
	public boolean workOnImprovement() {
		int improvementWorkDone = 1;
		if (unitType.hasAbility(Ability.EXPERT_PIONEER)) {
			improvementWorkDone = 2;
		}
		workLeft = workLeft - improvementWorkDone;
		if (workLeft <= 0) {
			roleCount -= tileImprovementType.getExpendedAmount();
			if (roleCount <= 0) {
				unitRole = Specification.instance.unitRoles.getById(UnitRole.DEFAULT_ROLE_ID);
			}
			return true;
		} else {
			return false;
		}
	}
	
	public void sailOnHighSea() {
	    workLeft -= 1;
	}
	
	public void moveUnitToHighSea() {
	    changeUnitLocation(owner.getHighSeas());
	    reduceMovesLeftToZero();
	    setDestinationEurope();
	    workLeft = getSailTurns();
	}
	
	private void embarkUnitsFromLocation(UnitLocation anUnitLocation) {
		if (hasNoSpace()) {
			return;
		}
		for (Unit unit : new ArrayList<Unit>(anUnitLocation.getUnits().entities())) {
			if (UnitState.SENTRY.equals(unit.getState())) {
				if (this.canAddUnit(unit)) {
					unit.setState(UnitState.SKIPPED);
					unit.changeUnitLocation(this);
					unit.reduceMovesLeftToZero();
				}
			}
		}
	}
	
	private void disembarkUnitsToLocation(UnitLocation newUnitLocation) {
		if (unitContainer != null && unitContainer.isNotEmpty()) {
			for (Unit unit : unitContainer.getUnits().entities()) {
				newUnitLocation.addUnit(unit);
				unit.location = newUnitLocation;
				unit.setState(UnitState.ACTIVE);
			}
			unitContainer.getUnits().clear();
		}
	}
	
	public void sailUnitToNewWorld() {
		changeUnitLocation(owner.getHighSeas());
		reduceMovesLeftToZero();
		setDestination(owner.getEntryLocationX(), owner.getEntryLocationY());
		workLeft = getSailTurns();
	}
	
    public int getSailTurns() {
        float base = Specification.options.getIntValue(GameOptions.TURNS_TO_SAIL);
        return (int)getOwner().getFeatures().applyModifier(Modifier.SAIL_HIGH_SEAS, base, unitType);
    }
    
	public boolean isWorkComplete() {
	    return workLeft <= 0;
	}

    public void gainExperience(int aditionalExperience) {
        this.experience = Math.min(this.experience + aditionalExperience, unitType.getMaximumExperience());
    }
	
    public boolean isPromotedToExpert() {
    	return this.experience >= unitType.getMaximumExperience();
    }

	public int getRoleCount() {
		return roleCount;
	}
    
	public void setIndianSettlement(IndianSettlement settlement) {
		this.indianSettlement = settlement.getId();
	}
	
	public void removeFromIndianSettlement() {
		this.indianSettlement = null;
	}
	
	public String getIndianSettlementId() {
	    return this.indianSettlement;
	}
	
	public boolean hasRepairLocation() {
	    return getRepairLocation() != null;
	}
	
	public UnitLocation getRepairLocation() {
	    for (Settlement settlement : getOwner().settlements.entities()) {
	        if (settlement.getColony().colonyUpdatableFeatures.hasAbility(Ability.REPAIR_UNITS)) {
	            return settlement;
	        }
	    }
	    return getOwner().getEurope();
	}

	public boolean isDisposed() {
		return disposed;
	}
	
	public void embarkCarrierOnTile(Tile destTile) {
	    Unit carrier = null;
        for (Unit u : destTile.getUnits().entities()) {
            if (u.canAddUnit(this)) {
                carrier = u;
                break;
            }
        }
        if (carrier == null) {
            throw new IllegalStateException("carrier unit on tile: " + destTile);
        }
        embarkTo(carrier);
	}
	
	public void embarkTo(Unit carrier) {
	    this.setState(UnitState.SKIPPED);
	    this.changeUnitLocation(carrier);
	    this.reduceMovesLeftToZero();
	}
	
	public void makeUnitDamaged(UnitLocation repairLocation) {
        goodsContainer.decreaseAllToZero();
        for (Unit u : new HashSet<Unit>(unitContainer.getUnits().entities())) {
            owner.removeUnit(u);
        }
        if (repairLocation instanceof Colony) {
            changeUnitLocation(((Colony) repairLocation).tile);
        } else {
            changeUnitLocation(repairLocation);
        }
        hitPoints = 1;
        clearDestination();
        setState(Unit.UnitState.ACTIVE);
        reduceMovesLeftToZero();
	}
	
	public boolean canCaptureEquipment(Unit unitEquipment) {
		return capturedEquipment(unitEquipment.unitRole) != null;
	}
	
	public UnitRole capturedEquipment(Unit unitEquipment) {
	    return capturedEquipment(unitEquipment.unitRole);
	}

    public boolean canCaptureEquipment(UnitRole unitRoleEquipment) {
        return capturedEquipment(unitRoleEquipment) != null;
    }
	
	public UnitRole capturedEquipment(UnitRole capturedRole) {
		if (!hasAbility(Ability.CAPTURE_EQUIPMENT)) {
			return null;
		}
		for (UnitRole milRole : Specification.instance.militaryRoles) {
			if (isRoleAvailable(milRole)) {
				if (milRole.canChangeRole(unitRole, capturedRole)) {
					return milRole;
				}
			}
		}
		return null;
	}
	
	public boolean losingEquipmentKillsUnit() {
		return hasAbility(Ability.DISPOSE_ON_ALL_EQUIPMENT_LOST) && unitRole.noDowngradeRole();
	}
	
	public boolean losingEquipmentDemotesUnit() {
		return hasAbility(Ability.DEMOTE_ON_ALL_EQUIPMENT_LOST) && unitRole.noDowngradeRole();
	}
	
	public boolean canUpgradeByChangeType(ChangeType changeType) {
		return unitType.upgradeByChangeType(changeType, getOwner()) != null;
	}
	
    public static class Xml extends XmlNodeParser<Unit> {
        
        private static final String ATTR_INDIAN_SETTLEMENT = "indianSettlement";
		private static final String ATTR_TILE_IMPROVEMENT_TYPE_ID = "tileImprovementTypeId";
		private static final String ATTR_WORK_LEFT = "workLeft";
		private static final String ATTR_DESTINATION_Y = "destinationY";
		private static final String ATTR_DESTINATION_X = "destinationX";
		private static final String ATTR_DESTINATION_TYPE = "destinationType";
		private static final String ATTR_EXPERIENCE = "experience";
		private static final String ATTR_NAME = "name";
		private static final String ATTR_ROLE_COUNT = "roleCount";
		private static final String ATTR_TREASURE_AMOUNT = "treasureAmount";
		private static final String ATTR_VISIBLE_GOODS_COUNT = "visibleGoodsCount";
		private static final String ATTR_HIT_POINTS = "hitPoints";
		private static final String ATTR_MOVES_LEFT = "movesLeft";
		private static final String ATTR_STATE = "state";
		private static final String ATTR_OWNER = "owner";
		private static final String ATTR_ROLE = "role";
		private static final String ATTR_UNIT_TYPE = "unitType";

		public Xml() {
            addNode(Unit.class, new ObjectFromNodeSetter<Unit,Unit>() {
                @Override
                public void set(Unit actualUnit, Unit newUnit) {
                    newUnit.changeUnitLocation(actualUnit);
                }
				@Override
				public void generateXml(Unit source, ChildObject2XmlCustomeHandler<Unit> xmlGenerator) throws IOException {
					if (source.unitContainer != null) {
						xmlGenerator.generateXmlFromCollection(source.unitContainer.getUnits().entities());
					}
				}
            });
            addNode(GoodsContainer.class, "goodsContainer");
        }

        @Override
        public void startElement(XmlNodeAttributes attr) {
            String unitTypeStr = attr.getStrAttribute(ATTR_UNIT_TYPE);
            String unitRoleStr = attr.getStrAttribute(ATTR_ROLE);
            String ownerStr = attr.getStrAttribute(ATTR_OWNER);
            
            Unit unit = new Unit(
        		attr.getStrAttribute(ATTR_ID),
        		Specification.instance.unitTypes.getById(unitTypeStr),
        		Specification.instance.unitRoles.getById(unitRoleStr),
        		game.players.getById(ownerStr)
            );
            
            unit.state = attr.getEnumAttribute(UnitState.class, ATTR_STATE);
            unit.movesLeft = attr.getIntAttribute(ATTR_MOVES_LEFT);
            unit.hitPoints = attr.getIntAttribute(ATTR_HIT_POINTS);
            unit.visibleGoodsCount = attr.getIntAttribute(ATTR_VISIBLE_GOODS_COUNT, -1);
            unit.treasureAmount = attr.getIntAttribute(ATTR_TREASURE_AMOUNT, 0);
            unit.roleCount = attr.getIntAttribute(ATTR_ROLE_COUNT, -1);
            unit.name = attr.getStrAttribute(ATTR_NAME);
            unit.experience = attr.getIntAttribute(ATTR_EXPERIENCE, 0);
            unit.indianSettlement = attr.getStrAttribute(ATTR_INDIAN_SETTLEMENT);
            
            unit.destinationType = attr.getEnumAttribute(MoveDestinationType.class, ATTR_DESTINATION_TYPE);
            if (MoveDestinationType.TILE.equals(unit.destinationType)) {
            	unit.destinationX = attr.getIntAttribute(ATTR_DESTINATION_X);
            	unit.destinationY = attr.getIntAttribute(ATTR_DESTINATION_Y);
            }
            
            unit.workLeft = attr.getIntAttribute(ATTR_WORK_LEFT, -1);
            String tileImprovementTypeId = attr.getStrAttribute(ATTR_TILE_IMPROVEMENT_TYPE_ID);
            if (tileImprovementTypeId != null) {
            	unit.tileImprovementType = Specification.instance.tileImprovementTypes.getById(tileImprovementTypeId);
            }
            
            nodeObject = unit;
        }

        @Override
        public void startWriteAttr(Unit unit, XmlNodeAttributesWriter attr) throws IOException {
        	attr.setId(unit);

        	attr.set(ATTR_UNIT_TYPE, unit.unitType);
        	attr.set(ATTR_ROLE, unit.unitRole);
        	attr.set(ATTR_OWNER, unit.owner);
            
        	attr.set(ATTR_STATE, unit.state);
        	attr.set(ATTR_MOVES_LEFT, unit.movesLeft);
        	attr.set(ATTR_HIT_POINTS, unit.hitPoints);
        	attr.set(ATTR_VISIBLE_GOODS_COUNT, unit.visibleGoodsCount);
        	attr.set(ATTR_TREASURE_AMOUNT, unit.treasureAmount);
        	attr.set(ATTR_ROLE_COUNT, unit.roleCount);
        	attr.set(ATTR_NAME, unit.name);
        	attr.set(ATTR_EXPERIENCE, unit.experience);
        	attr.set(ATTR_INDIAN_SETTLEMENT, unit.indianSettlement);
            
        	attr.set(ATTR_DESTINATION_TYPE, unit.destinationType);
            if (MoveDestinationType.TILE.equals(unit.destinationType)) {
            	attr.set(ATTR_DESTINATION_X, unit.destinationX);
            	attr.set(ATTR_DESTINATION_Y, unit.destinationY);
            }
            
        	attr.set(ATTR_WORK_LEFT, unit.workLeft);
        	attr.set(ATTR_TILE_IMPROVEMENT_TYPE_ID, unit.tileImprovementType);
        }
        
        @Override
        public String getTagName() {
            return tagName();
        }
        
        public static String tagName() {
            return "unit";
        }
    }
    
    public static UnitPredicate NOT_CARRIER_UNIT_PREDICATE = new UnitPredicate() {
		@Override
		public boolean obtains(Unit unit) {
			return !unit.isCarrier();
		}
	};

    public static UnitPredicate CARRIER_UNIT_PREDICATE = new UnitPredicate() {
		@Override
		public boolean obtains(Unit unit) {
			return unit.isCarrier();
		}
	};
	
    /**
     * A predicate that can be applied to a unit.
     */
    public static abstract class UnitPredicate {
        public abstract boolean obtains(Unit unit);
    }

    /**
     * A predicate for determining active units.
     */
    public static class ActivePredicate extends UnitPredicate {

        /**
         * Is the unit active and going nowhere, and thus available to
         * be moved by the player?
         *
         * @return True if the unit can be moved.
         */
        public boolean obtains(Unit unit) {
            return unit.location instanceof Tile && unit.couldMove();
        }
    }
}
