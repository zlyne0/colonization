package promitech.colonization.ai.military

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.player.Player
import promitech.colonization.ai.military.DefencePrice.DefencePurchase
import promitech.colonization.ai.military.DefencePrice.generateRequests
import promitech.colonization.orders.combat.DefencePower

class DefencePurchasePlaner(
    player: Player,
    private val threatModel: ThreatModel
) {

    private var budget = player.gold

    fun generateOrders(): List<ColonyDefencePurchase> {
        val colonyDefencePriority = threatModel.calculateColonyDefencePriority()

        var requestPurchaseCount = 0
        for (colonyThreat in colonyDefencePriority) {
            if (colonyThreat.colonyThreatWeights.threatPower > 0) {
                requestPurchaseCount++
            }
        }


        val purchases = ArrayList<ColonyDefencePurchase>()
        for (colonyThreat in colonyDefencePriority) {
            val orders = generatePurchasesForColony(colonyThreat, requestPurchaseCount)
            if (orders.isNotEmpty()) {
                requestPurchaseCount--
            }
            purchases.addAll(orders)
        }
        return purchases
    }

    private fun generatePurchasesForColony(colonyThreat: ColonyThreat, coloniesWithRequestPurchase: Int): List<ColonyDefencePurchase> {
        val expectedDefencePower = if (colonyThreat.war) {
            colonyThreat.colonyThreatWeights.threatPower
        } else {
            calculateExpectedDefencePower(colonyThreat.colonyThreatWeights.colonyWealth)
        }

        val potentialDefenceRequests = generateRequests(colonyThreat.colony)

        val defenceSimulation = ColonyDefenceSimulation(DefencePower.calculateColonyDefenceSnapshot(colonyThreat.colony))
        while (defenceSimulation.power() < expectedDefencePower) {
            val theBestPurchase = theBestPurchaseToBudget(budget, potentialDefenceRequests, defenceSimulation, coloniesWithRequestPurchase)
            if (theBestPurchase == null) {
                break
            }
            defenceSimulation.addPurchase(theBestPurchase)
            budget -= theBestPurchase.price
        }
        return defenceSimulation.purchases.map { purchase -> ColonyDefencePurchase(colonyThreat.colony, colonyThreat, purchase) }
    }

    private fun theBestPurchaseToBudget(
        budget: Int,
        potentialColonyPurchases: List<DefencePurchase>,
        colonyDefenceSimulation: ColonyDefenceSimulation,
        coloniesWithRequestPurchase: Int,
    ): DefencePurchase? {
        var purchase: DefencePurchase? = null

        for (pcp in potentialColonyPurchases) {
            if (colonyDefenceSimulation.hasBuilding() && pcp is DefencePurchase.ColonyBuildingPurchase) {
                // allow one building per order list
                continue
            }
            if (purchase == null || pcp.isBetterDefencePower(purchase, colonyDefenceSimulation)) {
                if (coloniesWithRequestPurchase == 1 && pcp.price <= budget || pcp.price <= budget * 0.5) {
                    purchase = pcp
                }
            }
        }
        return purchase
    }

    data class ColonyDefencePurchase(
        val colony: Colony,
        val colonyThreat: ColonyThreat,
        val purchase: DefencePurchase
    )

    companion object {

        @JvmStatic
        fun calculateExpectedDefencePower(colonyWealth: Int): Float {
            if (colonyWealth < 2000) {
                return 0f
            }
            if (colonyWealth <= 5000) {
                return (colonyWealth / 1000).toFloat()
            }
            if (colonyWealth < 10000) {
                return (colonyWealth / 1000).toFloat() + 1f
            }
            return 10f + ((colonyWealth - 10000) / 2000)
        }

    }

}