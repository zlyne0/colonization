package promitech.colonization.screen.map.diplomacy

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Label
import net.sf.freecol.common.model.player.Player
import promitech.colonization.ui.resources.Messages
import promitech.colonization.ui.resources.StringTemplate
import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.player.Stance

internal class InciteTradeItem(val victim : Player, fromPlayer : Player, toPlayer : Player)
	: TradeItem(fromPlayer, toPlayer)
{
	override fun acceptTrade(game : Game) {
		fromPlayer.changeStance(victim, Stance.WAR)
	}

	override fun calculateAgreementValue(isDemand: Boolean, game: Game, ss: ScoreService) {
		when (fromPlayer.getStance(victim)) {
			Stance.ALLIANCE -> agreementValue = UnacceptableTradeValue
			Stance.WAR -> agreementValue = 0
			else -> {
				// AI never initiate negotiations
				// so calculate strength ratio for player and victim
				agreementValue = (ss.strengthRatio(fromPlayer, victim) * 30).toInt()
			} 
		}
	}
	
	override fun createLabel(skin: Skin): Label {
		val labelStr = Messages.message(
			StringTemplate.template("inciteTradeItem.description")
				.addStringTemplate("%nation%", victim.getNationName())
		)
		return Label(labelStr, skin)
	}
	
	override fun toString() : String {
		return "InciteTradeItem player: ${victim.getId()}, agreementValue: $agreementValue"
	}
	
}