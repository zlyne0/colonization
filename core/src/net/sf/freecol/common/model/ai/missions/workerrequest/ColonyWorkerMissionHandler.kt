package net.sf.freecol.common.model.ai.missions.workerrequest

import net.sf.freecol.common.model.ColonyFactory
import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.MoveType
import net.sf.freecol.common.model.ai.missions.AbstractMission
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.ai.missions.transportunit.MoveNoAccessNotificationMissionHandler
import net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission
import net.sf.freecol.common.model.map.path.PathFinder
import promitech.colonization.ai.ColonyProductionPlaner
import promitech.colonization.ai.CommonMissionHandler
import promitech.colonization.ai.MissionHandler
import promitech.colonization.ai.MissionHandlerLogger
import promitech.colonization.orders.BuildColonyOrder
import promitech.colonization.orders.BuildColonyOrder.OrderStatus

class ColonyWorkerMissionHandler(
    private val game: Game,
    private val pathFinder: PathFinder
) : MissionHandler<ColonyWorkerMission>, MoveNoAccessNotificationMissionHandler {

    override fun handle(
        playerMissionsContainer: PlayerMissionsContainer,
        mission: ColonyWorkerMission
    ) {
        val player = playerMissionsContainer.player
        if (!CommonMissionHandler.isUnitExists(player, mission.unit)) {
            mission.setDone()
            return
        }
        if (mission.isUnitAtDestination) {
            if (mission.tile.hasSettlement()) {
                addWorkerToColony(mission)
            } else {
                buildColony(mission)
            }
        } else {
            val transportUnitRequestMission = TransportUnitRequestMission(
                game.turn, mission.unit, mission.tile, true, true, NOT_WORTH_EMBARK_RANGE
            )
            playerMissionsContainer.addMission(mission, transportUnitRequestMission)
        }
    }

    private fun addWorkerToColony(mission: ColonyWorkerMission) {
        val colony = mission.tile.settlement.asColony()
        ColonyProductionPlaner.initColonyBuilderUnit(colony, mission.unit)
        ColonyProductionPlaner.createPlanForNewColony(colony)
        mission.setDone()
    }

    private fun buildColony(mission: ColonyWorkerMission) {
        val buildColonyOrder = BuildColonyOrder(game.map)
        val check = buildColonyOrder.check(mission.unit, mission.tile)
        if (check == OrderStatus.OK) {
            val colonyFactory = ColonyFactory(game, pathFinder)
            colonyFactory.buildColonyByAI(mission.unit, mission.tile)
            mission.setDone()
        } else {
            if (check == OrderStatus.NO_MOVE_POINTS) {
                // do nothing wait on next turn for move points
            } else {
                // planer should create new mission for unit
                mission.setDone()
            }
        }
    }

    override fun moveNoAccessNotification(
        playerMissionsContainer: PlayerMissionsContainer,
        colonyWorkerMission: AbstractMission,
        transportRequestMission: TransportUnitRequestMission,
        moveType: MoveType
    ) {
        if (!(colonyWorkerMission is ColonyWorkerMission)) {
            return
        }
        if (moveType == MoveType.MOVE_NO_ACCESS_SETTLEMENT) {
            if (MissionHandlerLogger.logger.isDebug) {
                MissionHandlerLogger.logger.debug("player[%s].ColonyWorkerMission[%s].unit[%s].destination[%s] no move access moveType[%s]",
                    playerMissionsContainer.player.getId(),
                    colonyWorkerMission.id,
                    colonyWorkerMission.unit.id,
                    colonyWorkerMission.tile.toPrettyString(),
                    moveType
                )
            }

            endMissions(transportRequestMission, colonyWorkerMission, playerMissionsContainer)
        } else {
            colonyWorkerMission.increaseMoveNoAccessCounter()
            if (colonyWorkerMission.isMoveNoAccessCounterAboveRange(MOVE_NO_ACCESS_MAX_ATTEMPT)) {
                if (MissionHandlerLogger.logger.isDebug) {
                    MissionHandlerLogger.logger.debug("player[%s].ColonyWorkerMission[%s].unit[%s].destination[%s] no move access moveType[%s] max move attempt",
                        playerMissionsContainer.player.getId(),
                        colonyWorkerMission.id,
                        colonyWorkerMission.unit.id,
                        colonyWorkerMission.tile.toPrettyString(),
                        moveType
                    )
                }

                endMissions(transportRequestMission, colonyWorkerMission, playerMissionsContainer)
            }
        }
    }

    private fun endMissions(
        transportRequestMission: TransportUnitRequestMission,
        colonyWorkerMission: AbstractMission,
        playerMissionsContainer: PlayerMissionsContainer
    ) {
        // planer should take unit and create new destination
        transportRequestMission.setDone()
        colonyWorkerMission.setDone()
        // manual unblocking because MissionExecutor control only one handled mission
        playerMissionsContainer.unblockUnitsFromMission(transportRequestMission)
        playerMissionsContainer.unblockUnitsFromMission(colonyWorkerMission)
    }

    companion object {
        private const val NOT_WORTH_EMBARK_RANGE = 5
        private const val MOVE_NO_ACCESS_MAX_ATTEMPT = 3
    }
}
