package promitech.colonization.screen.map.diplomacy

import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.utils.Array
import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.player.Player
import promitech.colonization.GameResources
import promitech.colonization.isEmpty
import promitech.colonization.screen.ui.FrameWithCornersDrawableSkin
import promitech.colonization.toGdxArray
import promitech.colonization.ui.addListener
import promitech.colonization.ui.resources.Messages

class ColonySelectItem(val colony : Colony)  {
	override fun toString() : String {
		return colony.getName()
	} 
}

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
	
	public fun refreshList() {
		var items = player.settlements.entities()
			.filter { wasNotAdded(it.getColony()) }
			.map { ColonySelectItem(it.getColony()) }
			.toGdxArray(player.settlements.size())
		colonySelectBox.setItems(items)
		if (colonySelectBox.getItems().isEmpty()) {
			addButton.setDisabled(true)
		}
	}

	private fun wasNotAdded(colony : Colony) : Boolean {
		for (item : TradeItem in tradeItems) {
			if (item is ColonyTradeItem && item.colony.equalsId(colony)) {
				return false 
			}
		}
		return true
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