package promitech.colonization.screen.map.diplomacy

import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.Label
import promitech.colonization.ui.resources.Messages
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.player.Player
import promitech.colonization.isEmpty
import promitech.colonization.screen.ui.FrameWithCornersDrawableSkin
import promitech.colonization.toGdxArray
import com.badlogic.gdx.utils.Align
import promitech.colonization.GameResources
import promitech.colonization.ui.addListener

class PlayerSelectItem(val player : Player) {
	val name : String
	
	init {
		name = Messages.message(player.nationName)
	}
	
	override fun toString() : String {
		return name
	}
}

internal class InciteBox(
	val game : Game,
	val tradeType : TradeType,
	val player : Player,
	val skin : Skin,
	val addListener : (TradeItem) -> Unit,
	val tradeItems : ArrayList<TradeItem>
)
{
	
	private val selectBoxPlayerComparator = object : Comparator<PlayerSelectItem> {
		// first european than indian 
		override fun compare(p1: PlayerSelectItem, p2: PlayerSelectItem): Int {
			if (p1.player.isIndian() && p2.player.isIndian()) {
				return p1.name.compareTo(p2.name)
			} else {
				if (p1.player.isIndian) {
					return 1
				} else {
					return -1
				}
			}
		}
	}	
	
	val box = Table()
	val inciteSelectBox : SelectBox<PlayerSelectItem>
	val addButton : TextButton
	
	init {
		inciteSelectBox = SelectBox<PlayerSelectItem>(skin)
		addButton = TextButton(Messages.msg("negotiationDialog.add"), skin)
		
		createLayout()
		refreshList()
	}

	fun refreshList() {
		var items = game.players.entities()
			.filter { it.isLive }
			.filter { player.hasContacted(it) }
			.filter { player.notEqualsId(it) }
			.filter { wasNotAdded(it) }
			.map { PlayerSelectItem(it) }
			.toGdxArray(game.players.size())
		items.sort(selectBoxPlayerComparator)

		inciteSelectBox.setItems(items)
		disableAddButtonIfEmptyList()
	}

	private fun disableAddButtonIfEmptyList() {
		if (inciteSelectBox.getItems().isEmpty()) {
			addButton.setDisabled(true)
		} else {
			addButton.setDisabled(false)
		}
	}
	
	private fun wasNotAdded(player : Player) : Boolean {
		for (item : TradeItem in tradeItems) {
			if (item is InciteTradeItem && item.player.equalsId(player)) {
				return false
			}
		}
		return true
	}
			
	private fun createLayout() {
		val label = Label(Messages.msg("tradeItem.incite"), skin)
		
		addButton.addListener { _, _ ->
			val selectedItem = inciteSelectBox.selected
			if (selectedItem != null) {
				val tmpItems = Array(inciteSelectBox.getItems())
				tmpItems.removeValue(selectedItem, true)
				inciteSelectBox.setItems(tmpItems)
				disableAddButtonIfEmptyList()
				
				addListener(InciteTradeItem(selectedItem.player, tradeType))
			}
		}
				
		box.defaults()
			.padLeft(20f)
			.padRight(20f)
		box.add(label).align(Align.left).padTop(20f).row()
		box.add(inciteSelectBox).fillX().expandX().row()
		box.add(addButton).expandX().fillX().padBottom(20f).row()
		
		box.background = FrameWithCornersDrawableSkin(GameResources.instance)
	}
	
}