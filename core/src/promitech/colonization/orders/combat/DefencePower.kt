package promitech.colonization.orders.combat

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.UnitRole
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.player.Player
import net.sf.freecol.common.model.specification.BuildingType
import net.sf.freecol.common.model.specification.Modifier

object DefencePower {

    class ColonyDefenceSnapshot(val unitsPower: Float, val defenceBuildingType: BuildingType? = null) {
        val power: Float
        init {
            if (defenceBuildingType != null) {
                power = defenceBuildingType.applyModifier(Modifier.DEFENCE, unitsPower)
            } else {
                power = unitsPower
            }
        }

        fun unitsPowerWithBuilding(additionalUnitPower: Float): Float {
            if (defenceBuildingType != null) {
                return defenceBuildingType.applyModifier(Modifier.DEFENCE, unitsPower + additionalUnitPower)
            } else {
                return unitsPower + additionalUnitPower
            }
        }

        fun unitsPowerWithBuilding(additionalUnitPower: Float, buildingType: BuildingType): Float {
            return buildingType.applyModifier(Modifier.DEFENCE, unitsPower + additionalUnitPower)
        }
    }

    @JvmStatic
    fun calculateColonyDefenceSnapshot(colony: Colony): ColonyDefenceSnapshot {
        var power = 0f
        CombatSidesPool.use { combatSides ->
            for (singleUnit in colony.tile.units.entities()) {
                power += combatSides.calculateDefencePower(singleUnit.unitType, singleUnit.unitRole)
            }
        }

        var defenceBuildingType: BuildingType? = null
        for (building in colony.buildings) {
            if (building.buildingType.hasModifier(Modifier.DEFENCE)) {
                defenceBuildingType = building.buildingType
            }
        }
        return ColonyDefenceSnapshot(power, defenceBuildingType)
    }

    @JvmStatic
    fun calculatePaperDefencePower(unitType: UnitType, unitRole: UnitRole): Float {
        var power = 0f
        CombatSidesPool.use { combatSides ->
            power = combatSides.calculateDefencePower(unitType, unitRole)
        }
        return power
    }

    @JvmStatic
    fun calculatePaperDefencePower(player: Player, tile: Tile): Float {
        val firstUnit = tile.units.first()
        if (firstUnit == null) {
            if (tile.hasSettlement() && tile.settlement.owner.equalsId(player)) {
                return calculatePaperDefencePower(
                    Specification.instance.freeColonistUnitType,
                    Specification.instance.unitRoles.getById(UnitRole.DEFAULT_ROLE_ID),
                    tile
                )
            }
            return 0f
        }
        if (!firstUnit.isOwner(player)) {
            return 0f
        }
        var offencePower = 0f
        CombatSidesPool.use { combatSides ->
            for (singleUnit in tile.units.entities()) {
                offencePower += combatSides.calculateDefencePower(
                    singleUnit.unitType,
                    singleUnit.unitRole,
                    tile
                )
            }
        }
        return offencePower
    }

    @JvmStatic
    private fun calculatePaperDefencePower(unitType: UnitType, unitRole: UnitRole, tile: Tile): Float {
        var power = 0f
        CombatSidesPool.use { combatSides ->
            power = combatSides.calculateDefencePower(unitType, unitRole, tile)
        }
        return power
    }
}