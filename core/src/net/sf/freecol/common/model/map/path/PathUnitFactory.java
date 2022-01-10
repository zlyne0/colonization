package net.sf.freecol.common.model.map.path;

import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.player.Player;

class PathUnitFactory {
    private final Pool<PathUnit> pathUnitPool;

    PathUnitFactory() {
        pathUnitPool = Pools.get(PathUnit.class, 5);
    }

    PathUnit obtain(Unit unit) {
        PathUnit pathUnit = pathUnitPool.obtain();
        pathUnit.init(unit);
        return pathUnit;
    }

    PathUnit obtain(Player player, UnitType unitType) {
        PathUnit pathUnit = pathUnitPool.obtain();
        pathUnit.init(player, unitType);
        return pathUnit;
    }

    void free(PathUnit pathUnit) {
        pathUnitPool.free(pathUnit);
    }

}
