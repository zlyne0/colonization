package promitech.colonization.ai

import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.ai.missions.foreachMission
import net.sf.freecol.common.model.ai.missions.pioneer.RequestGoodsMission
import net.sf.freecol.common.model.colonyproduction.amount
import net.sf.freecol.common.model.colonyproduction.type
import net.sf.freecol.common.model.player.Player

class BuyGoodsPlaner(val game: Game) {

    fun handleBuyOrders(player: Player, playerMissionContainer: PlayerMissionsContainer) {
        playerMissionContainer.foreachMission(RequestGoodsMission::class.java) { mission ->
            if (!mission.boughtInEurope) {
                buyGoods(player, mission)
            }
        }
    }

    private fun buyGoods(player: Player, mission: RequestGoodsMission) {
        val price = player.market().aiBidPrice(mission.goodsCollection)
        if (player.hasGold((price * 2.5).toInt())) {
            player.market().aiBuyGoods(game, player, mission.goodsCollection)
            mission.setAsBoughtInEurope()
        }
    }
}