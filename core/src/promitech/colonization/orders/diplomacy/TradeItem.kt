package promitech.colonization.screen.map.diplomacy

import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.player.Player
import promitech.colonization.orders.diplomacy.ScoreService

internal abstract class TradeItem(val fromPlayer : Player, val toPlayer : Player) {
	
	companion object {
		internal val UnacceptableTradeValue = Integer.MIN_VALUE
	}
	
	var agreementValue : Int = 0
	
	abstract fun calculateAgreementValue(isDemand : Boolean, game : Game, ss : ScoreService)
	
	abstract fun acceptTrade(game : Game)
	
	abstract fun createLabel(skin : Skin) : Label
	
	abstract override fun toString() : String
}