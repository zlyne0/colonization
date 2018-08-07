package promitech.colonization.screen.map.diplomacy

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Label

internal abstract class TradeItem(val tradeType : TradeType) {
	abstract fun createLabel(skin : Skin) : Label;
}