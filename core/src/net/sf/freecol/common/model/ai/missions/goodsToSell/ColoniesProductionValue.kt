package net.sf.freecol.common.model.ai.missions.goodsToSell

import com.badlogic.gdx.utils.IntArray
import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.MapIdEntities
import net.sf.freecol.common.model.Settlement
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.map.path.PathFinder
import net.sf.freecol.common.model.player.Player
import net.sf.freecol.common.model.specification.GoodsType

class ColoniesProductionValue(val player: Player) {
	val goodsTypeToValue: MapIdEntities<GoodsType> = Specification.instance.goodsTypeToScoreByPrice

	fun goldValue() : Int {
		var sum : Int = 0
		player.settlements.forEach { settlement ->
			sum += colonyProdValue(settlement.asColony())
		}
		return sum
	}
	
	fun colonyProdValue(colony: Colony) : Int {
		var sum = 0
		colony.productionSummary().entries().forEach { entry ->
			if (goodsTypeToValue.containsId(entry.key)) {
				sum += player.market().getSalePrice(goodsTypeToValue.getById(entry.key), entry.value)
			}
		}
		return sum
	}

	fun findSettlementWorthTakeGoodsToBuyUnit(navyUnit: Unit): Settlement? {
		val goodsTypeToValue: MapIdEntities<GoodsType> = Specification.instance.goodsTypeToScoreByPrice

		val navyUnitCapacity = navyUnit.unitType.space
		val theCheapestUnitPrice = player.europe.aiTheCheapestUnitPrice()

		val goodsPrices = IntArray(goodsTypeToValue.size())
		for (settlement in player.settlements) {
			if (!(settlement is Colony)) {
				continue
			}
			val navyCapacityPriceSum = navyUnitGoodsCapacityPrice(settlement, goodsPrices, navyUnitCapacity)
			if (navyCapacityPriceSum >= theCheapestUnitPrice) {
				return settlement
			}
		}
		return null
	}

	private fun navyUnitGoodsCapacityPrice(
		settlement: Settlement,
		goodsPrices: IntArray,
		navyUnitCapacity: Int
	): Int {
		val productionSummary = settlement.productionSummary()
		goodsPrices.clear()
		for (goodsType in goodsTypeToValue) {
			var amount = productionSummary.getQuantity(goodsType)
			amount += settlement.goodsContainer.goodsAmount(goodsType)
			goodsPrices.add(player.market().getSalePrice(goodsType, amount))
		}
		goodsPrices.sort()
		goodsPrices.reverse()
		var navyCapacityPriceSum = 0
		for (i in 0 .. minOf(navyUnitCapacity, goodsPrices.size)) {
			navyCapacityPriceSum += goodsPrices.get(i)
		}
		return navyCapacityPriceSum
	}

}