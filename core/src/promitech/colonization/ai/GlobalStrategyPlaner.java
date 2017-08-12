package promitech.colonization.ai;

import net.sf.freecol.common.model.Unit;

public class GlobalStrategyPlaner {

    public static Unit findCarrierToRellocateUnit(Unit unit) {
        Unit ship = null;
        for (Unit u : unit.getOwner().units.entities()) {
            if (u.isNaval() && u.isCarrier()) {
                ship = u;
            }
        }
        return ship;
    }

}
