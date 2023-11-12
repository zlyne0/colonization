package net.sf.freecol.common.model.ai.missions.pioneer

import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.ai.missions.findParentMission
import net.sf.freecol.common.model.ai.missions.hasMissionKt
import net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission
import promitech.colonization.ai.MissionExecutor
import promitech.colonization.ai.MissionHandler
import promitech.colonization.ai.MissionHandlerLogger

class ReplaceColonyWorkerMissionHandler(
    private val game: Game,
    private val missionExecutor: MissionExecutor
) : MissionHandler<ReplaceColonyWorkerMission> {

    override fun handle(playerMissionsContainer: PlayerMissionsContainer, mission: ReplaceColonyWorkerMission) {

        val player = playerMissionsContainer.player

        if (!mission.isUnitExists(player)) {
            MissionHandlerLogger.logger.debug("player[%s].ReplaceColonyWorkerMissionHandler unit does not exists", player.getId())
            mission.setDone()
            return
        }
        if (!mission.isWorkerExistsInColony(player)) {
            MissionHandlerLogger.logger.debug("player[%s].ReplaceColonyWorkerMissionHandler worker doas not exist in colony", player.getId())
            mission.setDone()
            return
        }

        if (mission.replaceByUnit.isAtTileLocation) {
            if (mission.replaceByUnit.isAtLocation(mission.colony().tile)) {
                replaceUnits(playerMissionsContainer, mission)
            } else {
                createTransportRequest(playerMissionsContainer, mission)
            }
        } else if (mission.replaceByUnit.isAtUnitLocation) {
            // do nothing, wait for transport
            return
        } else if (mission.replaceByUnit.isAtEuropeLocation) {
            createTransportRequest(playerMissionsContainer, mission)
        }
    }

    private fun replaceUnits(playerMissionsContainer: PlayerMissionsContainer, mission: ReplaceColonyWorkerMission) {
        val colony = mission.colony()
        colony.replaceWorker(mission.replaceByUnit, mission.colonyWorkerUnitId)
        mission.setDone()

        notifyParentMissionAboutReplace(playerMissionsContainer, mission)
    }

    private fun notifyParentMissionAboutReplace(
        playerMissionsContainer: PlayerMissionsContainer,
        mission: ReplaceColonyWorkerMission
    ) {
        val colonyWorkerUnit = playerMissionsContainer.player.units.getById(mission.colonyWorkerUnitId)

        var parentMission = playerMissionsContainer.findParentMission(mission)
        while (parentMission != null) {
            val parentMissionHandler = missionExecutor.findMissionHandler(parentMission)
            if (parentMissionHandler is ReplaceUnitInMissionHandler) {
                parentMissionHandler.replaceUnitInMission(parentMission, mission.replaceByUnit, colonyWorkerUnit)
            }
            parentMission = playerMissionsContainer.findParentMission(parentMission)
        }
    }

    private fun createTransportRequest(
        playerMissionsContainer: PlayerMissionsContainer,
        mission: ReplaceColonyWorkerMission
    ) {
        val requestMissionExists = playerMissionsContainer.hasMissionKt(TransportUnitRequestMission::class.java, { requestMission ->
            requestMission.unit.equalsId(mission.replaceByUnit)
        })
        if (!requestMissionExists) {
            playerMissionsContainer.addMission(
                mission,
                TransportUnitRequestMission(game.turn, mission.replaceByUnit, mission.colony().tile)
                    .withAllowMoveToDestination()
            )
        }
    }
}