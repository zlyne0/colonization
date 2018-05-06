package net.sf.freecol.common.model.map.path;

import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.MoveType;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.specification.Ability;
import promitech.colonization.Direction;

// cost for move by civilian unit into foreign settlement is delt by moveType
// the same case for move to foreign unit
// the same case for defender unit
class CostDecider {
    private static final int TURN_FACTOR = 100;
	private static final int ILLEGAL_MOVE_COST = -1;

	protected Map map;
	protected Unit moveUnit;
	protected int unitInitialMoves;
	protected boolean moveUnitPiracy = false;

	protected int costNewTurns;
	protected int costMovesLeft;
	
	protected int moveCost;
	private int newTotalPathCost;
	protected boolean avoidUnexploredTiles = true;

    void init(Map map, Unit moveUnit) {
        this.map = map;
        this.unitInitialMoves = moveUnit.getInitialMovesLeft();
        this.moveUnit = moveUnit;
        moveUnitPiracy = moveUnit.unitType.hasAbility(Ability.PIRACY);
    }
	
    /**
     * @return boolean - return true when can improve move
     */
	boolean calculateAndImproveMove(Node currentNode, Node moveNode, MoveType moveType, Direction moveDirection) {
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
	
	boolean isMarkDestTileAsUnaccessible(Node source, Node dest, MoveType moveType) {
		return isMoveIllegal();
	}
	
	boolean isMoveIllegal() {
		return moveCost == ILLEGAL_MOVE_COST;
	}
	
	protected void getCost(Tile oldTile, Tile newTile, final int movesLeftBefore, MoveType moveType, Direction moveDirection) {
		int cost = moveUnit.getMoveCost(oldTile, newTile, moveDirection, movesLeftBefore);
		if (cost > movesLeftBefore) {
            final int moveCostNextTurn = moveUnit.getMoveCost(oldTile, newTile, moveDirection, unitInitialMoves);
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
		if (MoveType.ENTER_SETTLEMENT_WITH_CARRIER_AND_GOODS.equals(moveType)) {
			if (newTile.getSettlement() != null && !newTile.getSettlement().getOwner().equalsId(moveUnit.getOwner())) {
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
		
	    if (avoidUnexploredTiles && moveUnit.getOwner().isTileUnExplored(newTile)) {
	        moveCost = ILLEGAL_MOVE_COST;
	        return true;
	    }
	    return false;
	}
}