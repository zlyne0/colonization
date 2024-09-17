package net.sf.freecol.common.model.ai.missions

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission
import promitech.colonization.ai.CommonMissionHandler
import promitech.colonization.ai.MissionExecutor
import promitech.colonization.ai.MissionHandler
import promitech.colonization.ai.MissionHandlerLogger

class ReplaceColonyWorkerMissionHandler(
    private val game: Game,
    private val missionExecutor: MissionExecutor
) : MissionHandler<ReplaceColonyWorkerMission> {

    override fun handle(playerMissionsContainer: PlayerMissionsContainer, mission: ReplaceColonyWorkerMission) {
        val player = playerMissionsContainer.player

        val unit: Unit? = player.units.getByIdOrNull(mission.replaceByUnitId)
        if (unit == null || !CommonMissionHandler.isUnitExists(player, unit)) {
            MissionHandlerLogger.logger.debug("player[%s].ReplaceColonyWorkerMissionHandler unit does not exists", player.getId())
            mission.setDone()
            return
        }
        if (!mission.isWorkerExistsInColony(player)) {
            MissionHandlerLogger.logger.debug("player[%s].ReplaceColonyWorkerMissionHandler worker doas not exist in colony", player.getId())
            mission.setDone()
            return
        }
        val colony: Colony = player.settlements.getById(mission.colonyId).asColony()

        if (unit.isAtTileLocation) {
            if (unit.isAtLocation(colony.tile)) {
                replaceUnits(playerMissionsContainer, mission, unit, colony)
            } else {
                createTransportRequest(playerMissionsContainer, mission, unit, colony)
            }
        } else if (unit.isAtUnitLocation) {
            // do nothing, wait for transport
            return
        } else if (unit.isAtEuropeLocation) {
            createTransportRequest(playerMissionsContainer, mission, unit, colony)
        }
    }

    private fun replaceUnits(
        playerMissionsContainer: PlayerMissionsContainer,
        mission: ReplaceColonyWorkerMission,
        unit: Unit,
        colony: Colony
    ) {
        colony.replaceWorker(unit, mission.colonyWorkerUnitId)
        mission.setDone()

        notifyParentMissionAboutReplace(playerMissionsContainer, mission, unit)
    }

    private fun notifyParentMissionAboutReplace(
        playerMissionsContainer: PlayerMissionsContainer,
        mission: ReplaceColonyWorkerMission,
        unit: Unit
    ) {
        val colonyWorkerUnit = playerMissionsContainer.player.units.getById(mission.colonyWorkerUnitId)

        var parentMission = playerMissionsContainer.findParentMission(mission)
        while (parentMission != null) {
            val parentMissionHandler = missionExecutor.findMissionHandler(parentMission)
            if (parentMissionHandler is ReplaceUnitInMissionHandler) {
                parentMissionHandler.replaceUnitInMission(parentMission, unit, colonyWorkerUnit)
            }
            parentMission = playerMissionsContainer.findParentMission(parentMission)
        }
    }

    private fun createTransportRequest(
        playerMissionsContainer: PlayerMissionsContainer,
        mission: ReplaceColonyWorkerMission,
        unit: Unit,
        colony: Colony
    ) {
        val requestMissionExists = playerMissionsContainer.hasMission(TransportUnitRequestMission::class.java) { requestMission ->
            requestMission.unit.equalsId(mission.replaceByUnitId)
        }
        if (!requestMissionExists) {
            playerMissionsContainer.addMission(
                mission,
                TransportUnitRequestMission(game.turn, unit, colony.tile)
                    .withAllowMoveToDestination()
            )
        }
    }
}