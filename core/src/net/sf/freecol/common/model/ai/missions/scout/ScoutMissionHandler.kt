package net.sf.freecol.common.model.ai.missions.scout

import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.ai.missions.TransportUnitMission
import net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission
import net.sf.freecol.common.model.map.path.Path
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
        if (!mission.isScoutExists()) {
            MissionHandlerLogger.logger.debug("player[%s].ScoutMissionHandler scout does not exists", player.getId())
            mission.setDone()
            return
        }

        if (mission.phase == ScoutMission.Phase.NOTHING) {
            return
        }

        if (mission.isWaitingForTransport()) {
            if (TransportUnitMission.isUnitExistsOnTransportMission(playerMissionsContainer, mission.scout)) {
                return
            }
            if (mission.scout.isAtUnitLocation) {
                findAndHandleDestination(playerMissionsContainer, mission)
            }
            if (mission.scout.isAtTileLocation) {
                // when on the same island, start scout, else do nothing and wait for transport mission
                mission.startScoutAfterTransport(game)
            }
        }
        if (mission.scout.isAtEuropeLocation && !mission.isWaitingForTransport()) {
            findAndHandleDestination(playerMissionsContainer, mission)
        }

        if (mission.phase == ScoutMission.Phase.SCOUT && mission.scout.isAtTileLocation) {
            findAndHandleDestination(playerMissionsContainer, mission)
        }

        if (!mission.isScoutExists()) {
            // scout could die, remove mission
            MissionHandlerLogger.logger.debug("player[%s].ScoutMissionHandler scout does not exists", player.getId())
            mission.setDone()
        }
    }

    private fun findAndHandleDestination(playerMissionsContainer: PlayerMissionsContainer, mission: ScoutMission) {
        val scoutDestination = scoutMissionPlaner.findScoutDestination(mission.scout)
        when (scoutDestination) {
            is ScoutDestination.TheSameIsland -> moveToDestination(mission, scoutDestination.path)
            is ScoutDestination.OtherIsland -> moveToOtherIsland(playerMissionsContainer, scoutDestination, mission)
            is ScoutDestination.OtherIslandFromCarrier -> moveToOtherIslandFromCarrier(playerMissionsContainer, mission, scoutDestination)
            is ScoutDestination.Lack -> doNothing(mission)
        }
    }

    private fun moveToDestination(mission: ScoutMission, path: Path) {
        if (path.isReachedDestination()) {
            val moveContext = MoveContext(mission.scout, path)
            moveService.aiConfirmedMovePath(moveContext)
        }
    }

    private fun moveToOtherIsland(
        playerMissionsContainer: PlayerMissionsContainer,
        scoutDestination: ScoutDestination.OtherIsland,
        mission: ScoutMission
    ) {
        if (scoutDestination.transferLocationPath.isReachedDestination()) {
            val moveContext = MoveContext(mission.scout, scoutDestination.transferLocationPath)
            moveService.aiConfirmedMovePath(moveContext)

            if (mission.isScoutReadyToEmbark(scoutDestination.transferLocationPath.endTile)) {
                mission.waitForTransport(scoutDestination.tile)
                playerMissionsContainer.addMission(mission, TransportUnitRequestMission(mission.scout, scoutDestination.tile))
            }
        }
    }

    private fun moveToOtherIslandFromCarrier(
        playerMissionsContainer: PlayerMissionsContainer,
        mission: ScoutMission,
        scoutDestination: ScoutDestination.OtherIslandFromCarrier
    ) {
        mission.waitForTransport(scoutDestination.tile)
        playerMissionsContainer.addMission(mission, TransportUnitRequestMission(mission.scout, scoutDestination.tile))
    }

    override fun noDisembarkAccessNotification(
        playerMissionsContainer: PlayerMissionsContainer,
        transportUnitMission: TransportUnitMission,
        unitDestination: Tile,
        unit: Unit
    ) {
        val scoutMission = playerMissionsContainer.findFirstMission(ScoutMission::class.java, unit)
        if (scoutMission != null) {
            val playerAiContainer = game.aiContainer.playerAiContainer(scoutMission.scout.owner)
            playerAiContainer.addScoutBlockTile(unitDestination)
            findAndHandleDestination(playerMissionsContainer, scoutMission)
        }
    }

    private fun doNothing(mission: ScoutMission) {
        mission.setDoNothing()
        mission.setDone()
        // ColonyWorkerRequestPlaner should gather unit and find work
    }
}