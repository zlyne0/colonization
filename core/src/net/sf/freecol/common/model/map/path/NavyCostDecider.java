package net.sf.freecol.common.model.map.path;

import net.sf.freecol.common.model.MoveType;
import net.sf.freecol.common.model.Tile;
import promitech.colonization.Direction;

class NavyCostDecider extends CostDecider {
    
    private boolean moveToSeaside = false;
    
    @Override
    boolean calculateAndImproveMove(Node currentNode, Node moveNode, MoveType moveType, Direction moveDirection) {
        moveToSeaside = false;
        if ((moveType == MoveType.DISEMBARK || moveType == MoveType.MOVE_NO_ACCESS_LAND) && currentNode.tile.getType().isWater()) {
            moveType = MoveType.MOVE;
            moveToSeaside = true;
        }
        // check whether tile accessible
        // if moveNode.tile is land moveType == MoveType.MOVE_NO_ACCESS_LAND
        if (isMoveIllegal(moveNode.tile, moveType)) {
        	return false;
        }
        // check whether tile can be bombarded by colony or hostile ship
        if (isTileThreatForUnit(moveNode)) {
            // when there is threat it use all moves points
            moveCost = currentNode.unitMovesLeft;
        } else {
            // tile is not bombarded so use common move cost
            getCost(currentNode.tile, moveNode.tile, currentNode.unitMovesLeft, moveType, moveDirection);
        }
        boolean improveMove = improveMove(currentNode, moveNode);
        if (moveToSeaside) {
            // return false so no process other tiles from source tile (currentNode)
            return false;
        }
        return improveMove;
    }

	boolean isMarkDestTileAsUnaccessible(Node source, Node dest, MoveType moveType) {
		if (moveType == MoveType.MOVE_NO_ACCESS_LAND && source.tile.hasSettlement() && dest.tile.getType().isLand()) {
			return false;
		}
		return isMoveIllegal();
	}
    
    private boolean isTileThreatForUnit(Node moveNode) {
        if (moveNode.tileBombardedMetaData) {
            return moveNode.tileBombarded;
        }
        moveNode.tileBombardedMetaData = true;
        
        for (int i=0; i<Direction.values().length; i++) {
            Direction direction = Direction.values()[i];
            Tile neighbourToMoveTile = map.getTile(moveNode.tile, direction);
            if (neighbourToMoveTile == null) {
            	continue;
            }
            if (neighbourToMoveTile.hasSettlement()) {
                if (neighbourToMoveTile.isColonyOnTileThatCanBombardNavyUnit(moveUnit.getOwner(), moveUnitPiracy)) {
                    costMovesLeft = 0;
                    costNewTurns = 1;
                    
                    moveNode.tileBombarded = true;
                    return true;
                }
            } else {
                boolean useAllMove = neighbourToMoveTile.isTileHasNavyUnitThatCanBombardUnit(moveUnit.getOwner(), moveUnitPiracy);
                if (useAllMove) {
                    costMovesLeft = 0;
                    costNewTurns = 1;
                    moveNode.tileBombarded = true;
                    return true;
                }                
            }
        }
        moveNode.tileBombarded = false;
        return false;
    }
}