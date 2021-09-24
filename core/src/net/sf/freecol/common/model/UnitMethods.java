package net.sf.freecol.common.model;

import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.Ability;

public class UnitMethods {

    public static boolean canCashInTreasureInLocation(Player owner, UnitLocation unitLocation) {
        if (unitLocation == null) {
            throw new IllegalStateException("can not cash in treasure in null location");
        }
        if (owner.getEurope() == null) {
            // when inpedence any colony can cash in treasure
            return unitLocation instanceof Tile && ((Tile)unitLocation).hasSettlement();
        }
        if (unitLocation instanceof Europe) {
            return true;
        }
        if (unitLocation instanceof Tile) {
            Tile locTile = (Tile)unitLocation;
            if (locTile.hasSettlement() && locTile.getSettlement().isColony()) {
                if (locTile.getSettlement().asColony().hasSeaConnectionToEurope() && !owner.hasUnitType(UnitType.GALLEON)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isColonist(UnitType unitType, Player owner) {
        return unitType.hasAbility(Ability.FOUND_COLONY) && owner.getFeatures().hasAbility(Ability.FOUNDS_COLONIES);
    }

    public static boolean isOffensiveUnit(Unit unit) {
        return unit.unitType.isOffensive() || unit.unitRole.isOffensive();
    }

    public static boolean isOffensiveUnit(UnitType unitType, UnitRole unitRole) {
        return unitType.isOffensive() || unitRole.isOffensive();
    }

}
