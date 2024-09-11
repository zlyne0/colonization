package net.sf.freecol.common.model.ai.missions.scout

import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.ai.missions.TransportUnitMission
import net.sf.freecol.common.model.ai.missions.findFirstMissionKt
import net.sf.freecol.common.model.ai.missions.hasMission
import net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission
import net.sf.freecol.common.model.map.path.Path
import promitech.colonization.ai.CommonMissionHandler.isUnitExists
import promitech.colonization.ai.MissionHandler
import promitech.colonization.ai.MissionHandlerLogger
import promitech.colonization.ai.TransportUnitNoDisembarkAccessNotification
import promitech.colonization.orders.move.MoveContext
import promitech.colonization.orders.move.MoveService

class ScoutMissionHandler(
    private val game: Game,
    private val scoutMissionPlaner: ScoutMissionPlaner,
    private val moveService: MoveService
): MissionHandler<ScoutMission>, TransportUnitNoDisembarkAccessNotification {

    override fun handle(playerMissionsContainer: PlayerMissionsContainer, mission: ScoutMission) {
        val player = playerMissionsContainer.player
        val scout: Unit? = player.units.getByIdOrNull(mission.scoutId)

        if (scout == null || !isUnitExists(player, scout)) {
            MissionHandlerLogger.logger.debug("player[%s].ScoutMissionHandler scout does not exists", player.getId())
            mission.setDone()
            return
        }

        if (mission.phase == ScoutMission.Phase.NOTHING) {
            return
        }

        if (mission.isWaitingForTransport()) {
            if (isUnitExistsOnTransportMission(playerMissionsContainer, scout)) {
                return
            }
            if (scout.isAtUnitLocation) {
                findAndHandleDestination(playerMissionsContainer, mission, scout)
            }
            if (scout.isAtTileLocation) {
                // when on the same island, start scout, else do nothing and wait for transport mission
                mission.startScoutAfterTransport(game, scout)
            }
        }
        if (scout.isAtEuropeLocation && !mission.isWaitingForTransport()) {
            findAndHandleDestination(playerMissionsContainer, mission, scout)
        }

        if (mission.phase == ScoutMission.Phase.SCOUT && scout.isAtTileLocation) {
            findAndHandleDestination(playerMissionsContainer, mission, scout)
        }

        if (!isUnitExists(player, scout)) {
            // scout could die, remove mission
            MissionHandlerLogger.logger.debug("player[%s].ScoutMissionHandler scout does not exists", player.getId())
            mission.setDone()
        }
    }

    private fun isUnitExistsOnTransportMission(playerMissionsContainer: PlayerMissionsContainer, scout: Unit): Boolean {
        return playerMissionsContainer.hasMission(TransportUnitMission::class.java) { transportMission ->
            transportMission.isCarriedUnitTransportDestinationSet(scout)
        }
    }

    private fun findAndHandleDestination(playerMissionsContainer: PlayerMissionsContainer, mission: ScoutMission, scout: Unit) {
        val scoutDestination = scoutMissionPlaner.findScoutDestination(scout)
        when (scoutDestination) {
            is ScoutDestination.TheSameIsland -> moveToDestination(scoutDestination.path, scout)
            is ScoutDestination.OtherIsland -> moveToOtherIsland(playerMissionsContainer, scoutDestination, mission, scout)
            is ScoutDestination.OtherIslandFromCarrier -> moveToOtherIslandFromCarrier(playerMissionsContainer, mission, scout, scoutDestination)
            is ScoutDestination.Lack -> doNothing(mission)
        }
    }

    private fun moveToDestination(path: Path, scout: Unit) {
        if (path.isReachedDestination()) {
            val moveContext = MoveContext(scout, path)
            moveService.aiConfirmedMovePath(moveContext)
        }
    }

    private fun moveToOtherIsland(
        playerMissionsContainer: PlayerMissionsContainer,
        scoutDestination: ScoutDestination.OtherIsland,
        mission: ScoutMission,
        scout: Unit
    ) {
        if (scoutDestination.transferLocationPath.isReachedDestination()) {
            val moveContext = MoveContext(scout, scoutDestination.transferLocationPath)
            moveService.aiConfirmedMovePath(moveContext)

            if (isScoutReadyToEmbark(scout, scoutDestination.transferLocationPath.endTile)) {
                mission.waitForTransport(scoutDestination.tile)
                playerMissionsContainer.addMission(mission, TransportUnitRequestMission(game.turn, scout, scoutDestination.tile))
            }
        }
    }

    private fun isScoutReadyToEmbark(scout: Unit, embarkLocation: Tile): Boolean {
        val scoutLocation = scout.tile
        return scoutLocation.equalsCoordinates(embarkLocation) || scoutLocation.isStepNextTo(embarkLocation)
    }

    private fun moveToOtherIslandFromCarrier(
        playerMissionsContainer: PlayerMissionsContainer,
        mission: ScoutMission,
        scout: Unit,
        scoutDestination: ScoutDestination.OtherIslandFromCarrier
    ) {
        mission.waitForTransport(scoutDestination.tile)
        playerMissionsContainer.addMission(mission, TransportUnitRequestMission(game.turn, scout, scoutDestination.tile))
    }

    override fun noDisembarkAccessNotification(
        playerMissionsContainer: PlayerMissionsContainer,
        transportUnitMission: TransportUnitMission,
        unitDestination: Tile,
        unit: Unit
    ) {
        val scoutMission = playerMissionsContainer.findFirstMissionKt(unit, ScoutMission::class.java)
        if (scoutMission != null) {
            val scout = playerMissionsContainer.player.units.getByIdOrNull(scoutMission.scoutId)
            if (scout != null) {
                val playerAiContainer = game.aiContainer.playerAiContainer(playerMissionsContainer.player)
                playerAiContainer.addScoutBlockTile(unitDestination)
                findAndHandleDestination(playerMissionsContainer, scoutMission, scout)
            }
        }
    }

    private fun doNothing(mission: ScoutMission) {
        mission.setDoNothing()
        mission.setDone()
        // ColonyWorkerRequestPlaner should gather unit and find work
    }
}