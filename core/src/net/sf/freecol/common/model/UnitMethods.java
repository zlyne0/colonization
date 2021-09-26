package net.sf.freecol.common.model;

import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.Modifier;

public class UnitMethods {

    /**
     * Would this unit be beached if it was on a particular tile?
     *
     * @param tile The <code>Tile</code> to check.
     * @param unitType unitType
     * @return True if the unit is a beached ship.
     */
    public static boolean isBeached(Tile tile, UnitType unitType) {
        return unitType.isNaval() && tile != null && tile.getType().isLand() && !tile.hasSettlement();
    }

    public static int initialMoves(Player owner, UnitType unitType, UnitRole unitRole) {
        float m = owner.getFeatures().applyModifier(
            Modifier.MOVEMENT_BONUS,
            unitType.getMovement(),
            unitType
        );
        return (int)unitRole.applyModifier(Modifier.MOVEMENT_BONUS, m);
    }

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
