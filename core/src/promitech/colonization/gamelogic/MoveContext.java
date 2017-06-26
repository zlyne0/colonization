package promitech.colonization.gamelogic;

import promitech.colonization.Direction;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.Unit.UnitState;
import net.sf.freecol.common.model.map.Path;

public class MoveContext {
	public Unit unit;
	public Tile sourceTile;
	public Tile destTile;
	private Direction direction;
	public MoveType moveType;
	private int moveCost;
	
	private final Path path;
	private boolean endOfPath = false;
	private boolean hasMovePoints = false;
	private boolean unitKilled = false;
	private boolean moveViaHighSea = false;

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

	public MoveContext(Path unitPath) {
		this.path = unitPath;
		this.unit = path.unit;
	}

	public boolean isAi() {
		return unit.getOwner().isAi();
	}
	
	public boolean isHuman() {
		return unit.getOwner().isHuman();
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
		this.moveType = unit.getMoveType(sourceTile, destTile);
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
			case MOVE: {
				if (path != null) {
					path.removeFirst();
				}
				moveUnit();
			} break;
			case EMBARK: {
				if (path != null) {
					path.removeFirst();
				}
				embarkUnit();
			} break;
			default: {
				System.out.println("not handled move type: " + moveType);
			}
		}
	}
	
	private void embarkUnit() {
		Unit carrier = null;
		for (Unit u : destTile.getUnits().entities()) {
			if (u.canAddUnit(unit)) {
				carrier = u;
				break;
			}
		}
		if (carrier == null) {
			throw new IllegalStateException("carrier unit unit should exists and check while generate moveType");
		}
		System.out.println("moveContext.embarkUnit = " + this);
		unit.setState(UnitState.SKIPPED);
		unit.changeUnitLocation(carrier);
		unit.reduceMovesLeftToZero();
	}
	
	private void moveUnit() {
		unit.setState(UnitState.ACTIVE);
		unit.setStateToAllChildren(UnitState.SENTRY);
		//System.out.println("moveUnit: " + unit + ", moveLeft = " + unit.getMovesLeft() + ", moveCost = " + moveCost);
		unit.reduceMovesLeft(moveCost);
		unit.changeUnitLocation(destTile);
	}
	
	public boolean canHandleMove() {
		return hasMovePoints && !endOfPath && (
				MoveType.MOVE.equals(moveType) || 
				MoveType.MOVE_HIGH_SEAS.equals(moveType) || 
				MoveType.EMBARK.equals(moveType) ||
				MoveType.DISEMBARK.equals(moveType) ||
				MoveType.EXPLORE_LOST_CITY_RUMOUR.equals(moveType)
		);
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

	public void setUnitKilled() {
		unitKilled = true;
	}
	
	public boolean isUnitKilled() {
		return unitKilled;
	}
	
	public void setMoveViaHighSea() {
	    moveViaHighSea = true;
	}

}
