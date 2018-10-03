package promitech.colonization.screen.map.diplomacy

import com.badlogic.gdx.scenes.scene2d.ui.Label
import promitech.colonization.ui.resources.StringTemplate
import promitech.colonization.ui.resources.Messages
import promitech.colonization.GameResources
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import net.sf.freecol.common.model.player.Player

internal class GoldTradeItem(val gold : Int, tradeType : TradeType)
	: TradeItem(tradeType)
{
	
	override fun createLabel(skin : Skin) : Label {
		return Label(
			Messages.message(StringTemplate.template("tradeItem.gold.long").addAmount("%amount%", gold)),
			skin
		) 
	}
	
}