package net.sf.freecol.common.model.player

import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.ProductionSummary
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.specification.GameOptions
import promitech.colonization.Randomizer
import kotlin.math.max
import kotlin.math.min

fun generateRiseTaxNotification(game: Game, player: Player, action: Monarch.MonarchAction) {
    val mostValuableGoods = findMostValuableGoods(player, action)
    if (mostValuableGoods == null) {
        return
    }
    mostValuableGoods.tax = potentialTaxRaiseValue(game, player)
    player.eventsNotifications.addMessageNotificationAsFirst(mostValuableGoods)
}

private fun potentialTaxRaiseValue(game: Game, player: Player): Int {
    val taxAdjustment = Specification.options.getIntValue(GameOptions.TAX_ADJUSTMENT)
    val turn = game.turn.number
    val oldTax: Int = player.getTax()
    var adjust = max(1, (6 - taxAdjustment) * 10) // 20-60
    adjust = 1 + Randomizer.instance().randomInt(5 + turn / adjust)
    return min(oldTax + adjust, maximumTaxInGame())
}

/**
 * Get the most valuable goods available in one of the player's
 * colonies for the purposes of choosing a threat-to-boycott.  The
 * goods must not currently be boycotted, the player must have
 * traded in it, and the amount to be discarded will not exceed
 * GoodsContainer.CARGO_SIZE.
 */
private fun findMostValuableGoods(player: Player, action: Monarch.MonarchAction): MonarchActionNotification? {
    if (!player.isEuropean) {
        return null
    }
    val market = player.market()

    var man: MonarchActionNotification? = null
    var maxSellPriceValue = 0

    for (settlement in player.settlements) {
        val colony = settlement.asColony()

        for (marketGood in market.marketGoods) {
            if (marketGood.hasArrears() || !marketGood.isSalesExceedRiseTaxRange()) {
                continue
            }
            var amount: Int = colony.goodsContainer.goodsAmount(marketGood.goodsType)
            amount = min(amount, ProductionSummary.CARRIER_SLOT_MAX_QUANTITY)
            if (amount <= 0) {
                continue
            }
            val sellPrice: Int = marketGood.getCostToSell(amount)
            if (sellPrice > maxSellPriceValue) {
                maxSellPriceValue = sellPrice
                if (man == null) {
                    man = MonarchActionNotification(action)
                }
                man.setGoodsAmount(amount)
                man.setGoodsType(marketGood.goodsType)
                man.setColonyId(colony.id)
            }
        }
    }
    return man
}

private fun MarketData.isSalesExceedRiseTaxRange(): Boolean = this.sales > 500

fun maximumTaxInGame(): Int {
    return Specification.options.getIntValue(GameOptions.MAXIMUM_TAX)
}
