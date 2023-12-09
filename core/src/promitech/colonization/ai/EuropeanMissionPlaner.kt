package promitech.colonization.ai

import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.ai.missions.goodsToSell.TransportGoodsToSellMissionPlaner
import net.sf.freecol.common.model.ai.missions.pioneer.PioneerMissionPlaner
import net.sf.freecol.common.model.ai.missions.scout.ScoutMissionPlaner
import net.sf.freecol.common.model.ai.missions.workerrequest.ColonyWorkerRequestPlaner
import net.sf.freecol.common.model.map.path.PathFinder
import net.sf.freecol.common.model.player.Player
import promitech.colonization.ai.military.DefencePlaner
import promitech.colonization.ai.military.DefencePurchasePlaner
import promitech.colonization.ai.purchase.BuyShipOrder.CollectGoldAndBuy
import promitech.colonization.ai.purchase.BuyShipPlaner
import promitech.colonization.ai.purchase.PurchasePlaner

/*
   Zadaniem człowieka jest życie, nie zaś egzystencja.
   Nie będę tracić dni na próbę ich przedłużenia.
   Wykorzystam swój czas.
 */
class EuropeanMissionPlaner(
    val game: Game,
    val pathFinder: PathFinder,
    val pathFinder2: PathFinder
) {
    private val transportGoodsToSellMissionPlaner: TransportGoodsToSellMissionPlaner = TransportGoodsToSellMissionPlaner(game, pathFinder)
    private val scoutMissionPlaner: ScoutMissionPlaner = ScoutMissionPlaner(game, pathFinder, pathFinder2)
    private val pioneerMissionPlaner: PioneerMissionPlaner = PioneerMissionPlaner(game, pathFinder)
    private val defencePlaner = DefencePlaner(game, pathFinder)
    private val colonyWorkerRequestPlaner: ColonyWorkerRequestPlaner = ColonyWorkerRequestPlaner(
        game, pathFinder, pathFinder2, pioneerMissionPlaner, defencePlaner
    )
    private val buyGoodsPlaner: BuyGoodsPlaner = BuyGoodsPlaner(game)
    private val purchasePlaner: PurchasePlaner = PurchasePlaner(game)
    private val navyMissionPlaner: NavyMissionPlaner = NavyMissionPlaner(purchasePlaner, colonyWorkerRequestPlaner, transportGoodsToSellMissionPlaner)

    fun prepareMissions(player: Player, playerMissionContainer: PlayerMissionsContainer) {
        val colonyProductionPlaner = ColonyProductionPlaner(player)
        purchasePlaner.init(player, colonyProductionPlaner)

        scoutMissionPlaner.createMissionFromUnusedUnits(player, playerMissionContainer)
        colonyWorkerRequestPlaner.createMissionFromUnusedUnits(player, playerMissionContainer)
        handlePurchases(player, playerMissionContainer)

        navyMissionPlaner.plan(player, playerMissionContainer)

        colonyProductionPlaner.generateAndSetColonyProductionPlan()
        purchasePlaner.buyBuildings()
    }

    private fun handlePurchases(player: Player, playerMissionContainer: PlayerMissionsContainer) {
        val buyShipPlaner = BuyShipPlaner(player, Specification.instance, transportGoodsToSellMissionPlaner, playerMissionContainer)
        val buyShipPlan = buyShipPlaner.createBuyShipPlan()
        if (buyShipPlan is CollectGoldAndBuy) {
            purchasePlaner.avoidPurchasesAndCollectGold()
            return
        }
        buyShipPlaner.handleBuyOrders(buyShipPlan)

        val defencePurchasePlaner = DefencePurchasePlaner(game, player, defencePlaner)
        val defencePurchaseOrders = defencePurchasePlaner.generateOrders()
        defencePurchasePlaner.handlePurchaseOrders(defencePurchaseOrders, playerMissionContainer)

        val scoutBuyPlan = scoutMissionPlaner.createBuyPlan(player, playerMissionContainer)
        if (scoutBuyPlan != null) {
            scoutMissionPlaner.handleBuyPlan(scoutBuyPlan, player, playerMissionContainer)
        }
        val pioneerBuyPlan = pioneerMissionPlaner.createBuyPlan(player, playerMissionContainer)
        if (pioneerBuyPlan != null) {
            pioneerMissionPlaner.handlePioneerBuyPlan(pioneerBuyPlan, playerMissionContainer)
        }
        buyGoodsPlaner.handleBuyOrders(player, playerMissionContainer)
    }

    fun setAvoidPurchasesAndCollectGold() {
        purchasePlaner.avoidPurchasesAndCollectGold()
    }
}