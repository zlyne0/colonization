package promitech.colonization.screen.map.diplomacy

import net.sf.freecol.common.model.player.Stance
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Label
import promitech.colonization.ui.resources.Messages
import promitech.colonization.ui.resources.StringTemplate

internal class StanceTradeItem(val stance: Stance, tradeType: TradeType)
	: TradeItem(tradeType)
{
	override fun createLabel(skin: Skin): Label {
		val labelStr = Messages.msg("model.stance." + stance.getKey())
		return Label(labelStr, skin)
	}
}