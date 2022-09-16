package net.sf.freecol.common.model.ai.missions.pioneer

import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.colonyproduction.GoodsCollection
import promitech.colonization.ai.MissionHandler
import promitech.colonization.ai.MissionHandlerLogger.logger

class RequestGoodsMissionHandler(val game: Game) : MissionHandler<RequestGoodsMission> {

    override fun handle(playerMissionsContainer: PlayerMissionsContainer, mission: RequestGoodsMission) {
        val player = playerMissionsContainer.player

        if (!player.settlements.containsId(mission.colonyId)) {
            logger.debug("player[%s].RequestGoodsMissionHandler no colony %s", player.getId(), mission.colonyId)
            mission.setDone()
            return
        }
    }

    fun handleUnloadCargoForGoodsRequest(requestGoodsMissionId: String, playerMissionsContainer: PlayerMissionsContainer) {
        val requestGoodsMission = playerMissionsContainer.findMission(requestGoodsMissionId)
        if (requestGoodsMission != null && requestGoodsMission is RequestGoodsMission) {
            requestGoodsMission.setDone()

            if (playerMissionsContainer.findMission(requestGoodsMission.purpose) != null) {
                val playerAiContainer = game.aiContainer.playerAiContainer(playerMissionsContainer.player)

                val colonySupplyGoods = playerAiContainer.findOrCreateColonySupplyGoods(requestGoodsMission.colonyId)
                colonySupplyGoods.makeReservation(requestGoodsMission.purpose, requestGoodsMission.goodsCollection)
            }

        }
    }
}