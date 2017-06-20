package promitech.colonization.gamelogic;

import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.specification.GameOptions;

public class BuildColonyOrder {

	public static enum OrderStatus {
		OK,
		NO_MOVE_POINTS,
		UNIT_CAN_NOT_BUILD_COLONY,
		INCORRECT_TILE,
		MAP_EDGE,
		COLONY_IN_RANGE;
	}
	
	private final Map map;
	
	public BuildColonyOrder(Map map) {
		this.map = map;
	}
	
	public OrderStatus check(Unit unit, Tile tile) {
		if (!tile.getType().canSettle()) {
			System.out.println("can not settle on tile type " + tile.getType());
			return OrderStatus.INCORRECT_TILE;
		}
		if (map.isOnMapEdge(tile)) {
			System.out.println("can not settle on map edge");
			return OrderStatus.MAP_EDGE;
		}
		if (map.hasColonyInRange(tile, 1)) {
			System.out.println("another colony in one tile range");
			return OrderStatus.COLONY_IN_RANGE;
		}
		if (hasNoMovePoints(unit)) {
			return OrderStatus.NO_MOVE_POINTS;
		}
		if (unitCanNotBuildColony(unit)) {
			System.out.println("unit can not build colony");
			return OrderStatus.UNIT_CAN_NOT_BUILD_COLONY;
		}
		return OrderStatus.OK;
	}
	
	public boolean hasNoMovePoints(Unit unit) {
		return !unit.hasMovesPoints();
	}
	
	public boolean unitCanNotBuildColony(Unit unit) {
		return !(unit.unitType.canBuildColony() 
				&& (!unit.getOwner().isRebel() || Specification.options.getBoolean(GameOptions.FOUND_COLONY_DURING_REBELLION)));
	}
	
}
