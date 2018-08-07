package promitech.colonization.screen.map.hud

import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldFilter
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.player.Player
import net.sf.freecol.common.model.player.Stance
import promitech.colonization.GameResources
import promitech.colonization.screen.ui.FrameWithCornersDrawableSkin
import promitech.colonization.ui.ModalDialog
import promitech.colonization.ui.addListener
import promitech.colonization.ui.resources.Messages
import promitech.colonization.ui.resources.StringTemplate
import promitech.colonization.screen.map.diplomacy.TradeItem
import promitech.colonization.screen.map.diplomacy.GoldTradeItem
import promitech.colonization.screen.map.diplomacy.ColonyTradeItem
import promitech.colonization.screen.map.diplomacy.TradeType
import promitech.colonization.screen.map.diplomacy.ColonyBox

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
	
	private val offers = ArrayList<TradeItem>()
	private val demands = ArrayList<TradeItem>()
	private var tradeItemsLayout = Table()
	
	private val demandGoldBox : Table
	private val offerGoldBox : Table
	private val demandColonyBox : ColonyBox
	private val offerColonyBox : ColonyBox
	
	init {
		demandGoldBox = createGoldBox(TradeType.Demand, this::addTradeItem)
		offerGoldBox = createGoldBox(TradeType.Offer, this::addTradeItem)
		demandColonyBox = ColonyBox(contactPlayer, TradeType.Demand, skin, this::addTradeItem, demands)
		offerColonyBox = ColonyBox(player, TradeType.Offer, skin, this::addTradeItem, offers)
		
		createLayout()
		refreshSummaryBox()
	}

	private fun createLayout() {
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
		demandLayout.add(demandGoldBox).row()
		demandLayout.add(demandColonyBox.box).row()
		demandLayout.add(createInciteBox(contactPlayer)).row()
		
		val sentItemsLayout = Table()
		sentItemsLayout.defaults()
			.padTop(10f).padLeft(10f).padRight(10f)
			.align(Align.top or Align.left)
		sentItemsLayout.add(situationLabel).row()
		sentItemsLayout.add(tradeItemsLayout).row()
		
		
		val offerLayout = Table()
		offerLayout.defaults().fillX().expandX().padTop(10f).padLeft(10f).padRight(10f)
		offerLayout.add(offerLabel).row()
		offerLayout.add(offerGoldBox).row()
		offerLayout.add(offerColonyBox.box).row()
		offerLayout.add(createStanceBox(player)).row()
		offerLayout.add(createInciteBox(player)).row()
		
		val layoutTable = Table()
		layoutTable.defaults().align(Align.top or Align.left)
		layoutTable.add(demandLayout)
		layoutTable.add(sentItemsLayout)
		layoutTable.add(offerLayout)
				
		getContentTable().add(headerLabel).row()
		getContentTable().add(layoutTable).row()
	}
	
	private fun addTradeItem(item : TradeItem) {
		when (item.tradeType) {
			TradeType.Demand -> demands.add(item)
			TradeType.Offer -> offers.add(item)
		}
		refreshSummaryBox()
	}

	private fun refreshSummaryBox() {
		tradeItemsLayout.clear()
		
		val offerLabelStr = Messages.message(
			StringTemplate.template("negotiationDialog.offer")
				.addStringTemplate("%nation%", player.getNationName())
		)
		val demandLabelStr = Messages.message(
			StringTemplate.template("negotiationDialog.demand")
				.addStringTemplate("%nation%", player.getNationName())
		)
		val exchengeLabelStr = Messages.msg("negotiationDialog.exchange")
		
		tradeItemsLayout.defaults().align(Align.top or Align.left)
		if (offers.isEmpty()) {
			if (demands.isNotEmpty()) {
				tradeItemsLayout.add(Label(demandLabelStr, skin)).row()
				for (item : TradeItem in demands) {
					addTradeItemDescription(item)
				}
			}
		} else {
			tradeItemsLayout.add(Label(offerLabelStr, skin)).row()
			for (item : TradeItem in offers) {
				addTradeItemDescription(item)
			}
			if (demands.isNotEmpty()) {
				tradeItemsLayout.add(Label(exchengeLabelStr, skin)).row()
				for (item : TradeItem in demands) {
					addTradeItemDescription(item)
				}
			}
		}
	}
	
	private fun addTradeItemDescription(tradeItem : TradeItem) {
		val label = tradeItem.createLabel(skin)
		tradeItemsLayout.add(label).padLeft(20f)
		
		val deleteButton = TextButton(Messages.msg("list.remove"), skin)
		deleteButton.addListener { _, _ ->
			when (tradeItem.tradeType) {
				TradeType.Demand -> demands.remove(tradeItem)
				TradeType.Offer -> offers.remove(tradeItem)
			}
			when (tradeItem) {
				is ColonyTradeItem -> {
					demandColonyBox.refreshList()
					offerColonyBox.refreshList()
				}
			}
			refreshSummaryBox()
		}
		tradeItemsLayout.add(deleteButton).padLeft(20f).row()
	}
	
	private fun createGoldBox(tradeType : TradeType, addListener : (TradeItem) -> Unit) : Table {
		val goldAmountTextField = TextField("", skin)
		val addButton = TextButton(Messages.msg("negotiationDialog.add"), skin)
		
		goldAmountTextField.setTextFieldFilter(TextFieldFilter.DigitsOnlyFilter())
		goldAmountTextField.setTextFieldListener( object : TextFieldListener {
			override fun keyTyped(textField: TextField, c: Char) {
				if (goldAmountTextField.getText().equals("") || goldAmountTextField.getText().toInt() == 0) {
					addButton.setDisabled(true)
				} else {
					addButton.setDisabled(false)
				}
			}
		})
		addButton.setDisabled(true)
		
		addButton.addListener { _, _ ->
			val item = GoldTradeItem(goldAmountTextField.getText().toInt(), tradeType)
			addListener(item)
			goldAmountTextField.setText("")
			addButton.setDisabled(true)
		}

		val box = Table()
		box.defaults()
			.padLeft(20f)
			.padRight(20f)
		box.add(goldAmountTextField).padTop(20f).row()
		box.add(addButton).expandX().fillX().padBottom(20f).row()

		box.background = FrameWithCornersDrawableSkin(
			Messages.msg("tradeItem.gold"),
			skin.get(LabelStyle::class.java).font,				
			GameResources.instance
		)
		return box
	}

	fun createStanceBox(player : Player) : Table {
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
		box.add(stanceSelectBox).fillX().expandX().padTop(20f).row()
		box.add(addButton).expandX().fillX().padBottom(20f).row()
		
		box.background = FrameWithCornersDrawableSkin(
			Messages.msg("tradeItem.stance"),
			skin.get(LabelStyle::class.java).font,				
			GameResources.instance
		)
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