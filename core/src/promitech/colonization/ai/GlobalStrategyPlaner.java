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

    public static void blockUnitForMission(Unit potentialCarrier, RellocationMission mission) {
    }

    public static void blockUnitsForMission(RellocationMission rellocationMission) {
    }

    public static void blockUnitsForMission(FoundColonyMission foundColonyMission) {
    }

    public static void unblockUnitFromMission(Unit carrier, RellocationMission mission) {
    }
    
    public static void unblockUnitsFromMission(RellocationMission mission) {
    }

    public static void unblockUnitsFromMission(FoundColonyMission mission) {
    }

}
