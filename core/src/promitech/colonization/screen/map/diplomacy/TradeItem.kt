package promitech.colonization.screen.map.diplomacy

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Label

internal abstract class TradeItem(val tradeType : TradeType) {
	var aggrementValue : Int = 0
	
	abstract fun createLabel(skin : Skin) : Label;
}