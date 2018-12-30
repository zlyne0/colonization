package promitech.colonization.screen.map.diplomacy

import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.player.Player
import net.sf.freecol.common.model.player.Stance
import net.sf.freecol.common.model.specification.Ability
import promitech.colonization.orders.diplomacy.ScoreService
import promitech.colonization.ui.resources.Messages

internal class StanceTradeItem(val stance: Stance, fromPlayer : Player, toPlayer : Player)
	: TradeItem(fromPlayer, toPlayer)
{
	override fun acceptTrade(game : Game) {
		fromPlayer.changeStance(toPlayer, stance)
	}

	override fun calculateAgreementValue(isDemand: Boolean, game: Game, ss: ScoreService) {
		// only in case human init contact with ai
		val franklin = fromPlayer.features.hasAbility(Ability.ALWAYS_OFFERED_PEACE)
		val ratio = ss.strengthRatio(fromPlayer, toPlayer)
		when (stance) {
			Stance.WAR -> {
				if (ratio < 0.33) {
					// ai much stronger than human  
					agreementValue = UnacceptableTradeValue
				} else if (ratio < 0.5) {
					// when human is weak, ai unlike declare war
					agreementValue = -(100 * ratio).toInt()
				} else {
					// when human is strong, ai more like declare war 
					agreementValue = (100 * ratio).toInt()
				}
			}
			Stance.PEACE -> {
				if (!fromPlayer.hasContacted(toPlayer)) {
					// peace on first contact
					agreementValue = 0
				} else {
					agreementValue = allianceTradeItemAgreementValue(franklin, ratio)
				}
			}
			Stance.CEASE_FIRE, Stance.ALLIANCE -> {
				agreementValue = allianceTradeItemAgreementValue(franklin, ratio)
			}
			else -> {}
		}
	}
	
	private fun allianceTradeItemAgreementValue(franklin : Boolean, ratio : Double) : Int {
		if (franklin) {
			// peace from begining
			return 0
		} else if (ratio > 0.77) {
			// human is much stronger than ai
			return UnacceptableTradeValue
		} else if (ratio > 0.5) {
			// human is stronger than ai, only small drawback 
			return -(100 * ratio).toInt()
		} else if (ratio > 0.33) {
			// human is much stronger than ai, so it give advantage
			return (100 * ratio).toInt()
		} else {
			// human is much, much stronger than ai, so it give advantage
			return 1000
		}
	}
	
	override fun createLabel(skin: Skin): Label {
		val labelStr = Messages.msg("model.stance." + stance.getKey())
		return Label(labelStr, skin)
	}
	
	override fun toString() : String {
		return "StanceTradeItem stance: $stance, agreementValue: $agreementValue"
	}
}