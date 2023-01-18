package promitech.colonization.ai.purchase

import com.badlogic.gdx.utils.ObjectIntMap
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.ai.missions.foreachMission
import net.sf.freecol.common.model.ai.missions.goodsToSell.TransportGoodsToSellMissionPlaner
import net.sf.freecol.common.model.ai.missions.pioneer.RequestGoodsMission
import net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission
import net.sf.freecol.common.model.player.Player
import promitech.colonization.ai.MissionHandlerLogger.logger
import promitech.colonization.ai.UnitTypeId
import promitech.colonization.ai.calculateNavyTotalCargo
import kotlin.LazyThreadSafetyMode.NONE

sealed class BuyShipOrder {
    data class BuyShipNow(val unitType: UnitType): BuyShipOrder() {
        override fun toString(): String {
            return "BuyShipNow[${unitType.id}]"
        }
    }
    data class CollectGoldAndBuy(val unitType: UnitType): BuyShipOrder() {
        override fun toString(): String {
            return "CollectGoldAndBuy[${unitType.id}]"
        }
    }
    class NoNeedToBuy(): BuyShipOrder()
    class NoGoldToBuy(): BuyShipOrder()
}

class BuyShipPlaner(
    private val player: Player,
    private val specification: Specification,
    private val transportGoodsToSellMissionPlaner: TransportGoodsToSellMissionPlaner,
    private val playerMissionsContainer: PlayerMissionsContainer
) {

    private val cargoRequestToCapacityRatio = 1.8

    private val transportShips = listOf(
        specification.unitTypes.getById(UnitType.CARAVEL),
        specification.unitTypes.getById(UnitType.MERCHANTMAN),
        specification.unitTypes.getById(UnitType.GALLEON),
        specification.unitTypes.getById(UnitType.FRIGATE)
    )

    private val ownedShips by lazy(NONE) { calculateOwnedShips() }
    private val cargoAmountWaitingForTransport by lazy(NONE) { transportGoodsToSellMissionPlaner.potentialCargoAmountWaitingForTransport(player) }
    private val cargoSlotsRequest: CargoSlotsRequest by lazy(NONE) { calculateCargoSlotRequests() }

    fun createBuyShipPlan(): BuyShipOrder {
        val freeNavyCargoSlots = player.calculateNavyTotalCargo(transportShipsToSet())

        if (cargoSlotsRequest.sum() > freeNavyCargoSlots * cargoRequestToCapacityRatio) {
            val lastOwnPlusOne = lastOwnPlusOne()
            val buyOrder = calculateBuyOrder(lastOwnPlusOne)

            if (logger.isDebug) {
                logger.debug("player[%s].buyShipPlan cargoSlotRequest > capacity [ %s > %s ] buy ship request, order: %s",
                    player.id,
                    cargoSlotsRequest.sum(),
                    freeNavyCargoSlots * cargoRequestToCapacityRatio,
                    buyOrder
                )
            }
            return buyOrder
        } else {
            if (logger.isDebug) {
                logger.debug("player[%s].buyShipPlan cargoSlotRequest < capacity [ %s < %s ] do not need buy ship",
                    player.id,
                    cargoSlotsRequest.sum(),
                    freeNavyCargoSlots * cargoRequestToCapacityRatio
                )
            }
            return BuyShipOrder.NoNeedToBuy()
        }
    }

    fun handleBuyOrders(buyShipOrder: BuyShipOrder) {
        when (buyShipOrder) {
            is BuyShipOrder.BuyShipNow -> player.europe.buyUnitByAI(buyShipOrder.unitType)
            is BuyShipOrder.CollectGoldAndBuy -> return
            is BuyShipOrder.NoGoldToBuy -> return
            is BuyShipOrder.NoNeedToBuy -> return
        }
    }

    private fun calculateBuyOrder(shipType: UnitType): BuyShipOrder {
        if (player.hasGold(shipType.price)) {
            return BuyShipOrder.BuyShipNow(shipType)
        }
        if (player.gold + cargoAmountWaitingForTransport.cargoValue >= shipType.price) {
            return BuyShipOrder.CollectGoldAndBuy(shipType)
        }

        val cheaperVersionShipType = minusOne(shipType)
        if (player.hasGold(cheaperVersionShipType.price)) {
            return BuyShipOrder.BuyShipNow(cheaperVersionShipType)
        }
        if (player.gold + cargoAmountWaitingForTransport.cargoValue >= cheaperVersionShipType.price) {
            return BuyShipOrder.CollectGoldAndBuy(cheaperVersionShipType)
        }
        if (hasNotAnyShip()) {
            return BuyShipOrder.CollectGoldAndBuy(cheaperVersionShipType)
        }
        return BuyShipOrder.NoGoldToBuy()
    }

    private fun lastOwnPlusOne(): UnitType {
        var lastOwned = transportShips.first()

        for (transportShip in transportShips) {
            if (ownedShips.get(transportShip.id, 0) > 0) {
                lastOwned = transportShip
            }
        }
        return plusOne(lastOwned)
    }

    private fun plusOne(unitType: UnitType): UnitType {
        var found = false
        for (transportShip in transportShips) {
            if (transportShip.equalsId(unitType)) {
                found = true
            } else if (found) {
                return transportShip
            }
        }
        return unitType
    }

    private fun minusOne(unitType: UnitType): UnitType {
        var preview = unitType
        for (transportShip in transportShips) {
            if (transportShip.equalsId(unitType)) {
                return preview
            }
            preview = transportShip
        }
        return unitType
    }

    private fun hasNotAnyShip(): Boolean {
        for (transportShip in transportShips) {
            if (ownedShips.get(transportShip.id, 0) > 0) {
                return false
            }
        }
        return true
    }

    private fun calculateCargoSlotRequests(): CargoSlotsRequest {
        val cargoSlotsRequest = CargoSlotsRequest()
        cargoSlotsRequest.cargoAmountWaitingForTransport = cargoAmountWaitingForTransport.cargoSlots

        playerMissionsContainer.foreachMission(TransportUnitRequestMission::class.java, { transportUnitRequestMission ->
            if (transportUnitRequestMission.isWorthEmbark()) {
                cargoSlotsRequest.transportUnitRequest++
            }
        })
        playerMissionsContainer.foreachMission(RequestGoodsMission::class.java, { requestGoodsMission ->
            cargoSlotsRequest.requestGoods++
        })
        return cargoSlotsRequest
    }

    private fun calculateOwnedShips(): ObjectIntMap<UnitTypeId> {
        val ownedShips = ObjectIntMap<UnitTypeId>()
        for (unit in player.units) {
            if (unit.isNaval && !unit.isDamaged) {
                ownedShips.getAndIncrement(unit.unitType.id, 0, 1)
            }
        }
        return ownedShips
    }

    private fun transportShipsToSet(): Set<UnitTypeId> {
        val ids = mutableSetOf<UnitTypeId>()
        for (transportShip in transportShips) {
            ids.add(transportShip.id)
        }
        return ids
    }

    fun printDebugInfo() {
        for (ownShipEntry in ownedShips) {
            println("ownShipEntry[${ownShipEntry.key}] = ${ownShipEntry.value}")
        }
        println("CargoSlotsRequest.cargoAmountWaitingForTransport = ${cargoSlotsRequest.cargoAmountWaitingForTransport}")
        println("CargoSlotsRequest.requestGoods = ${cargoSlotsRequest.requestGoods}")
        println("CargoSlotsRequest.transportUnitRequest = ${cargoSlotsRequest.transportUnitRequest}")
        println("CargoSlotsRequest.sum = ${cargoSlotsRequest.sum()}")
        val freeNavyCargoSlots = player.calculateNavyTotalCargo(transportShipsToSet())
        println("freeNavyCargoSlots = ${freeNavyCargoSlots}")

        val signal = cargoSlotsRequest.sum() > freeNavyCargoSlots * cargoRequestToCapacityRatio
        println("buy ship signal (${cargoSlotsRequest.sum()} > ${freeNavyCargoSlots * cargoRequestToCapacityRatio}) = ${signal}")
    }

    private class CargoSlotsRequest {
        var transportUnitRequest: Int = 0
        var requestGoods: Int = 0
        var cargoAmountWaitingForTransport: Int = 0

        fun sum(): Int = transportUnitRequest + requestGoods + cargoAmountWaitingForTransport
    }
}