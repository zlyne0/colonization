package promitech.colonization.screen.map.diplomacy

import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.utils.Array
import net.sf.freecol.common.model.player.Player
import net.sf.freecol.common.model.player.Stance
import promitech.colonization.GameResources
import promitech.colonization.screen.map.hud.StanceSelectItem
import promitech.colonization.screen.ui.FrameWithCornersDrawableSkin
import promitech.colonization.ui.addListener
import promitech.colonization.ui.resources.Messages

internal class StanceBox(
	val tradeType : TradeType,
	val player : Player,
	val contactPlayer : Player,
	val skin : Skin,
	val addListener : (TradeItem) -> Unit,
	val tradeItems : ArrayList<TradeItem>
)
{
	val box = Table()
	val addButton : TextButton
	val stanceSelectBox : SelectBox<StanceSelectItem>
	
	init {
		addButton = TextButton(Messages.msg("negotiationDialog.add"), skin)
		stanceSelectBox = SelectBox<StanceSelectItem>(skin)
		
		createLayout()
	}
	
	fun refresh() {
		val found = tradeItems
			.filter { it is StanceTradeItem }
			.isNotEmpty()
		if (found) {
			stanceSelectBox.setDisabled(true)
			addButton.setDisabled(true)
		} else {
			stanceSelectBox.setDisabled(false)
			addButton.setDisabled(false)
		}
	}
	
	private fun createLayout() {
		addButton.addListener { _, _ ->
			val selectedStance = stanceSelectBox.selected
			if (selectedStance != null) {
				stanceSelectBox.setDisabled(true)
				addButton.setDisabled(true)
				addListener(StanceTradeItem(selectedStance.stance, tradeType))
			}
		}
		stanceSelectBox.setItems(stanceBoxItems())

		box.defaults()
			.padLeft(20f)
			.padRight(20f)
		box.add(stanceSelectBox).fillX().expandX().padTop(20f).row()
		box.add(addButton).expandX().fillX().padBottom(20f).row()
		
		box.background = FrameWithCornersDrawableSkin(
			Messages.msg("tradeItem.stance"),
			skin.get(LabelStyle::class.java).font,				
			GameResources.instance
		)
	}
	
	private fun stanceBoxItems() : Array<StanceSelectItem> {
		var items = Array<StanceSelectItem>()
		
		val stance = player.getStance(contactPlayer);
		if (stance != Stance.WAR) {
			items.add(StanceSelectItem(Stance.WAR))
		} else {
			items.add(StanceSelectItem(Stance.CEASE_FIRE))
		}
		if (stance != Stance.PEACE && stance != Stance.UNCONTACTED) {
			items.add(StanceSelectItem(Stance.PEACE))
		}
		if (stance == Stance.PEACE) {
			items.add(StanceSelectItem(Stance.ALLIANCE))
		}
		return items
	}
	
}