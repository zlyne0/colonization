package promitech.colonization.screen.map.diplomacy

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Label
import net.sf.freecol.common.model.player.Player
import promitech.colonization.ui.resources.Messages
import promitech.colonization.ui.resources.StringTemplate

internal class InciteTradeItem(val player : Player, tradeType : TradeType)
	: TradeItem(tradeType)
{
	override fun createLabel(skin: Skin): Label {
		val labelStr = Messages.message(
			StringTemplate.template("inciteTradeItem.description")
				.addStringTemplate("%nation%", player.getNationName())
		)
		return Label(labelStr, skin)
	}
	
	override fun toString() : String {
		return "InciteTradeItem player: ${player.getId()}, aggrementValue: $aggrementValue"
	}
	
}