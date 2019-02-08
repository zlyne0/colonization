package promitech.colonization.screen.map.diplomacy

import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.player.Player
import promitech.colonization.orders.diplomacy.ScoreService

// You need drags to make stars come down.
internal class DiplomacyAgreement(val game : Game, val player : Player, val contactPlayer : Player) {
	
	enum class TradeStatus {
		ACCEPT, REJECT
	}
	
	private val ss = ScoreService()
	
	internal val offers = ArrayList<TradeItem>()
	internal val demands = ArrayList<TradeItem>()
	
	public fun calculate() : TradeStatus {
		val demandsValue = calculateTradeAgreements(demands)
		val offersValue = calculateTradeAgreements(offers)
		
		val tradeStatus = status(demandsValue, offersValue)
		var logs = "tradeStatus: $tradeStatus, demands($demandsValue): " + demands.toString() + ", offers($offersValue): " + offers.toString()
		System.out.println(logs)
		return tradeStatus
	}
	
	private fun status(demandsValue : Int, offersValue : Int) : TradeStatus {
		if (demandsValue == TradeItem.UnacceptableTradeValue || offersValue == TradeItem.UnacceptableTradeValue) {
			return TradeStatus.REJECT
		}
		if (offersValue >= demandsValue) {
			return TradeStatus.ACCEPT
		} else {
			return TradeStatus.REJECT
		}
	}
	
	private fun calculateTradeAgreements(items : List<TradeItem>) : Int {
		for (item : TradeItem in items) {
			item.calculateAgreementValue(isDemand(item), game, ss)
		}
		
		var agreementValue = 0
		for (item : TradeItem in items) {
			if (item.agreementValue == TradeItem.UnacceptableTradeValue) {
				return TradeItem.UnacceptableTradeValue
			}
			agreementValue += item.agreementValue
		}
		return agreementValue
	}
	
	fun remove(item : TradeItem) {
		if (isDemand(item)) {
			demands.remove(item)
		} else {
			offers.remove(item)
		}
	}
	
	fun add(item : TradeItem) {
		if (isDemand(item)) {
			demands.add(item)
		} else {
			offers.add(item)
		}
	}
	
	private fun isDemand(item : TradeItem) : Boolean {
		return item.fromPlayer.equalsId(contactPlayer) 
	}
	
	public fun acceptTrade() {
		// In freecol InGameController.csAcceptGold
		for (item in offers) {
			item.acceptTrade(game)
		}
		for (item in demands) {
			item.acceptTrade(game)
		}
	}
	
}