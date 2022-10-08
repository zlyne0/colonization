package net.sf.freecol.common.model.map.path;

import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.MoveType;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.UnitMoveType;
import net.sf.freecol.common.model.specification.Ability;
import promitech.colonization.Direction;

// cost for move by civilian unit into foreign settlement is delt by moveType
// the same case for move to foreign unit
// the same case for defender unit
class DefaultCostDecider implements CostDecider {
    private static final int TURN_FACTOR = 100;
	private static final int ILLEGAL_MOVE_COST = -1;

	protected UnitMoveType unitMove;
	protected Map map;
	protected int unitInitialMoves;
	protected boolean moveUnitPiracy = false;

	protected int costNewTurns;
	protected int costMovesLeft;
	
	protected int moveCost;
	private int newTotalPathCost;
	protected boolean avoidUnexploredTiles = true;
	protected boolean allowEmbark = false;
	protected boolean allowCarrierEnterWithGoods = false;

	@Override
	public void init(Map map, UnitMoveType unitMove) {
        this.map = map;
        this.unitMove = unitMove;
        this.unitInitialMoves = unitMove.initialMoves();
        this.moveUnitPiracy = unitMove.getUnitType().hasAbility(Ability.PIRACY);
    }

	@Override
	public boolean isAllowCarrierEnterWithGoods() {
		return allowCarrierEnterWithGoods;
	}

	/**
     * @return boolean - return true when can improve move
     */
    @Override
	public boolean calculateAndImproveMove(Node currentNode, Node moveNode, MoveType moveType, Direction moveDirection) {
		if (
			moveType == MoveType.ENTER_INDIAN_SETTLEMENT_WITH_SCOUT
			|| moveType == MoveType.MOVE_NO_ACCESS_EMBARK && allowEmbark
		) {
			getCost(currentNode.tile, moveNode.tile, currentNode.unitMovesLeft, moveType, moveDirection);
			improveMove(currentNode, moveNode);
			moveNode.noMove = true;
			return false;
		}

        if (isMoveIllegal(moveNode.tile, moveType)) {
        	return false;
        }
		getCost(currentNode.tile, moveNode.tile, currentNode.unitMovesLeft, moveType, moveDirection);
		return improveMove(currentNode, moveNode);
	}
	
	protected boolean improveMove(Node currentNode, Node moveNode) {
	    newTotalPathCost = TURN_FACTOR * (currentNode.turns + costNewTurns) + moveCost + currentNode.totalCost;
	    
	    if (moveNode.totalCost > newTotalPathCost) {
		    moveNode.totalCost = newTotalPathCost;
	        moveNode.unitMovesLeft = costMovesLeft;
	        moveNode.turns = currentNode.turns + costNewTurns;
            moveNode.preview = currentNode;
	        return true;
	    }
	    return false;
	}

	protected boolean improveMove(Node currentNode, Node moveNode, int additionalCost, int additionalTurnsCost) {
	    newTotalPathCost = TURN_FACTOR * (currentNode.turns + costNewTurns + additionalTurnsCost) + moveCost + currentNode.totalCost + additionalCost;
	    
	    if (moveNode.totalCost > newTotalPathCost) {
		    moveNode.totalCost = newTotalPathCost;
	        moveNode.unitMovesLeft = costMovesLeft;
	        moveNode.turns = currentNode.turns + costNewTurns + additionalTurnsCost;
            moveNode.preview = currentNode;
	        return true;
	    }
	    return false;
	}

	@Override
	public boolean isMarkDestTileAsUnaccessible(Node source, Node dest, MoveType moveType) {
		return isMoveIllegal();
	}
	
	boolean isMoveIllegal() {
		return moveCost == ILLEGAL_MOVE_COST;
	}
	
	protected void getCost(Tile oldTile, Tile newTile, final int movesLeftBefore, MoveType moveType, Direction moveDirection) {
		int cost = unitMove.caclulateMoveCost(oldTile, newTile, moveDirection, movesLeftBefore, unitInitialMoves);
		if (cost > movesLeftBefore) {
            final int moveCostNextTurn = unitMove.caclulateMoveCost(oldTile, newTile, moveDirection, unitInitialMoves, unitInitialMoves);
            cost = movesLeftBefore + moveCostNextTurn;
            costMovesLeft = unitInitialMoves - moveCostNextTurn;
            costNewTurns = 1;
		} else {
			costMovesLeft = movesLeftBefore - cost;
			costNewTurns = 0;
		}
		moveCost = cost;
	}
	
	protected boolean isMoveIllegal(Tile newTile, MoveType moveType) {
		if (MoveType.ENTER_SETTLEMENT_WITH_CARRIER_AND_GOODS.equals(moveType) || MoveType.MOVE_NO_ACCESS_GOODS.equals(moveType)) {
			if (newTile.getSettlement() != null && !newTile.getSettlement().getOwner().equalsId(unitMove.getOwner())) {
				moveCost = ILLEGAL_MOVE_COST;
				return true;
			}
		} else {
			// consider only moves without actions, actions can only happen on find path goal
			if (!moveType.isProgress()) {
				moveCost = ILLEGAL_MOVE_COST;
				return true;
			}
		}
		
	    if (avoidUnexploredTiles && unitMove.getOwner().isTileUnExplored(newTile)) {
	        moveCost = ILLEGAL_MOVE_COST;
	        return true;
	    }
	    return false;
	}
}