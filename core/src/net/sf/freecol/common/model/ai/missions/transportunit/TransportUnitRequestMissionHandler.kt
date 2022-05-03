package net.sf.freecol.common.model.ai.missions.transportunit

import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import promitech.colonization.ai.MissionHandler
import promitech.colonization.ai.MissionHandlerLogger.logger

class TransportUnitRequestMissionHandler : MissionHandler<TransportUnitRequestMission> {
    override fun handle(playerMissionsContainer: PlayerMissionsContainer, mission: TransportUnitRequestMission) {
        val player = playerMissionsContainer.player

        if (!player.units.containsId(mission.unit)) {
            logger.debug("player[%s].TransportUnitRequestMission no unit %s", player.getId(), mission.unit.id)
            mission.setDone()
            return
        }
        // Do nothing. Transport request handled by transport unit handler and planer.
    }
}