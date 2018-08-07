package promitech.colonization.screen.map.diplomacy

import net.sf.freecol.common.model.Colony
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Label
import promitech.colonization.ui.resources.Messages
import promitech.colonization.ui.resources.StringTemplate

internal class ColonyTradeItem(tradeType : TradeType, val colony : Colony) : TradeItem(tradeType) {
	override fun createLabel(skin: Skin): Label {
		return Label(
			Messages.message(StringTemplate.template("tradeItem.colony.long").add("%colony%", colony.getName())),
			skin
		) 
	}
}