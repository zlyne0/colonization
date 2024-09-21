package promitech.colonization.ai

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.Map
import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.ai.missions.TransportUnitMission
import net.sf.freecol.common.model.ai.missions.foreachMission
import net.sf.freecol.common.model.player.Player

typealias UnitTypeId = String
typealias UnitRoleId = String

fun Player.findShipsTileLocations(map: Map): List<Tile> {
    val sourceTiles = mutableListOf<Tile>()
    for (unit in units) {
        if (unit.isNaval && unit.isCarrier && !unit.isDamaged) {
            if (unit.isAtTileLocation) {
                sourceTiles.add(unit.tile)
            } else if (unit.isAtEuropeLocation || unit.isAtHighSeasLocation) {
                sourceTiles.add(map.getSafeTile(entryLocation))
            } else if (unit.isAtColonyLocation) {
                sourceTiles.add(unit.getLocationOrNull(Colony::class.java).tile)
            }
        }
    }
    return sourceTiles
}

fun Player.calculateNavyTotalCargo(shipUnitTypeIds: Set<UnitTypeId>): Int {
    var slots = 0
    for (unit in units) {
        if (shipUnitTypeIds.contains(unit.unitType.id) && !unit.isDamaged) {
            slots += unit.unitType.getSpace()
        }
    }
    return slots
}

fun Player.findCarrier(): Unit? {
    for (u in units) {
        if (u.isNaval && u.isCarrier && !u.isDamaged) {
            return u
        }
    }
    return null
}

class Units {
    companion object {
        @JvmField
        // more free cargo first
        val FREE_CARGO_SPACE_COMPARATOR = java.util.Comparator<Unit> { o1, o2 ->
            if (o2.hasMoreFreeCargoSpace(o1)) {
                1
            } else {
                -1
            }
        }

        fun transporterCapacity(transporter: Unit, playerMissionContainer: PlayerMissionsContainer): Int {
            var capacity: Int = transporter.freeUnitsSlots()
            playerMissionContainer.foreachMission(TransportUnitMission::class.java) { transportUnitMission ->
                if (transportUnitMission.isCarrier(transporter)) {
                    capacity -= transportUnitMission.spaceTakenByUnits(transporter)
                }
            }
            return capacity
        }
    }
}