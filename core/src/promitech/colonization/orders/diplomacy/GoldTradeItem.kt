package promitech.colonization.screen.map.diplomacy

import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.player.Player
import promitech.colonization.orders.diplomacy.ScoreService
import promitech.colonization.ui.resources.Messages
import promitech.colonization.ui.resources.StringTemplate

internal class GoldTradeItem(val gold : Int, fromPlayer : Player, toPlayer : Player)
	: TradeItem(fromPlayer, toPlayer)
{
	override fun acceptTrade(game : Game) {
		fromPlayer.transferGoldToPlayer(gold, toPlayer)
	}

	override fun calculateAgreementValue(isDemand: Boolean, game: Game, ss: ScoreService) {
		if (fromPlayer.hasGold(gold)) {
			agreementValue = gold
		} else {
			agreementValue = UnacceptableTradeValue
		}
	}
	
	override fun createLabel(skin : Skin) : Label {
		return Label(
			Messages.message(StringTemplate.template("tradeItem.gold.long").addAmount("%amount%", gold)),
			skin
		) 
	}
	
	override fun toString() : String {
		return "GoldTradeItem gold: $gold, agreementValue: $agreementValue"
	}
	
}