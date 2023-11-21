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
import net.sf.freecol.common.model.ai.missions.transportunit.NextDestinationWhenNoMoveAccessMissionHandler
import net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission
import net.sf.freecol.common.model.map.path.PathFinder
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
    NextDestinationWhenNoMoveAccessMissionHandler
{

    override fun handle(playerMissionsContainer: PlayerMissionsContainer, mission: DefenceMission) {
        val player = playerMissionsContainer.player

        if (!mission.isDefenceUnitExists()) {
            MissionHandlerLogger.logger.debug("player[%s].DefenceMissionHandler unit does not exists", player.getId())
            mission.setDone()
            return
        }

        if (!mission.isColonyOwner()) {
            MissionHandlerLogger.logger.debug("player[%s].DefenceMissionHandler no colony to protect", player.getId())

            val colony = defencePlaner.findColonyToProtect(mission.unit)
            if (colony == null) {
                mission.setDone()
                // global unit planer should take case of mission unit
                return
            } else {
                MissionHandlerLogger.logger.debug("player[%s].DefenceMissionHandler change colony: %s", player.getId(), colony.toString())
                mission.changeColony(colony)
            }
        }

        if (mission.unit.isAtTileLocation) {
            val colony = mission.colony()
            if (mission.unit.isAtLocation(colony.tile)) {
                mission.ifNotThenFortified()
                return
            }
            relocateUnit(playerMissionsContainer, mission)
        } else if (mission.unit.isAtUnitLocation) {
            if (!playerMissionsContainer.hasMission(TransportUnitRequestMission::class.java, mission.unit)) {
                val transportRequest = TransportUnitRequestMission(game.turn, mission.unit, mission.colony().tile)
                playerMissionsContainer.addMission(mission, transportRequest)
            }
            // else do nothing, wait for transport
            return
        } else if (mission.unit.isAtEuropeLocation) {
            createDefenceTransportRequest(playerMissionsContainer, mission)
        }
    }

    private fun relocateUnit(playerMissionsContainer: PlayerMissionsContainer, mission: DefenceMission) {
        val colony = mission.colony()
        if (mission.isDestinationOnTheSameIsland(game.map)) {
            moveToDestination(game, moveService, pathFinder, mission.unit, colony.tile) {
                mission.ifNotThenFortified()
            }
        } else {
            createDefenceTransportRequest(playerMissionsContainer, mission)
        }
    }

    private fun createDefenceTransportRequest(playerMissionsContainer: PlayerMissionsContainer, mission: DefenceMission) {
        val transportRequest = createTransportRequest(
            game,
            playerMissionsContainer,
            mission,
            mission.unit,
            mission.colony().tile
        )
        if (mission.unit.unitRole.equalsId(UnitRole.DRAGOON)) {
            transportRequest.withAllowMoveToDestination()
        }
    }

    override fun noDisembarkAccessNotification(
        playerMissionsContainer: PlayerMissionsContainer,
        transportUnitMission: TransportUnitMission,
        unitDestination: Tile,
        unit: Unit
    ) {
        val defenceMission = playerMissionsContainer.findFirstMissionKt(DefenceMission::class.java, unit)
        if (defenceMission != null) {
            val colony = defencePlaner.findColonyToProtect(unit)
            if (colony != null) {
                defenceMission.changeColony(colony)
                createDefenceTransportRequest(playerMissionsContainer, defenceMission)
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
            val colony = defencePlaner.findColonyToProtect(mission.unit)
            if (colony != null) {
                MissionHandlerLogger.logger.debug("player[%s].DefenceMissionHandler change colony: %s",
                    mission.unit.owner.id,
                    colony.toString()
                )
                mission.changeColony(colony)
                return colony.tile
            }
        }
        return null
    }
}