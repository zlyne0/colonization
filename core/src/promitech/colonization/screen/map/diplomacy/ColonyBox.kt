package promitech.colonization.screen.map.diplomacy

import net.sf.freecol.common.model.player.Player
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.scenes.scene2d.ui.Table
import promitech.colonization.screen.map.hud.ColonySelectItem
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import promitech.colonization.ui.resources.Messages
import promitech.colonization.ui.addListener
import promitech.colonization.screen.ui.FrameWithCornersDrawableSkin
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import promitech.colonization.GameResources
import net.sf.freecol.common.model.Colony

internal class ColonyBox(
	val player : Player,
	val tradeType : TradeType,
	val skin : Skin,
	val addListener : (TradeItem) -> Unit,
	val tradeItems : ArrayList<TradeItem>
)
{
	val box = Table()
	val colonySelectBox : SelectBox<ColonySelectItem>
	val addButton : TextButton
	
	init {
		colonySelectBox = SelectBox<ColonySelectItem>(skin)
		addButton = TextButton(Messages.msg("negotiationDialog.add"), skin)
				
		createLayout()
	}
	
	private fun wasNotAdded(colony : Colony) : Boolean {
		for (item : TradeItem in tradeItems) {
			if (item is ColonyTradeItem && item.colony.equalsId(colony)) {
				return false 
			}
		}
		return true
	}
	
	public fun refreshList() {
		var items = Array<ColonySelectItem>(player.settlements.size())
		player.settlements.entities()
			.filter { wasNotAdded(it.getColony()) }
			.map { ColonySelectItem(it.getColony()) }
			.forEach { items.add(it) }
		colonySelectBox.setItems(items)
		if (colonySelectBox.getItems().size == 0) {
			addButton.setDisabled(true)
		}
	}
	
	private fun createLayout() {
		refreshList()
		
		addButton.addListener { _, _ ->
			val selectedItem = colonySelectBox.selected
			if (selectedItem != null) {
				val tmpItems = Array(colonySelectBox.getItems())
				tmpItems.removeValue(selectedItem, true)
				colonySelectBox.setItems(tmpItems)
				
				addListener(ColonyTradeItem(tradeType, selectedItem.colony))
			}
		}
		
		box.defaults()
			.padLeft(20f)
			.padRight(20f)
		box.add(colonySelectBox).expandX().fillX().padTop(20f).row()
		box.add(addButton).expandX().fillX().padBottom(20f).row()
		
		box.background = FrameWithCornersDrawableSkin(
			Messages.msg("tradeItem.colony"),
			skin.get(LabelStyle::class.java).font,				
			GameResources.instance
		)
	}
}