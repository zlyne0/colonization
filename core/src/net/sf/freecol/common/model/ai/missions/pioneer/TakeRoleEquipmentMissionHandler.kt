package net.sf.freecol.common.model.ai.missions.pioneer

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.ai.missions.AbstractMission
import net.sf.freecol.common.model.ai.missions.MissionId
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.ai.missions.hasMission
import net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission
import promitech.colonization.ai.MissionHandler
import promitech.colonization.ai.MissionHandlerLogger

class TakeRoleEquipmentMissionHandler(val game: Game): MissionHandler<TakeRoleEquipmentMission>, ReplaceUnitInMissionHandler {

    override fun handle(playerMissionsContainer: PlayerMissionsContainer, mission: TakeRoleEquipmentMission) {
        val player = playerMissionsContainer.player

        if (!mission.isUnitExists(player)) {
            MissionHandlerLogger.logger.debug("player[%s].TakeRoleEquipmentMissionHandler unit does not exists", player.getId())
            mission.setDone()
            return
        }
        if (!mission.isColonyExist(player)) {
            MissionHandlerLogger.logger.debug("player[%s].TakeRoleEquipmentMissionHandler colony does not exist", player.getId())
            mission.setDone()
            return
        }

        if (mission.unit.isAtTileLocation) {
            if (mission.unit.isAtLocation(mission.colony().tile)) {
                equip(mission)
            } else {
                createTransportRequest(playerMissionsContainer, mission)
            }
        } else if (mission.unit.isAtUnitLocation) {
            // do nothing, wait for transport
            return
        } else if (mission.unit.isAtEuropeLocation) {
            createTransportRequest(playerMissionsContainer, mission)
        }
    }

    private fun equip(mission: TakeRoleEquipmentMission) {
        val colony = mission.colony()
        if (colony.hasGoodsToEquipRole(mission.role, mission.roleCount)) {
            colony.changeUnitRole(mission.unit, mission.role, mission.roleCount)
            removeSupplyGoodsReservation(colony, mission.id)
            mission.setDone()
        }
        // else wait for goods
    }

    private fun removeSupplyGoodsReservation(colony: Colony, missionId: MissionId) {
        val playerAiContainer = game.aiContainer.playerAiContainer(colony.owner)
        val colonySupplyGoods = playerAiContainer.findColonySupplyGoods(colony)
        if (colonySupplyGoods != null) {
            colonySupplyGoods.removeSupplyReservation(missionId)
        }
    }

    private fun createTransportRequest(
        playerMissionsContainer: PlayerMissionsContainer,
        mission: TakeRoleEquipmentMission
    ) {
        val requestMissionExists = playerMissionsContainer.hasMission(TransportUnitRequestMission::class.java) { requestMission ->
            requestMission.unit.equalsId(mission.unit)
        }
        if (!requestMissionExists) {
            playerMissionsContainer.addMission(
                mission,
                TransportUnitRequestMission(game.turn, mission.unit, mission.colony().tile)
                    .withAllowMoveToDestination()
            )
        }
    }

    override fun replaceUnitInMission(mission: AbstractMission, unitToReplace: Unit, replaceBy: Unit) {
        if (mission is TakeRoleEquipmentMission) {
            val playerMissionsContainer = game.aiContainer.missionContainer(replaceBy.owner)
            mission.changeUnit(unitToReplace, replaceBy, playerMissionsContainer)
        }
    }
}