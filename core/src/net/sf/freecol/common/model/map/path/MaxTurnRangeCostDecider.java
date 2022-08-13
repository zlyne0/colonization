package net.sf.freecol.common.model.map.path;

import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.MoveType;
import net.sf.freecol.common.model.UnitMoveType;

import promitech.colonization.Direction;

class MaxTurnRangeCostDecider implements CostDecider {

    protected int maxTurn = Integer.MAX_VALUE;
    protected CostDecider costDecider;

    void init(int maxTurn, CostDecider costDecider) {
        this.maxTurn = maxTurn;
        this.costDecider = costDecider;
    }

    @Override
    public void init(Map map, UnitMoveType unitMove) {
        costDecider.init(map, unitMove);
    }

    @Override
    public boolean isAllowCarrierEnterWithGoods() {
        return costDecider.isAllowCarrierEnterWithGoods();
    }

    @Override
    public boolean calculateAndImproveMove(Node currentNode, Node moveNode, MoveType moveType, Direction moveDirection) {
        boolean calculateAndImproveMove = costDecider.calculateAndImproveMove(currentNode, moveNode, moveType, moveDirection);
        if (moveNode.turns >= maxTurn) {
            return false;
        }
        return calculateAndImproveMove;
    }

    @Override
    public boolean isMarkDestTileAsUnaccessible(Node source, Node dest, MoveType moveType) {
        return costDecider.isMarkDestTileAsUnaccessible(source, dest, moveType);
    }
}
