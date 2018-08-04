package promitech.colonization.screen.map.hud

import promitech.colonization.ui.ModalDialog
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import promitech.colonization.ui.resources.Messages
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.scenes.scene2d.ui.Label
import net.sf.freecol.common.model.player.Player
import promitech.colonization.ui.resources.StringTemplate
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldFilter
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox
import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.Settlement
import com.badlogic.gdx.utils.Array
import net.sf.freecol.common.model.Game
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener
import promitech.colonization.GameResources
import promitech.colonization.screen.ui.FrameWithCornersDrawableSkin
import net.sf.freecol.common.model.player.Stance

class ColonySelectItem(val colony : Colony)  {
	override fun toString() : String {
		return colony.getName()
	} 
}

class PlayerSelectItem(val player : Player) {
	val name : String
	
	init {
		name = Messages.message(player.nationName)
	}
	
	override fun toString() : String {
		return name
	}
}

class StanceSelectItem(val stance : Stance) {
	val name : String
	
	init {
		name = Messages.msg("model.stance." + stance.name.toLowerCase())
	}
	
	override fun toString() : String {
		return name
	}
}

class DiplomacyContactDialog(
	val game : Game,
	val player : Player,
	val contactPlayer : Player
)
	: ModalDialog<DiplomacyContactDialog>()
{

	val selectBoxPlayerComparator = object : Comparator<PlayerSelectItem> {
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
	
	init {
		val headerLabel = Label(Messages.msg("negotiationDialog.title.diplomatic"), skin)

		val demandLabelStr = Messages.message(
			StringTemplate.template("negotiationDialog.demand")
				.addStringTemplate("%nation%", player.getNationName())
		)
		val offerLabelStr = Messages.message(
			StringTemplate.template("negotiationDialog.offer")
				.addStringTemplate("%nation%", player.getNationName())
		)
		

		val demandLabel = Label(demandLabelStr, skin)
		var situationLabel = Label(
			Messages.message(
				StringTemplate.template("negotiationDialog.send.diplomatic")
					.addStringTemplate("%nation%", contactPlayer.getNationName())
				),
			skin
		)
		val offerLabel = Label(offerLabelStr, skin)
				
		
		val demandLayout = Table()
		demandLayout.defaults().fillX().expandX().padTop(10f).padLeft(10f).padRight(10f)
		demandLayout.add(demandLabel).row()
		demandLayout.add(createGoldBox()).row()
		demandLayout.add(createColonyBox(contactPlayer)).row()
		demandLayout.add(createInciteBox(contactPlayer)).row()
		
		val sentItemsLayout = Table()
		sentItemsLayout.defaults().fillX().expandX().padTop(10f).padLeft(10f).padRight(10f)
		sentItemsLayout.add(situationLabel).row()
		
		val offerLayout = Table()
		offerLayout.defaults().fillX().expandX().padTop(10f).padLeft(10f).padRight(10f)
		offerLayout.add(offerLabel).row()
		offerLayout.add(createGoldBox()).row()
		offerLayout.add(createColonyBox(player)).row()
		offerLayout.add(createStanceBox(player)).row()
		offerLayout.add(createInciteBox(player)).row()
		
		val layoutTable = Table()
		layoutTable.add(demandLayout).align(Align.top)
		layoutTable.add(sentItemsLayout).align(Align.top)
		layoutTable.add(offerLayout).align(Align.top)
				
		getContentTable().add(headerLabel).row()
		getContentTable().add(layoutTable).row()
	}

	fun createGoldBox(): Table {
		val label = Label(Messages.msg("tradeItem.gold"), skin)
		val goldAmountTextField = TextField("0", skin)
		val addButton = TextButton(Messages.msg("negotiationDialog.add"), skin)
		
		goldAmountTextField.setTextFieldFilter(TextFieldFilter.DigitsOnlyFilter())
		goldAmountTextField.setTextFieldListener( object : TextFieldListener {
			override fun keyTyped(textField: TextField, c: Char) {
				if (goldAmountTextField.getText().equals("0")) {
					addButton.setDisabled(true)
				} else {
					addButton.setDisabled(false)
				}
			}
		})
		addButton.setDisabled(true)

		val box = Table()
		box.defaults()
			.padLeft(20f)
			.padRight(20f)
		box.add(label).align(Align.left).padTop(20f).row()
		box.add(goldAmountTextField).row()
		box.add(addButton).expandX().fillX().padBottom(20f).row()

		box.background = FrameWithCornersDrawableSkin(GameResources.instance)
		return box
	}

	fun createColonyBox(player : Player) : Table {
		val label = Label(Messages.msg("tradeItem.colony"), skin)
		
		var items = Array<ColonySelectItem>(player.settlements.size())
		player.settlements.entities()
			.forEach {
				items.add(ColonySelectItem(it.getColony()))
			}
		val colonySelectBox = SelectBox<ColonySelectItem>(skin)
		colonySelectBox.setItems(items)
		
		val addButton = TextButton(Messages.msg("negotiationDialog.add"), skin)
		if (items.size == 0) {
			addButton.setDisabled(true)
		}
		
		val box = Table()
		box.defaults()
			.padLeft(20f)
			.padRight(20f)
		box.add(label).align(Align.left).padTop(20f).row()
		box.add(colonySelectBox).expandX().fillX().row()
		box.add(addButton).expandX().fillX().padBottom(20f).row()
		
		box.background = FrameWithCornersDrawableSkin(GameResources.instance)
		return box
	}
	
	fun createStanceBox(player : Player) : Table {
		val label = Label(Messages.msg("tradeItem.stance"), skin)
		val addButton = TextButton(Messages.msg("negotiationDialog.add"), skin)

		var items = Array<StanceSelectItem>()
		items.add(StanceSelectItem(Stance.ALLIANCE))
		items.add(StanceSelectItem(Stance.CEASE_FIRE))
		items.add(StanceSelectItem(Stance.PEACE))
		
		val stanceSelectBox = SelectBox<StanceSelectItem>(skin)
		stanceSelectBox.setItems(items)

		val box = Table()
		box.defaults()
			.padLeft(20f)
			.padRight(20f)
		box.add(label).align(Align.left).padTop(20f).row()
		box.add(stanceSelectBox).fillX().expandX().row()
		box.add(addButton).expandX().fillX().padBottom(20f).row()
		
		box.background = FrameWithCornersDrawableSkin(GameResources.instance)
		return box
	}
	
	fun createInciteBox(player : Player) : Table {
		val label = Label(Messages.msg("tradeItem.incite"), skin)
		val addButton = TextButton(Messages.msg("negotiationDialog.add"), skin)
		
		var items = Array<PlayerSelectItem>(game.players.size())
		game.players.entities()
			.filter { it.isLive }
			.filter { player.hasContacted(it) }
			.filter { player.notEqualsId(it) }
			.forEach { items.add(PlayerSelectItem(it)) }
		items.sort(selectBoxPlayerComparator)

		if (items.size == 0) {
			addButton.setDisabled(true)
		}
		val playerSelectBox = SelectBox<PlayerSelectItem>(skin)
		playerSelectBox.setItems(items)
				
		val box = Table()
		box.defaults()
			.padLeft(20f)
			.padRight(20f)
		box.add(label).align(Align.left).padTop(20f).row()
		box.add(playerSelectBox).fillX().expandX().row()
		box.add(addButton).expandX().fillX().padBottom(20f).row()
		
		box.background = FrameWithCornersDrawableSkin(GameResources.instance)
		return box
	}
	
	override fun init(shapeRenderer : ShapeRenderer) {
	}
	
	override fun show(stage : Stage) {
		super.show(stage)
		resetPositionToCenter()
	}
	
}