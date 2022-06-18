package net.sf.freecol.common.model.ai.missions.pioneer

import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import promitech.colonization.ai.MissionHandler
import promitech.colonization.ai.MissionHandlerLogger.logger

class RequestGoodsMissionHandler : MissionHandler<RequestGoodsMission> {
    override fun handle(playerMissionsContainer: PlayerMissionsContainer, mission: RequestGoodsMission) {
        val player = playerMissionsContainer.player

        if (!player.settlements.containsId(mission.colonyId)) {
            logger.debug("player[%s].RequestGoodsMissionHandler no colony %s", player.getId(), mission.colonyId)
            mission.setDone()
            return
        }
    }
}