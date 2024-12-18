package net.sf.freecol.common.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.ObjectIntMap.Entry;

import net.sf.freecol.common.model.colonyproduction.GoodsCollection;
import net.sf.freecol.common.model.player.HighSeas;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.AbstractGoods;
import net.sf.freecol.common.model.specification.GameOptions;
import net.sf.freecol.common.model.specification.Modifier;
import net.sf.freecol.common.model.specification.ScopeAppliable;
import net.sf.freecol.common.model.specification.UnitTypeChange.ChangeType;
import promitech.colonization.savegame.ObjectFromNodeSetter;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;
import promitech.map.isometric.IterableSpiral;

public class Unit extends ObjectWithId implements UnitLocation, ScopeAppliable {

	public static final Comparator<Unit> EXPERTS_LAST_COMPARATOR = new Comparator<Unit>() {
		@Override
		public int compare(Unit unit1, Unit unit2) {
			if (unit1.isExpert() && unit2.isExpert()) {
				return 0;
			}
			if (unit1.isExpert() && !unit2.isExpert()) {
				return 1;
			}
			if (!unit1.isExpert() && unit2.isExpert()) {
				return -1;
			}
			return 0;
		}
	};

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
    private GridPoint2 enterHighSea;
    
    private String indianSettlement;

    /**
     * The amount of role-equipment this unit carries, subject to
     * role.getMaximumCount().  Currently zero or one except for pioneers.
     */
    protected int roleCount = -1;
    
    private UnitContainer unitContainer = null;
    private GoodsContainer goodsContainer = null;
    private TradeRoute tradeRoute; 
    
    protected Unit(String id) {
    	super(id);
    }
    
    public Unit(String id, UnitType aUnitType, UnitRole aUnitRole, Player anOwner) {
    	super(id);
    	this.unitType = aUnitType;
    	this.unitRole = aUnitRole;
    	this.owner = anOwner;
    	
    	this.movesLeft = Unit.initialMoves(owner, unitType, unitRole);
    	this.hitPoints = unitType.getHitPoints();
    	
        if (unitType.canCarryUnits()) {
            unitContainer = new UnitContainer();
        }
        if (unitType.hasAbility(Ability.CARRY_GOODS)) {
        	goodsContainer = new GoodsContainer();
        }
        roleCount = unitRole.getMaximumCount();
    }

	public String toString() {
        return "id = " + id + ", unitType = " + unitType + ", workLeft = " + workLeft;
    }
    
	public String toStringTypeLocation() {
		return "" + id + " " + unitType + " " + location;
	}
	
    public String resourceImageKey() {
    	if (!owner.nationType().isEuropean()) {
    		if (unitType.isType(UnitType.FREE_COLONIST)) {
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

	public boolean isAtTileLocation() {
    	return isAtLocation(Tile.class);
	}

	public boolean isAtUnitLocation() {
		return isAtLocation(Unit.class);
	}

	public boolean isAtEuropeLocation() {
        return isAtLocation(Europe.class);
    }

    public boolean isAtHighSeasLocation() {
    	return isAtLocation(HighSeas.class);
	}

	public boolean isAtColonyLocation() {
    	return isAtLocation(Colony.class);
	}

	public Colony getColonyLocation() {
		if (location != null) {
			if (location instanceof Colony) {
				return (Colony) location;
			}
			if (location instanceof Tile) {
				Tile tile = (Tile) location;
				if (tile.hasSettlement()) {
					return tile.getSettlement().asColony();
				}
			}
		}
		return null;
	}

	public boolean isAtLocation(Class<? extends UnitLocation> unitLocationClass) {
	    return location != null && location.getClass().equals(unitLocationClass);
	}

	public boolean isAtLocation(Tile tile) {
    	return location instanceof Tile && ((Tile)location).equalsCoordinates(tile);
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
		}
		newUnitLocation.addUnit(this);
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
	
    public boolean canAddUnit(Player unitOwner, UnitType unitType) {
    	if (unitContainer == null) {
    	    return false;
    	}
    	return unitContainer.canAdd(this, unitOwner, unitType);
    }

	private boolean canAddUnit(Unit unit) {
		if (unitContainer == null) {
			return false;
		}
		return unitContainer.canAdd(this, unit.owner, unit.unitType);
	}

	public void loadCargo(AbstractGoods anAbstractGood) {
		goodsContainer.increaseGoodsQuantity(anAbstractGood);
		if (!hasFullMovesPoints()) {
			reduceMovesLeftToZero();
		}
	}

	public void unloadCargo(AbstractGoods anAbstractGood) {
		goodsContainer.decreaseGoodsQuantity(anAbstractGood);
		if (!hasFullMovesPoints()) {
			reduceMovesLeftToZero();
		}
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
		float lineOfSight = unitType.lineOfSight();
		lineOfSight = unitType.applyModifier(Modifier.LINE_OF_SIGHT_BONUS, unitType.lineOfSight());
		lineOfSight = owner.getFeatures().applyModifier(Modifier.LINE_OF_SIGHT_BONUS, lineOfSight, unitType);
		lineOfSight = unitRole.applyModifier(Modifier.LINE_OF_SIGHT_BONUS, lineOfSight, unitRole);
		return (int)lineOfSight;
	}
	
	public void partlyRepair() {
		hitPoints++;
	}

    public boolean isDamaged() {
        return hitPoints < unitType.getHitPoints();
    }

	public int getHitPoints() {
		return hitPoints;
	}

	public boolean isOnCarrier() {
        return location != null && location instanceof Unit;
    }
    
    public boolean hasMoreFreeCargoSpace(Unit u) {
    	if (goodsContainer == null) {
    		return false;
    	}
    	if (freeCargoSlots() == 0) {
    		return false;
    	}
    	if (u == null) {
    		return true;
    	}
    	return freeCargoSlots() > u.freeCargoSlots();
    }
    
    public int freeCargoSlots() {
    	return unitType.getSpace() - getSpaceTaken();
    }

	public int allCargoSlotsAndFreeSlots() {
		int space = 0;
		if (unitContainer != null) {
			space += unitContainer.getSpaceTakenByUnits();
		}
		return unitType.getSpace() - space;
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

    public int freeUnitsSlots() {
    	return unitType.getSpace() - getSpaceTaken(); 
    }
    
    public boolean hasSpaceForAdditionalUnit(UnitType additionalUnitType) {
    	return getSpaceTaken() + additionalUnitType.getSpaceTaken() <= unitType.getSpace();
    }
    
    public boolean hasSpaceForAdditionalCargo() {
        return getSpaceTaken() < unitType.getSpace();
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

    public boolean hasSpaceForAdditionalCargo(GoodsCollection goodsCollection) {
		int space = 0;
		if (unitContainer != null) {
			space += unitContainer.getSpaceTakenByUnits();
		}
		if (goodsContainer.isEmpty()) {
			space += goodsCollection.slotsAmount();
		} else {
			ProductionSummary tmpCargo = goodsContainer.cloneGoods();
			tmpCargo.addGoods(goodsCollection);
			space += tmpCargo.allCargoSlots();
		}
		return space <= unitType.getSpace();
	}

	public int maxGoodsAmountToFillFreeSlots(String goodsTypeId) {
		int cargoSlots = allGoodsCargoSlots();
		return goodsContainer.maxGoodsAmountToFillFreeSlots(goodsTypeId, cargoSlots);
	}
    
	/**
	 * @return Total goods cargo slots
	 */
	public int allGoodsCargoSlots() {
		int cargoSlots = unitType.getSpace();
		if (unitContainer != null) {
			cargoSlots -= unitContainer.getSpaceTakenByUnits();
		}
		if (goodsContainer == null) {
			return 0;
		}
		return cargoSlots;
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

    public final TradeRoute getTradeRoute() {
        return tradeRoute;
    }

    public final boolean isTradeRouteSet() {
    	return tradeRoute != null;
    }
    
    public final void setTradeRoute(TradeRoute tradeRoute) {
    	this.tradeRoute = tradeRoute;
    	clearDestination();
    }
    
    public final void removeTradeRoute() {
    	this.tradeRoute = null;
    	setState(UnitState.ACTIVE);
    	clearDestination();
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
    
    public boolean isSentry() {
    	return state == UnitState.SENTRY;
    }
    
	public TileImprovementType getTileImprovementType() {
		return tileImprovementType;
	}
    
    public boolean isNaval() {
    	return unitType.isNaval();
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
    	return unitType.isPerson();
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
    
    private int getGoodsSpaceTaken() {
        if (goodsContainer == null) {
        	return 0;
        }
        return goodsContainer.getCargoSpaceTaken();
    }
    
    public void transferAllGoods(Unit toUnit) {
    	if (goodsContainer == null) {
    		return;
    	}
    	if (toUnit.goodsContainer == null) {
    		return;
    	}
    	for (Entry<String> transferedGoods : goodsContainer.entries()) {
			int max = toUnit.maxGoodsAmountToFillFreeSlots(transferedGoods.key);
			if (max > 0) {
				toUnit.goodsContainer.increaseGoodsQuantity(
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

    public int treasureTransportFee() {
        if (isAtLocation(Europe.class) || owner.getEurope() == null) {
            return 0;
        }
        float fee = (Specification.options.getIntValue(GameOptions.TREASURE_TRANSPORT_FEE) * treasureAmount) / 100f;
        return (int)owner.getFeatures().applyModifier(Modifier.TREASURE_TRANSPORT_FEE, fee);
    }

    public boolean isBeached() {
    	return Unit.isBeached(this.getTileLocationOrNull(), unitType);
    }

	public void fortify() {
		setState(UnitState.FORTIFYING);
		clearDestination();
	}

	public void clearOrders() {
		removeTradeRoute();
		setState(UnitState.ACTIVE);
		clearDestination();
	}

	public void sentry() {
		removeTradeRoute();
		setState(UnitState.SENTRY);
		clearDestination();
	}

	public void activate() {
		removeTradeRoute();
		setState(UnitState.ACTIVE);
		clearDestination();
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
				case SENTRY:
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
            return state == UnitState.FORTIFYING;
        case FORTIFYING:
            return getMovesLeft() > 0 && state != UnitState.FORTIFIED;
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
			movesLeft = Unit.initialMoves(owner, unitType, unitRole);
		}
	}

	public int initialMoves() {
    	return Unit.initialMoves(owner, unitType, unitRole);
	}

	public boolean hasFullMovesPoints() {
		return movesLeft == Unit.initialMoves(owner, unitType, unitRole);
	}
	
    public int getMovesLeft() {
        return movesLeft;
    }
    
    public boolean hasMovesPoints() {
    	return movesLeft > 0;
    }
	
    public UnitContainer getUnitContainer() {
        return unitContainer;
    }

	public boolean canCarryUnits() {
		return unitContainer != null;
	}

	@Override
	public MapIdEntitiesReadOnly<Unit> getUnits() {
		return unitContainer.getUnits();
	}
    
    @Override
    public void addUnit(Unit unit) {
		if (!unitType.canCarryUnits()) {
			throw new IllegalStateException("unit[" + this + "] has not ability carry unit but try add unit to it");
		}
        unitContainer.addUnit(unit);
    }

    @Override
    public void removeUnit(Unit unit) {
        unitContainer.getUnits().removeId(unit);
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
		return unitType.isExpert();
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

	public boolean isWorkingOnImprovement() {
		return state == UnitState.IMPROVING;
	}

	public void sailOnHighSea() {
	    workLeft -= 1;
	}
	
	public void sailUnitToEurope(Tile highSeaEntrence) {
		enterHighSea = new GridPoint2(highSeaEntrence.x, highSeaEntrence.y);
	    changeUnitLocation(owner.getHighSeas());
	    reduceMovesLeftToZero();
	    setDestinationEurope();
	    workLeft = getSailTurns();
	}
	
	public void embarkUnitsFromLocation(UnitLocation anUnitLocation) {
		if (hasNoSpace()) {
			return;
		}
		if (!unitType.canCarryUnits()) {
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
	
	public void disembarkUnitsToLocation(UnitLocation newUnitLocation) {
		if (unitContainer != null && unitContainer.isNotEmpty()) {
			for (Unit unit : unitContainer.getUnits().entities()) {
				newUnitLocation.addUnit(unit);
				unit.location = newUnitLocation;
				unit.setState(UnitState.ACTIVE);
			}
			unitContainer.getUnits().clear();
		}
	}
	
	public void disembarkUnitToLocation(Tile dest, Unit unit) {
		unit.changeUnitLocation(dest);
		unit.setState(UnitState.ACTIVE);
		this.removeUnit(unit);
	}
	
	public void sailUnitToNewWorld() {
		changeUnitLocation(owner.getHighSeas());
		reduceMovesLeftToZero();
		if (enterHighSea != null) {
			setDestination(enterHighSea.x, enterHighSea.y);
		} else {
			setDestination(owner.getEntryLocation().x, owner.getEntryLocation().y);
		}
		enterHighSea = null;
		workLeft = getSailTurns();
	}
	
	public GridPoint2 getEnterHighSea() {
		return enterHighSea;
	}
	
    public int getSailTurns() {
        float base = Specification.options.getIntValue(GameOptions.TURNS_TO_SAIL);
        return (int)getOwner().getFeatures().applyModifier(Modifier.SAIL_HIGH_SEAS, base, unitType);
    }
    
	public boolean isWorkComplete() {
	    return workLeft <= 0;
	}

	public int workTurnsToComplete() {
		return workLeft;
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
	
	public boolean isBelongToIndianSettlement(IndianSettlement is) {
		return indianSettlement != null && is.equalsId(indianSettlement);
	}
	
	public boolean hasRepairLocation() {
	    return getRepairLocation() != null;
	}
	
	public UnitLocation getRepairLocation() {
	    for (Settlement settlement : getOwner().settlements.entities()) {
	        if (settlement.asColony().canRepairUnits()) {
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
	
	public boolean isSeeEnemy(IterableSpiral<Tile> is, Map map) {
		for (Tile tile : map.neighbourTiles(is, getTile(), lineOfSight())) {
			if (tile.getUnits().isNotEmpty()) {
				if (tile.getUnits().first().getOwner().notEqualsId(getOwner())) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean isSeeHostile(IterableSpiral<Tile> is, Map map) {
		for (Tile tile : map.neighbourTiles(is, getTile(), lineOfSight())) {
			Player tileOwner = null;
			if (tile.hasSettlement()) {
				tileOwner = tile.getSettlement().getOwner();
			} else {
				if (tile.getUnits().isNotEmpty()) {
					tileOwner = tile.getUnits().first().getOwner();
				}
			}
			if (tileOwner != null) {
				if (getOwner().atWarWith(tileOwner)) {
					return true;
				}
			}
		}
		return false;
	}

	public Tile positionRelativeToMap(Map map) {
		Tile sourceTile = getTileLocationOrNull();
		if (sourceTile == null) {
			if (isAtHighSeasLocation() || isAtEuropeLocation()) {
				if (enterHighSea != null) {
					sourceTile = map.getSafeTile(enterHighSea);
				} else {
					sourceTile = map.getSafeTile(owner.getEntryLocation());
				}
			} else if (isAtUnitLocation()) {
				Unit carrier = getLocationOrNull(Unit.class);
				return carrier.positionRelativeToMap(map);
			}
		}
		return sourceTile;
	}

    public static class Xml extends XmlNodeParser<Unit> {
        
    	private static final String ATTR_ENTER_HIGH_SEA = "enterHighSea";
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
            addNode(TradeRoute.class, "tradeRoute");
        }

        @Override
        public void startElement(XmlNodeAttributes attr) {
            String unitTypeStr = attr.getStrAttribute(ATTR_UNIT_TYPE);
            String unitRoleStr = attr.getStrAttribute(ATTR_ROLE);
            String ownerStr = attr.getStrAttribute(ATTR_OWNER);
            
            Player unitOwner = game.players.getById(ownerStr);
			Unit unit = new Unit(
        		attr.getStrAttribute(ATTR_ID),
        		Specification.instance.unitTypes.getById(unitTypeStr),
        		Specification.instance.unitRoles.getById(unitRoleStr),
        		unitOwner
            );
			unitOwner.units.add(unit);
            
            unit.state = attr.getEnumAttribute(UnitState.class, ATTR_STATE);
            unit.movesLeft = attr.getIntAttribute(ATTR_MOVES_LEFT);
            unit.hitPoints = attr.getIntAttribute(ATTR_HIT_POINTS, 0);
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
            if (attr.hasAttr(ATTR_ENTER_HIGH_SEA)) {
            	unit.enterHighSea = new GridPoint2(attr.getPoint(ATTR_ENTER_HIGH_SEA));
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
        	attr.set(ATTR_HIT_POINTS, unit.hitPoints, 0);
        	attr.set(ATTR_VISIBLE_GOODS_COUNT, unit.visibleGoodsCount, -1);
        	attr.set(ATTR_TREASURE_AMOUNT, unit.treasureAmount, 0);
        	attr.set(ATTR_ROLE_COUNT, unit.roleCount, -1);
        	attr.set(ATTR_NAME, unit.name);
        	attr.set(ATTR_EXPERIENCE, unit.experience, 0);
        	attr.set(ATTR_INDIAN_SETTLEMENT, unit.indianSettlement);
            
        	attr.set(ATTR_DESTINATION_TYPE, unit.destinationType);
            if (MoveDestinationType.TILE.equals(unit.destinationType)) {
            	attr.set(ATTR_DESTINATION_X, unit.destinationX);
            	attr.set(ATTR_DESTINATION_Y, unit.destinationY);
            }
            if (unit.enterHighSea != null) {
            	attr.setPoint(ATTR_ENTER_HIGH_SEA, unit.enterHighSea);
            }
            
        	attr.set(ATTR_WORK_LEFT, unit.workLeft, -1);
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

	/**
	 * Would this unit be beached if it was on a particular tile?
	 *
	 * @param tile The <code>Tile</code> to check.
	 * @param unitType unitType
	 * @return True if the unit is a beached ship.
	 */
	public static boolean isBeached(Tile tile, UnitType unitType) {
		return unitType.isNaval() && tile != null && tile.getType().isLand() && !tile.hasSettlement();
	}

	public static int initialMoves(Player owner, UnitType unitType, UnitRole unitRole) {
		float m = owner.getFeatures().applyModifier(
			Modifier.MOVEMENT_BONUS,
			unitType.getMovement(),
			unitType
		);
		return (int)unitRole.applyModifier(Modifier.MOVEMENT_BONUS, m);
	}

	public static boolean canCashInTreasureInLocation(Player owner, UnitLocation unitLocation) {
		if (unitLocation == null) {
			throw new IllegalStateException("can not cash in treasure in null location");
		}
		if (owner.getEurope() == null) {
			// when inpedence any colony can cash in treasure
			return unitLocation instanceof Tile && ((Tile)unitLocation).hasSettlement();
		}
		if (unitLocation instanceof Europe) {
			return true;
		}
		if (unitLocation instanceof Tile) {
			Tile locTile = (Tile)unitLocation;
			if (locTile.hasSettlement() && locTile.getSettlement().isColony()) {
				if (locTile.getSettlement().asColony().hasSeaConnectionToEurope() && !owner.hasUnitType(UnitType.GALLEON)) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isColonist(UnitType unitType, Player owner) {
		return unitType.hasAbility(Ability.FOUND_COLONY) && owner.getFeatures().hasAbility(Ability.FOUNDS_COLONIES);
	}

	public static boolean isOffensiveUnit(Unit unit) {
		return unit.unitType.isOffensive() || unit.unitRole.isOffensive();
	}

	public static boolean isOffensiveUnit(UnitType unitType, UnitRole unitRole) {
		return unitType.isOffensive() || unitRole.isOffensive();
	}

}
