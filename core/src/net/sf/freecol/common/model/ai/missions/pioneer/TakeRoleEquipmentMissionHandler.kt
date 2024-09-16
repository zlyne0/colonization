package net.sf.freecol.common.model.ai.missions.pioneer

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.ai.missions.AbstractMission
import net.sf.freecol.common.model.ai.missions.MissionId
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.ai.missions.ReplaceUnitInMissionHandler
import net.sf.freecol.common.model.ai.missions.hasMission
import net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission
import promitech.colonization.ai.CommonMissionHandler
import promitech.colonization.ai.MissionHandler
import promitech.colonization.ai.MissionHandlerLogger

class TakeRoleEquipmentMissionHandler(val game: Game): MissionHandler<TakeRoleEquipmentMission>, ReplaceUnitInMissionHandler {

    override fun handle(playerMissionsContainer: PlayerMissionsContainer, mission: TakeRoleEquipmentMission) {
        val player = playerMissionsContainer.player
        val unit: Unit? = player.units.getByIdOrNull(mission.unitId)

        if (unit == null || !CommonMissionHandler.isUnitExists(player, unit)) {
            MissionHandlerLogger.logger.debug("player[%s].TakeRoleEquipmentMissionHandler unit does not exists", player.getId())
            mission.setDone()
            return
        }
        if (!mission.isColonyExist(player)) {
            MissionHandlerLogger.logger.debug("player[%s].TakeRoleEquipmentMissionHandler colony does not exist", player.getId())
            mission.setDone()
            return
        }
        val colony = player.settlements.getById(mission.colonyId).asColony()

        if (unit.isAtTileLocation) {
            if (unit.isAtLocation(colony.tile)) {
                equip(mission, unit, colony)
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

    private fun equip(mission: TakeRoleEquipmentMission, unit: Unit, colony: Colony) {
        if (colony.hasGoodsToEquipRole(mission.role, mission.roleCount)) {
            colony.changeUnitRole(unit, mission.role, mission.roleCount)
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
        mission: TakeRoleEquipmentMission,
        missionUnit: Unit,
        colony: Colony
    ) {
        val requestMissionExists = playerMissionsContainer.hasMission(TransportUnitRequestMission::class.java) { requestMission ->
            requestMission.unit.equalsId(mission.unitId)
        }
        if (!requestMissionExists) {
            playerMissionsContainer.addMission(
                mission,
                TransportUnitRequestMission(game.turn, missionUnit, colony.tile)
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