package net.sf.freecol.common.model.ai.missions.military

import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.UnitRole
import net.sf.freecol.common.model.ai.missions.AbstractMission
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.ai.missions.TransportUnitMission
import net.sf.freecol.common.model.ai.missions.findFirstMissionKt
import net.sf.freecol.common.model.ai.missions.hasMission
import net.sf.freecol.common.model.ai.missions.ReplaceUnitInMissionHandler
import net.sf.freecol.common.model.ai.missions.transportunit.NextDestinationWhenNoMoveAccessMissionHandler
import net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission
import net.sf.freecol.common.model.map.path.PathFinder
import promitech.colonization.ai.CommonMissionHandler
import promitech.colonization.ai.MissionHandler
import promitech.colonization.ai.MissionHandlerLogger
import promitech.colonization.ai.TransportUnitNoDisembarkAccessNotification
import promitech.colonization.ai.createTransportRequest
import promitech.colonization.ai.military.DefencePlaner
import promitech.colonization.ai.moveToDestination
import promitech.colonization.orders.move.MoveService

class DefenceMissionHandler(
    private val game: Game,
    private val defencePlaner: DefencePlaner,
    private val moveService: MoveService,
    private val pathFinder: PathFinder
):
    MissionHandler<DefenceMission>,
    TransportUnitNoDisembarkAccessNotification,
    NextDestinationWhenNoMoveAccessMissionHandler,
    ReplaceUnitInMissionHandler
{

    override fun handle(playerMissionsContainer: PlayerMissionsContainer, mission: DefenceMission) {
        val player = playerMissionsContainer.player
        val unit: Unit? = player.units.getByIdOrNull(mission.unitId)

        if (unit == null || !CommonMissionHandler.isUnitExists(player, unit)) {
            MissionHandlerLogger.logger.debug("player[%s].DefenceMissionHandler unit does not exists", player.getId())
            mission.setDone()
            return
        }

        if (!mission.isTileAccessible(player)) {
            MissionHandlerLogger.logger.debug("player[%s].DefenceMissionHandler no colony to protect", player.getId())

            val colony = defencePlaner.findColonyToProtect(player)
            if (colony == null) {
                mission.setDone()
                // global unit planer should take case of mission unit
                return
            } else {
                MissionHandlerLogger.logger.debug("player[%s].DefenceMissionHandler change colony: %s", player.getId(), colony.toString())
                mission.changeDefenceDestination(colony)
            }
        }

        if (unit.isAtTileLocation) {
            if (mission.isAtDefenceLocation(unit)) {
                mission.ifNotThenFortified(unit)
                return
            }
            relocateUnit(playerMissionsContainer, mission, unit)
        } else if (unit.isAtUnitLocation) {
            if (!playerMissionsContainer.hasMission(TransportUnitRequestMission::class.java, unit)) {
                val transportRequest = TransportUnitRequestMission(game.turn, unit, mission.tile)
                playerMissionsContainer.addMission(mission, transportRequest)
            }
            // else do nothing, wait for transport
            return
        } else if (unit.isAtEuropeLocation) {
            createDefenceTransportRequest(playerMissionsContainer, mission, unit)
        }
    }

    private fun relocateUnit(playerMissionsContainer: PlayerMissionsContainer, mission: DefenceMission, unit: Unit) {
        if (game.map.isTheSameArea(mission.tile, unit.tile)) {
            moveToDestination(game, moveService, pathFinder, unit, mission.tile) {
                mission.ifNotThenFortified(unit)
            }
        } else {
            createDefenceTransportRequest(playerMissionsContainer, mission, unit)
        }
    }

    private fun createDefenceTransportRequest(playerMissionsContainer: PlayerMissionsContainer, mission: DefenceMission, unit: Unit) {
        val transportRequest = createTransportRequest(
            game,
            playerMissionsContainer,
            mission,
            unit,
            mission.tile
        )
        if (unit.unitRole.equalsId(UnitRole.DRAGOON)) {
            transportRequest.withAllowMoveToDestination()
        }
    }

    override fun noDisembarkAccessNotification(
        playerMissionsContainer: PlayerMissionsContainer,
        transportUnitMission: TransportUnitMission,
        unitDestination: Tile,
        unit: Unit
    ) {
        val defenceMission = playerMissionsContainer.findFirstMissionKt(unit, DefenceMission::class.java)
        if (defenceMission != null) {
            val colony = defencePlaner.findColonyToProtect(unit.owner)
            if (colony != null) {
                defenceMission.changeDefenceDestination(colony)
                createDefenceTransportRequest(playerMissionsContainer, defenceMission, unit)
            } else {
                MissionHandlerLogger.logger.debug("player[%s].DefenceMissionHandler no colony to defence", unit.owner.id)
                defenceMission.setDone()
                // global mission planer should take care of unit
            }
        }
    }

    override fun nextDestination(
        playerMissionsContainer: PlayerMissionsContainer,
        mission: AbstractMission,
        transportRequestMission: TransportUnitRequestMission
    ): Tile? {
        if (mission is DefenceMission) {
            val colony = defencePlaner.findColonyToProtect(playerMissionsContainer.player)
            if (colony != null) {
                MissionHandlerLogger.logger.debug("player[%s].DefenceMissionHandler change colony: %s",
                    playerMissionsContainer.player.id,
                    colony.toString()
                )
                mission.changeDefenceDestination(colony)
                return colony.tile
            }
        }
        return null
    }

    override fun replaceUnitInMission(mission: AbstractMission, unitToReplace: Unit, replaceBy: Unit) {
        if (mission is DefenceMission) {
            val playerMissionsContainer = game.aiContainer.missionContainer(replaceBy.owner)
            mission.changeUnit(unitToReplace, replaceBy, playerMissionsContainer)
        }
    }
}