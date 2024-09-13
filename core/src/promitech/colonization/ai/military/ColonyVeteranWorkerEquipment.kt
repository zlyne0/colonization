package promitech.colonization.ai.military

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.UnitRole
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.ai.missions.ReplaceColonyWorkerMission
import net.sf.freecol.common.model.ai.missions.eachAsSequence
import net.sf.freecol.common.model.ai.missions.findFirstMissionKt
import net.sf.freecol.common.model.ai.missions.military.DefenceMission
import net.sf.freecol.common.model.map.path.PathFinder
import net.sf.freecol.common.model.player.Player
import net.sf.freecol.common.util.whenNotNull

class ColonyVeteranWorkerEquipment(
    private val game: Game,
    private val player: Player,
    private val playerMissionContainer: PlayerMissionsContainer,
    private val pathFinder: PathFinder,
    private val threatModel: ThreatModel
) {

    fun equip() {
        onEachColonyVeteranWorker { colony, colonyWorker ->
            val pathUnit = pathFinder.createPathUnit(
                player,
                Specification.instance.freeColonistUnitType,
                Specification.instance.unitRoles.getById(UnitRole.DRAGOON)
            )
            pathFinder.generateRangeMap(game.map, colony.tile, pathUnit, PathFinder.includeUnexploredTiles)

            val theBestMissionToReplaceUnit = findDefenceMission(colony.tile)
            if (theBestMissionToReplaceUnit != null) {
                player.units.getByIdOrNull(theBestMissionToReplaceUnit.unitId).whenNotNull { defenceMissionUnit ->
                    val replaceColonyWorkerMission = ReplaceColonyWorkerMission(
                        colony,
                        colonyWorker,
                        defenceMissionUnit
                    )
                    playerMissionContainer.addMission(theBestMissionToReplaceUnit, replaceColonyWorkerMission)
                }
            }
        }
    }

    private inline fun onEachColonyVeteranWorker(
        action: (colony: Colony, colonyWorker: Unit) -> kotlin.Unit
    ) {
        for (settlement in player.settlements) {
            val colony = settlement.asColony()
            for (colonyWorker in colony.settlementWorkers()) {
                if (colonyWorker.unitType.isType(UnitType.VETERAN_SOLDIER)) {
                    if (playerMissionContainer.isUnitBlockedForMission(colonyWorker)) {
                        continue
                    }
                    action(colony, colonyWorker)
                }
            }
        }
    }

    private fun findDefenceMission(veteranWorkerTile: Tile): DefenceMission? {
        val maxTurnDistance = 1
        return playerMissionContainer.eachAsSequence(DefenceMission::class.java)
            .filter { mission ->
                val defenceMissionUnit: Unit? = playerMissionContainer.player.units.getByIdOrNull(mission.unitId)
                defenceMissionUnit != null && defenceMissionUnit.unitType.isType(UnitType.FREE_COLONIST)
                        && playerMissionContainer.isAllDependMissionDone(mission)
                        && pathFinder.turnsCost(mission.tile) <= maxTurnDistance
                        && (mission.tile.equalsCoordinates(veteranWorkerTile) || !isDirectThreat(mission.tile))
            }
            .minByOrNull { mission -> pathFinder.totalCost(mission.tile) }
    }

    private fun isDirectThreat(tile: Tile): Boolean {
        return threatModel.isInWarRange(tile) || threatModel.isDefencePowerBelowThreatPower(tile) && countDefenceMissionAtTile(tile) <= 2
    }

    private fun countDefenceMissionAtTile(tile: Tile): Int {
        var count = 0
        for (unit in tile.units.entities()) {
            val defMission = playerMissionContainer.findFirstMissionKt(unit, DefenceMission::class.java)
            if (defMission != null && defMission.isAtDefenceLocation(unit)) {
                count++
            }
        }
        return count
    }

}