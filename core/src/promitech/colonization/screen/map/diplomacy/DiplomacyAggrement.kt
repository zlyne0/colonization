package promitech.colonization.screen.map.diplomacy

import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.player.Stance
import net.sf.freecol.common.model.player.Player
import net.sf.freecol.common.model.specification.Ability
import net.sf.freecol.common.model.Colony

// You need drags to make stars come down.
internal class DiplomacyAggrement(val game : Game, val player : Player, val contactPlayer : Player) {
	
	enum class TradeStatus {
		ACCEPT, REJECT
	}
	
	private val UnacceptableTradeValue = Integer.MIN_VALUE
	
	private val ss = ScoreService()
	
	public fun calculate(offers: List<TradeItem>, demands : List<TradeItem>) : TradeStatus {
		val demandsValue = calculateTradeAggrements(demands, player, contactPlayer)
		val offersValue = calculateTradeAggrements(offers, player, player)
		
		val tradeStatus = status(demandsValue, offersValue)
		var logs = "tradeStatus: $tradeStatus, demands($demandsValue): " + demands.toString() + ", offers($offersValue): " + offers.toString()
		System.out.println(logs)
		return tradeStatus
	}
	
	private fun status(demandsValue : Int, offersValue : Int) : TradeStatus {
		if (demandsValue == UnacceptableTradeValue || offersValue == UnacceptableTradeValue) {
			return TradeStatus.REJECT
		}
		if (offersValue >= demandsValue) {
			return TradeStatus.ACCEPT
		} else {
			return TradeStatus.REJECT
		}
	}
	
	private fun calculateTradeAggrements(items : List<TradeItem>, offerer : Player, demandFromPlayer : Player) : Int {
		for (item : TradeItem in items) {
			when (item) {
				is ColonyTradeItem -> {
					item.aggrementValue = ss.scoreColony(game, item.colony, demandFromPlayer)
				}
				is GoldTradeItem -> {
					if (demandFromPlayer.hasGold(item.gold)) {
						item.aggrementValue = item.gold
					} else {
						item.aggrementValue = UnacceptableTradeValue
					}
				}
				is InciteTradeItem -> inciteTradeItemAggrementValue(item, offerer, demandFromPlayer)
				is StanceTradeItem -> stanceTradeItemAggrementValue(item)
			}
		}
		
		var aggrementValue = 0
		for (item : TradeItem in items) {
			if (item.aggrementValue == UnacceptableTradeValue) {
				return UnacceptableTradeValue
			}
			aggrementValue += item.aggrementValue
		}
		return aggrementValue
	}
	
	private fun inciteTradeItemAggrementValue(item : InciteTradeItem, offerer : Player, demandFromPlayer : Player) {
		val victim = item.player
		when (demandFromPlayer.getStance(victim)) {
			Stance.ALLIANCE -> item.aggrementValue = UnacceptableTradeValue
			Stance.WAR -> item.aggrementValue = 0
			else -> {
				// AI never initiate negotiations
				// so calculate strength ratio for player and victim
				item.aggrementValue = (ss.strengthRatio(offerer, victim) * 30).toInt()
			} 
		}
	}
	
	private fun stanceTradeItemAggrementValue(item : StanceTradeItem) {
		// only in case human init contact with ai
		val franklin = player.features.hasAbility(Ability.ALWAYS_OFFERED_PEACE)
		val ratio = ss.strengthRatio(player, contactPlayer)
		when (item.stance) {
			Stance.WAR -> {
				if (ratio < 0.33) {
					// ai much stronger than human  
					item.aggrementValue = UnacceptableTradeValue
				} else if (ratio < 0.5) {
					// when human is weak, ai unlike declare war
					item.aggrementValue = -(100 * ratio).toInt()
				} else {
					// when human is strong, ai more like declare war 
					item.aggrementValue = (100 * ratio).toInt()
				}
			}
			Stance.PEACE -> {
				if (!player.hasContacted(contactPlayer)) {
					// peace on first contact
					item.aggrementValue = 0
				} else {
					allianceTradeItemAggrementValue(item, franklin, ratio)
				}
			}
			Stance.CEASE_FIRE, Stance.ALLIANCE -> {
				allianceTradeItemAggrementValue(item, franklin, ratio)
			}
			else -> {}
		}
	}
	
	private fun allianceTradeItemAggrementValue(item : StanceTradeItem, franklin : Boolean, ratio : Double) {
		if (franklin) {
			// peace from begining
			item.aggrementValue = 0
		} else if (ratio > 0.77) {
			// human is much stronger than ai
			item.aggrementValue = UnacceptableTradeValue
		} else if (ratio > 0.5) {
			// human is stronger than ai, only small drawback 
			item.aggrementValue = -(100 * ratio).toInt()
		} else if (ratio > 0.33) {
			// human is much stronger than ai, so it give advantage
			item.aggrementValue = (100 * ratio).toInt()
		} else {
			// human is much, much stronger than ai, so it give advantage
			item.aggrementValue = 1000
		}
	}
	public fun acceptTrade(offers: List<TradeItem>, demands : List<TradeItem>) {
		// In freecol InGameController.csAcceptGold
		for (item in offers) {
			when (item) {
				is GoldTradeItem -> {
					player.transferGoldToPlayer(item.gold, contactPlayer)
				}
				is ColonyTradeItem -> {
					transferColony(item.colony, contactPlayer)
				}
				is InciteTradeItem -> {
				}
				is StanceTradeItem -> {
				}
			}
		}
		for (item in demands) {
			when (item) {
				is GoldTradeItem -> {
					contactPlayer.transferGoldToPlayer(item.gold, player)
				}
				is ColonyTradeItem -> {
					transferColony(item.colony, player)
				}
				is InciteTradeItem -> {
				}
				is StanceTradeItem -> {
				}
			}
		}
	}

	private fun transferColony(colony : Colony, to : Player) {
		colony.changeOwner(to)
		colony.updateColonyFeatures()
		colony.updateColonyPopulation()
		to.fogOfWar.resetFogOfWarForSettlement(to, colony)
	}
	
}