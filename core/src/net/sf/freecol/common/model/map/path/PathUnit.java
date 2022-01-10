package net.sf.freecol.common.model.map.path;

import com.badlogic.gdx.utils.Pool;

import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitMoveType;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.player.Player;

public class PathUnit implements Pool.Poolable {
    final UnitMoveType unitMove = new UnitMoveType();
    int movesLeft;

    public PathUnit init(Unit unit) {
        this.movesLeft = unit.getMovesLeft();
        this.unitMove.init(unit);
        return this;
    }

    public PathUnit init(Player player, UnitType unitType) {
        this.unitMove.init(player, unitType);
        this.movesLeft = unitMove.initialMoves();
        return this;
    }

    boolean isNaval() {
        return unitMove.getUnitType().isNaval();
    }

    @Override
    public void reset() {
        this.movesLeft = 0;
        this.unitMove.reset();
    }
}
