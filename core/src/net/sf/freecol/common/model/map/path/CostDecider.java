package net.sf.freecol.common.model.map.path;

import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.MoveType;
import net.sf.freecol.common.model.UnitMoveType;

import promitech.colonization.Direction;

interface CostDecider {

    void init(Map map, UnitMoveType unitMove);

    boolean isAllowCarrierEnterWithGoods();

    boolean calculateAndImproveMove(Node currentNode, Node moveNode, MoveType moveType, Direction moveDirection);

    boolean isMarkDestTileAsUnaccessible(Node source, Node dest, MoveType moveType);

    boolean isMoveImproved();
}
