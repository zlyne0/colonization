package promitech.colonization.ai.military

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.ai.missions.military.DefenceMission
import net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission
import net.sf.freecol.common.model.player.Player
import promitech.colonization.ai.military.DefencePurchaseGenerator.DefencePurchase
import promitech.colonization.ai.military.DefencePurchaseGenerator.generateRequests
import promitech.colonization.ai.military.MilitaryLogger.logger
import promitech.colonization.orders.combat.DefencePower

class DefencePurchasePlaner(
    private val game: Game,
    private val player: Player,
    private val defencePlaner: DefencePlaner
) {

    private var budget = player.gold

    fun generateOrders(): List<ColonyDefencePurchase> {
        val colonyDefencePriority = defencePlaner.generateThreatModel(player).calculateColonyDefencePriority()

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
            if (logger.isDebug) {
                logger.debug(
                    "player[${colonyThreat.colony.owner.id}].defencePurchase"
                    + " colony[${colonyThreat.colony.name}], war: ${colonyThreat.war}"
                    + " power [${defenceSimulation.power()} < ${expectedDefencePower}], budget: ${budget}, price: ${theBestPurchase.price}"
                )
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

    fun handlePurchaseOrders(
        defencePurchaseOrders: List<ColonyDefencePurchase>,
        playerMissionContainer: PlayerMissionsContainer
    ) {
        val player = playerMissionContainer.player
        var bought = false
        for (order in defencePurchaseOrders) {
            when (order.purchase) {
                is DefencePurchase.ColonyBuildingPurchase -> {
                    if (player.hasGold(order.colony.getAIPriceForBuilding(order.purchase.buildingType))) {
                        order.colony.aiPayForBuilding(order.purchase.buildingType, game)
                        bought = true
                    }
                }
                is DefencePurchase.UnitPurchase -> {
                    val price = player.europe.aiUnitPrice(order.purchase.unitType, order.purchase.unitRole)
                    if (player.hasGold(price)) {
                        val unit = player.europe.aiBuyUnit(game, order.purchase.unitType, order.purchase.unitRole)

                        val defenceMission = DefenceMission(order.colony.tile, unit)
                        playerMissionContainer.addMission(defenceMission)

                        val transportRequest = TransportUnitRequestMission(game.turn, unit, order.colony.tile)
                        playerMissionContainer.addMission(defenceMission, transportRequest)
                        bought = true
                    }
                }
            }
        }

        if (bought) {
            defencePlaner.invalidateThreatModel()
        }
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