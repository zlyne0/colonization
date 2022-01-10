package net.sf.freecol.common.model.ai.missions.goodsToSell

import net.sf.freecol.common.model.player.Market.MarketTransactionLogger
import net.sf.freecol.common.model.player.TransactionEffectOnMarket
import net.sf.freecol.common.model.specification.AbstractGoods

class TestMarketTransactionLogger : MarketTransactionLogger {
	
	val sales = mutableListOf<AbstractGoods>()
	
	override fun logSale(transaction: TransactionEffectOnMarket) {
		sales.add(AbstractGoods(transaction.goodsTypeId, transaction.quantity))
	}

	override fun logPurchase(transaction: TransactionEffectOnMarket) {
	}

	fun containsSale(goodsTypeId: String, quantity: Int): Boolean {
		for (ag in sales) {
			if (ag.isEquals(goodsTypeId, quantity)) {
				return true
			}
		}
		return false
	}

    fun notContainsSale(goodsTypeId: String, quantity: Int): Boolean {
		for (ag in sales) {
			if (ag.isEquals(goodsTypeId, quantity)) {
				return false
			}
		}
		return true
    }
}