package promitech.colonization.orders.move;

import promitech.colonization.Direction;
import net.sf.freecol.common.model.MoveType;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.Unit.UnitState;
import net.sf.freecol.common.model.UnitMoveType;
import net.sf.freecol.common.model.map.path.Path;

public class MoveContext {
	private final UnitMoveType unitMoveType = new UnitMoveType();

	public Unit unit;
	public Tile sourceTile;
	public Tile destTile;
	private Direction direction;
	public MoveType moveType;
	private int moveCost;
	private Unit carrierToEmbark = null;
	
	private final Path path;
	private boolean endOfPath = false;
	private boolean hasMovePoints = false;
	private boolean moveViaHighSea = false;
	private boolean moveAfterAttack = false;

	public static MoveContext embarkUnit(Unit unit, Unit carrier) {
		MoveContext moveContext = new MoveContext(unit.getTile(), carrier.getTile(), unit);
		moveContext.carrierToEmbark = carrier;
		return moveContext;
	}
	
	public MoveContext() {
		this.path = null;
	}
	
	public MoveContext(Tile sourceTile, Tile destTile, Unit unit) {
		this(sourceTile, destTile, unit, Direction.fromCoordinates(sourceTile.x, sourceTile.y, destTile.x, destTile.y));
	}
	
	public MoveContext(Tile sourceTile, Tile destTile, Unit unit, Direction direction) {
		this.path = null;
		this.unit = unit;
		this.sourceTile = sourceTile;
		this.destTile = destTile;
		this.direction = direction;
		
		initMoveCostAndType();
	}

	public MoveContext(Unit unit, Path unitPath) {
		this.path = unitPath;
		this.unit = unit;
	}

	public boolean isAi() {
		return unit.getOwner().isAi();
	}
	
	public boolean isHuman() {
		return unit.getOwner().isHuman();
	}

	public void init(Tile sourceTile, Tile destTile, Unit unit) {
		init(sourceTile, destTile, unit, Direction.fromCoordinates(sourceTile.x, sourceTile.y, destTile.x, destTile.y));
	}
	
	public void init(Tile sourceTile, Tile destTile, Unit unit, Direction direction) {
		this.sourceTile = sourceTile;
		this.destTile = destTile;
		this.direction = direction;
		this.unit = unit;
		
		initMoveCostAndType();
	}
	
	public void changeCords(Tile sourceTile, Tile destTile, Direction direction) {
		
		initMoveCostAndType();
	}
	
	public void initNextPathStep() {
		if (path.hasNotTilesToMove()) {
			endOfPath = true;
			return;
		}
		sourceTile = path.moveStepSource();
		destTile = path.moveStepDest();
		direction = Direction.fromCoordinates(sourceTile.x, sourceTile.y, destTile.x, destTile.y);

		initMoveCostAndType();
	}
	
	private void initMoveCostAndType() {
		moveCost = unit.getMoveCost(sourceTile, destTile, direction);
		if (unit.hasMovesPoints(moveCost)) {
			hasMovePoints = true;
		} else {
			hasMovePoints = false;
			return;
		}
		this.moveType = unitMoveType.calculateMoveType(unit, sourceTile, destTile);
	}
	
	public String toString() {
		String st = "moveType: " + moveType;
		st += " hasMovePoints: " + hasMovePoints;
		st += " " + unit.getId() + " " + unit.unitType.getId();
		if (sourceTile != null) {
			st += " from[" + sourceTile.x + "," + sourceTile.y + "]";
		} else {
			st += " from[null]";
		}
		if (destTile != null) {
			st += " to[" + destTile.x + "," + destTile.y +"]";
		} else {
			st += " to[null]";
		}
		st += " direction: " + direction; 
		return st;
	}
	
	public void handleMove() {
		switch (moveType) {
		    case MOVE_HIGH_SEAS:
		    case EXPLORE_LOST_CITY_RUMOUR:
			case MOVE:
				if (path != null) {
					path.removeFirst();
				}
				moveUnit();
			break;
			case ENTER_FOREIGN_COLONY_WITH_SCOUT: 
			case ATTACK_UNIT:
			case ATTACK_SETTLEMENT:
			    if (moveAfterAttack) {
			        unit.changeUnitLocation(destTile);
			    }
			    unit.reduceMovesLeftToZero();
			break;
			case EMBARK:
				if (path != null) {
					path.removeFirst();
				}
				embarkUnit();
			break;
			default:
				System.out.println("not handled move type: " + moveType);
		}
	}
	
	private void embarkUnit() {
	    if (carrierToEmbark != null) {
	        unit.embarkTo(carrierToEmbark);
	    } else {
	        unit.embarkCarrierOnTile(destTile);
	    }
		System.out.println("moveContext.embarkUnit = " + this);
	}
	
	private void moveUnit() {
		unit.setState(UnitState.ACTIVE);
		unit.setStateToAllChildren(UnitState.SENTRY);
		unit.reduceMovesLeft(moveCost);
		if (sourceTile.hasSettlement() && unit.getOwner().isHuman()) {
			unit.embarkUnitsFromLocation(sourceTile);
		}
		unit.changeUnitLocation(destTile);
	}
	
	public boolean canHandleMove() {
		return hasMovePoints && !endOfPath && (
				MoveType.MOVE.equals(moveType) || 
				MoveType.MOVE_HIGH_SEAS.equals(moveType) || 
				MoveType.EMBARK.equals(moveType) ||
				MoveType.DISEMBARK.equals(moveType) ||
				MoveType.EXPLORE_LOST_CITY_RUMOUR.equals(moveType) ||
				MoveType.ATTACK_UNIT.equals(moveType) ||
				MoveType.ATTACK_SETTLEMENT.equals(moveType) ||
				MoveType.ENTER_FOREIGN_COLONY_WITH_SCOUT.equals(moveType) ||
				MoveType.ENTER_INDIAN_SETTLEMENT_WITH_SCOUT.equals(moveType) ||
				MoveType.ENTER_INDIAN_SETTLEMENT_WITH_FREE_COLONIST.equals(moveType) ||
				MoveType.ENTER_INDIAN_SETTLEMENT_WITH_MISSIONARY.equals(moveType) ||
				MoveType.ENTER_SETTLEMENT_WITH_CARRIER_AND_GOODS.equals(moveType) ||
				MoveType.MOVE_CASH_IN_TREASURE.equals(moveType)
		);
	}

	public boolean isMoveTypeRevealMap() {
        return 
    		MoveType.MOVE.equals(moveType) ||
	        MoveType.MOVE_HIGH_SEAS.equals(moveType) ||
			MoveType.DISEMBARK.equals(moveType) ||
			MoveType.EXPLORE_LOST_CITY_RUMOUR.equals(moveType);
	}
	
	public boolean isMoveType() {
		return MoveType.MOVE.equals(moveType);
	}

	public boolean isRequireUserInteraction() {
		switch (moveType) {
		case DISEMBARK: // ask which units disembark
		case EXPLORE_LOST_CITY_RUMOUR: // ask for sure
			return true;
		case MOVE_HIGH_SEAS: {
			// show question dialog are you sure
			if (moveViaHighSea || isMoveViaPath() && path.isPathToEurope()) {
				return false;
			} else {
				return true;
			}
		}
		case MOVE_CASH_IN_TREASURE:
		case ATTACK_UNIT: 
		case ATTACK_SETTLEMENT:
		case ENTER_FOREIGN_COLONY_WITH_SCOUT:
		case ENTER_INDIAN_SETTLEMENT_WITH_SCOUT:
		case ENTER_INDIAN_SETTLEMENT_WITH_FREE_COLONIST:
		case ENTER_INDIAN_SETTLEMENT_WITH_MISSIONARY:
		case ENTER_SETTLEMENT_WITH_CARRIER_AND_GOODS:
			return true;
		default:
			return false;
		}
	}
	
	public boolean isMoveViaPath() {
		return path != null;
	}

	public boolean isEndOfPath() {
		return endOfPath;
	}

	public boolean isMoveType(MoveType moveType) {
		return moveType.equals(this.moveType);
	}

	public void setMoveViaHighSea() {
	    moveViaHighSea = true;
	}
	
	public void setMoveAfterAttack() {
	    moveAfterAttack = true;
	}

}
