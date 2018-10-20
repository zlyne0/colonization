package promitech.colonization.screen.map.diplomacy

import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.player.Stance
import net.sf.freecol.common.model.player.Player
import net.sf.freecol.common.model.specification.Ability
import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.Tile
import promitech.colonization.Direction

// You need drags to make stars come down.
internal class DiplomacyAgreement(val game : Game, val player : Player, val contactPlayer : Player) {
	
	enum class TradeStatus {
		ACCEPT, REJECT
	}
	
	private val UnacceptableTradeValue = Integer.MIN_VALUE
	
	private val ss = ScoreService()
	
	public fun calculate(offers: List<TradeItem>, demands : List<TradeItem>) : TradeStatus {
		val demandsValue = calculateTradeAgreements(demands, player, contactPlayer)
		val offersValue = calculateTradeAgreements(offers, player, player)
		
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
	
	private fun calculateTradeAgreements(items : List<TradeItem>, offerer : Player, demandFromPlayer : Player) : Int {
		for (item : TradeItem in items) {
			when (item) {
				is ColonyTradeItem -> {
					if (item.tradeType == TradeType.Demand && demandFromPlayer.settlements.size() < 5) {
						item.agreementValue = UnacceptableTradeValue
					} else {
						item.agreementValue = ss.scoreColony(game, item.colony, demandFromPlayer)
					}
				}
				is GoldTradeItem -> {
					if (demandFromPlayer.hasGold(item.gold)) {
						item.agreementValue = item.gold
					} else {
						item.agreementValue = UnacceptableTradeValue
					}
				}
				is InciteTradeItem -> inciteTradeItemAgreementValue(item, offerer, demandFromPlayer)
				is StanceTradeItem -> stanceTradeItemAgreementValue(item)
			}
		}
		
		var agreementValue = 0
		for (item : TradeItem in items) {
			if (item.agreementValue == UnacceptableTradeValue) {
				return UnacceptableTradeValue
			}
			agreementValue += item.agreementValue
		}
		return agreementValue
	}
	
	private fun inciteTradeItemAgreementValue(item : InciteTradeItem, offerer : Player, demandFromPlayer : Player) {
		val victim = item.player
		when (demandFromPlayer.getStance(victim)) {
			Stance.ALLIANCE -> item.agreementValue = UnacceptableTradeValue
			Stance.WAR -> item.agreementValue = 0
			else -> {
				// AI never initiate negotiations
				// so calculate strength ratio for player and victim
				item.agreementValue = (ss.strengthRatio(offerer, victim) * 30).toInt()
			} 
		}
	}
	
	private fun stanceTradeItemAgreementValue(item : StanceTradeItem) {
		// only in case human init contact with ai
		val franklin = player.features.hasAbility(Ability.ALWAYS_OFFERED_PEACE)
		val ratio = ss.strengthRatio(player, contactPlayer)
		when (item.stance) {
			Stance.WAR -> {
				if (ratio < 0.33) {
					// ai much stronger than human  
					item.agreementValue = UnacceptableTradeValue
				} else if (ratio < 0.5) {
					// when human is weak, ai unlike declare war
					item.agreementValue = -(100 * ratio).toInt()
				} else {
					// when human is strong, ai more like declare war 
					item.agreementValue = (100 * ratio).toInt()
				}
			}
			Stance.PEACE -> {
				if (!player.hasContacted(contactPlayer)) {
					// peace on first contact
					item.agreementValue = 0
				} else {
					allianceTradeItemAgreementValue(item, franklin, ratio)
				}
			}
			Stance.CEASE_FIRE, Stance.ALLIANCE -> {
				allianceTradeItemAgreementValue(item, franklin, ratio)
			}
			else -> {}
		}
	}
	
	private fun allianceTradeItemAgreementValue(item : StanceTradeItem, franklin : Boolean, ratio : Double) {
		if (franklin) {
			// peace from begining
			item.agreementValue = 0
		} else if (ratio > 0.77) {
			// human is much stronger than ai
			item.agreementValue = UnacceptableTradeValue
		} else if (ratio > 0.5) {
			// human is stronger than ai, only small drawback 
			item.agreementValue = -(100 * ratio).toInt()
		} else if (ratio > 0.33) {
			// human is much stronger than ai, so it give advantage
			item.agreementValue = (100 * ratio).toInt()
		} else {
			// human is much, much stronger than ai, so it give advantage
			item.agreementValue = 1000
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
					player.changeStance(item.player, Stance.WAR)
				}
				is StanceTradeItem -> {
					player.changeStance(contactPlayer, item.stance)
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
					contactPlayer.changeStance(item.player, Stance.WAR)
				}
				is StanceTradeItem -> {
					player.changeStance(contactPlayer, item.stance)
				}
			}
		}
	}

	private fun transferColony(colony : Colony, to : Player) {
		if (colony.isCoastland()) {
			moveNavyToFreeNeighbourTile(colony)
		}
		
		colony.changeOwner(to)
		colony.updateColonyFeatures()
		colony.updateColonyPopulation()
		to.fogOfWar.resetFogOfWarForSettlement(to, colony)
	}
	
	private fun moveNavyToFreeNeighbourTile(colony : Colony) {
		var seaTile = firstSeaNeighbourTile(colony.tile, colony.getOwner())
		if (seaTile != null) {
			for (unit : Unit in ArrayList(colony.tile.units.entities())) {
				if (unit.isNaval()) {
					unit.changeUnitLocation(seaTile)
					unit.setState(Unit.UnitState.ACTIVE)
				}
			}
		}
	}
	
	private fun firstSeaNeighbourTile(tile : Tile, player : Player) : Tile? {
		for (direction : Direction in Direction.allDirections) {
			var neighbourTile = game.map.getTile(tile, direction)
			if (neighbourTile != null && neighbourTile.type.isWater()) {
				if (neighbourTile.units.isEmpty()) {
					return neighbourTile  
				}
				var unit = neighbourTile.units.first()
				if (player.equalsId(unit.getOwner())) {
					return neighbourTile
				}
			}
		}
		return null
	}
	
}