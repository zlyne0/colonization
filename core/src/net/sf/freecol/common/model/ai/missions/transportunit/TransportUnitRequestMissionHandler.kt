package net.sf.freecol.common.model.ai.missions.transportunit

import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.MoveType
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.ai.missions.findParentMission
import net.sf.freecol.common.model.map.path.Path
import net.sf.freecol.common.model.map.path.PathFinder
import net.sf.freecol.common.util.CollectionUtils
import promitech.colonization.ai.MissionExecutor
import promitech.colonization.ai.MissionHandler
import promitech.colonization.ai.MissionHandlerLogger.logger
import promitech.colonization.ai.findShipsTileLocations
import promitech.colonization.orders.move.MoveContext
import promitech.colonization.orders.move.MoveService

class TransportUnitRequestMissionHandler(
    private val game: Game,
    private val moveService: MoveService,
    private val pathFinder: PathFinder,
    private val pathFinder2: PathFinder,
    private val missionExecutor: MissionExecutor
) : MissionHandler<TransportUnitRequestMission> {

    override fun handle(
        playerMissionsContainer: PlayerMissionsContainer,
        mission: TransportUnitRequestMission
    ) {
        val player = playerMissionsContainer.player

        if (!player.units.containsId(mission.unit)) {
            logger.debug(
                "player[%s].TransportUnitRequestMission no unit %s",
                player.getId(),
                mission.unit.id
            )
            mission.setDone()
            return
        }

        // Do nothing. Transport request handled by transport unit handler and planer.

        if (mission.allowMoveToDestination && !mission.hasTransportUnitMission() && mission.isUnitAtTileLocation()) {
            if (mission.isDestinationOnTheSameIsland(game.map)) {
                moveToDestination(playerMissionsContainer, mission, {
                    mission.setDone()
                })
            } else {
                // move to sea side and wait for ship
                moveToOtherIsland(mission)
            }
        }
    }

    private fun moveToOtherIsland(mission: TransportUnitRequestMission) {
        val recommendEmbarkLocationPath = recommendEmbarkLocationPath(mission)
        if (recommendEmbarkLocationPath != null) {
            val moveContext = MoveContext(mission.unit, recommendEmbarkLocationPath)
            moveService.aiConfirmedMovePath(moveContext)
        }
    }

    private fun recommendEmbarkLocationPath(mission: TransportUnitRequestMission): Path? {
        val unitEmbarkGenerateRangeFlags = CollectionUtils.enumSum(
            PathFinder.includeUnexploredTiles,
            PathFinder.FlagTypes.AllowEmbark
        )
        pathFinder.generateRangeMap(game.map, mission.unit, unitEmbarkGenerateRangeFlags)

        val shipsTileLocations = mission.unit.owner.findShipsTileLocations(game.map)
        if (shipsTileLocations.isEmpty()) {
            return null
        }
        pathFinder2.generateRangeMap(
            game.map,
            shipsTileLocations,
            pathFinder2.createPathUnit(
                mission.unit.owner,
                Specification.instance.unitTypes.getById(UnitType.CARAVEL)
            ),
            CollectionUtils.enumSum(
                PathFinder.includeUnexploredTiles,
                PathFinder.FlagTypes.AvoidDisembark
            )
        )
        val embarkLocation =
            pathFinder2.findFirstTheBestSumTurnCost(pathFinder, PathFinder.SumPolicy.PRIORITY_SUM)
        if (embarkLocation == null || mission.isUnitAt(embarkLocation)) {
            return null
        }
        return pathFinder.createPath(embarkLocation)
    }

    private inline fun moveToDestination(
        playerMissionsContainer: PlayerMissionsContainer,
        mission: TransportUnitRequestMission,
        actionOnDestination: () -> kotlin.Unit = {}
    ) {
        if (mission.isUnitAtDestination()) {
            actionOnDestination()
            return
        }
        if (!mission.unit.hasMovesPoints()) {
            return
        }
        val path: Path = pathFinder.findToTile(
            game.map,
            mission.unit,
            mission.destination,
            PathFinder.includeUnexploredTiles
        )
        if (path.reachTile(mission.destination)) {
            mission.recalculateWorthEmbark(pathFinder.turnsCost(mission.destination))

            val moveContext = MoveContext(mission.unit, path)
            val lastMoveType = moveService.aiConfirmedMovePath(moveContext)

            if (lastMoveType.isProgress || MoveType.MOVE_NO_MOVES.equals(lastMoveType)) {
                if (mission.isUnitAtDestination()) {
                    actionOnDestination()
                }
            } else {
                notifyParentMissionMoveNoAccess(playerMissionsContainer, mission, lastMoveType)
            }
        } else {
            notifyParentMissionMoveNoAccess(playerMissionsContainer, mission, MoveType.MOVE_ILLEGAL)
        }
    }

    private fun notifyParentMissionMoveNoAccess(
        playerMissionsContainer: PlayerMissionsContainer,
        mission: TransportUnitRequestMission,
        moveType: MoveType
    ) {
        val parentMission = playerMissionsContainer.findParentMission(mission)
        if (parentMission == null) {
            logger.debug(
                "player[%s].TransportUnitRequestMission.unit[%s].destination[%s] no access moveType[%s] no parent mission",
                playerMissionsContainer.player.getId(),
                mission.unit.id,
                mission.destination.toPrettyString(),
                moveType
            )
            mission.setDone()
            return
        }
        val parentMissionHandler = missionExecutor.findMissionHandler(parentMission)
        if (parentMissionHandler is MoveNoAccessNotificationMissionHandler) {
            parentMissionHandler.moveNoAccessNotification(
                playerMissionsContainer,
                parentMission,
                mission,
                moveType
            )
        }
    }
}