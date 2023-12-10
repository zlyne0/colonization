package promitech.colonization.ai.military

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.ai.missions.AbstractMission
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.ai.missions.military.DefenceMission
import net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission
import net.sf.freecol.common.model.forEachTile
import net.sf.freecol.common.model.map.path.PathFinder
import net.sf.freecol.common.model.player.Player
import net.sf.freecol.common.util.whenNotNull

class DefencePlaner(
    val game: Game,
    val pathFinder: PathFinder
) {

    private var localThreatModel: ThreatModel? = null
    private var recalculateThreatRequest: Player? = null

    fun generateThreatModel(player: Player): ThreatModel {
        if (localThreatModel == null) {
            return ThreatModel(game, player)
        }
        localThreatModel.whenNotNull { lth ->
            lth.recalculate(player)
            if (recalculateThreatRequest != null || !lth.player.equalsId(player) || !lth.createTurn.equals(game.turn)) {
                lth.recalculate(player)
            }
        }
        recalculateThreatRequest = null
        return localThreatModel!!
    }

    fun recalculateThreat(player: Player) {
        recalculateThreatRequest = player
    }

    fun createMissionFromUnusedUnit(unit: Unit, playerMissionContainer: PlayerMissionsContainer): AbstractMission? {
        if (!isMilitaryUnit(unit)) {
            return null
        }
        val colony = findColonyToProtect(unit)
        if (colony != null) {
            val defenceMission = DefenceMission(colony.tile, unit)
            playerMissionContainer.addMission(defenceMission)
            playerMissionContainer.addMission(defenceMission, TransportUnitRequestMission(game.turn, unit, colony.tile))
            return defenceMission
        }
        if (unit.unitType.equalsId(UnitType.ARTILLERY)) {
            //if (unit.isAtTileLocation) {
            // do nothing wait to create colony
            if (unit.isAtEuropeLocation) {
                // do nothing, just wait for colony
                return null
            }
            if (unit.isAtUnitLocation) {
                val carrier: Unit = unit.getLocationOrNull(Unit::class.java)
                val carrierMapPosition = carrier.positionRelativeToMap(game.map)

                val disembarkLocation = findTheClosestDisembarkLocation(carrierMapPosition, carrier)
                if (disembarkLocation != null) {
                    return TransportUnitRequestMission(game.turn, unit, disembarkLocation)
                }
            }
        }
        return null
    }

    private fun isMilitaryUnit(unit: Unit): Boolean {
        return unit.unitType.equalsId(UnitType.ARTILLERY) || unit.isArmed
    }

    fun findColonyToProtect(unit: Unit): Colony? {
        val threatModel = generateThreatModel(unit.owner)
        val colonyDefencePriority = threatModel.calculateColonyDefencePriority()
        if (colonyDefencePriority.isEmpty()) {
            return null
        }
        for (colonyThreat in colonyDefencePriority) {
            if (colonyThreat.war || colonyThreat.colonyDefencePower < 0) {
                return colonyThreat.colony
            }
        }
        return null
    }

    private fun findTheClosestDisembarkLocation(sourceTile: Tile, unit: Unit): Tile? {
        pathFinder.generateRangeMap(game.map, sourceTile, unit, PathFinder.includeUnexploredTiles)

        var theClosesLandTile: Tile? = null
        var theClosesLandTileTurnCost = 0
        var cost = 0

        game.map.forEachTile { tile ->
            if (tile.type.isLand) {
                cost = pathFinder.turnsCost(tile)
                if (theClosesLandTile == null || cost < theClosesLandTileTurnCost) {
                    theClosesLandTile = tile
                    theClosesLandTileTurnCost = cost
                }
            }
        }
        return theClosesLandTile
    }
}