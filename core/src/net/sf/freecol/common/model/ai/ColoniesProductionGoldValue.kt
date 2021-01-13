package net.sf.freecol.common.model.ai

import net.sf.freecol.common.model.player.Player
import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.MapIdEntities
import net.sf.freecol.common.model.specification.GoodsType


class ColoniesProductionGoldValue(val player: Player, val goodsTypeToValue: MapIdEntities<GoodsType>) {
	
	fun goldValue() : Int {
		var sum : Int = 0
		player.settlements.forEach { settlement ->
			sum += colonyProdValue(settlement.asColony())
		}
		return sum
	}
	
	fun colonyProdValue(colony : Colony) : Int {
		var sum = 0
		colony.productionSummary().entries().forEach { entry ->
			if (goodsTypeToValue.containsId(entry.key)) {
				sum += player.market().getSalePrice(goodsTypeToValue.getById(entry.key), entry.value)
			}
		}
		return sum
	}
}