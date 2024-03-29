package promitech.colonization.screen.map.hud

import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.utils.Align
import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.player.Player
import promitech.colonization.orders.move.HumanPlayerInteractionSemaphore
import promitech.colonization.screen.map.Map
import promitech.colonization.screen.map.diplomacy.ColonyBox
import promitech.colonization.screen.map.diplomacy.ColonyTradeItem
import promitech.colonization.screen.map.diplomacy.DiplomacyAgreement
import promitech.colonization.screen.map.diplomacy.DiplomacyAgreement.TradeStatus
import promitech.colonization.screen.map.diplomacy.GoldBox
import promitech.colonization.screen.map.diplomacy.InciteBox
import promitech.colonization.screen.map.diplomacy.InciteTradeItem
import promitech.colonization.screen.map.diplomacy.StanceBox
import promitech.colonization.screen.map.diplomacy.StanceTradeItem
import promitech.colonization.screen.map.diplomacy.TradeItem
import promitech.colonization.ui.ModalDialog
import promitech.colonization.ui.ModalDialogSize
import promitech.colonization.ui.addListener
import promitech.colonization.ui.kAddOnCloseListener
import promitech.colonization.ui.resources.Messages
import promitech.colonization.ui.resources.StringTemplate

class DiplomacyContactDialog(
	val screenMap : Map,
	val game : Game,
	val player : Player,
	val contactPlayer : Player,
	val humanPlayerInteractionSemaphore : HumanPlayerInteractionSemaphore = HumanPlayerInteractionSemaphore()
)
	: ModalDialog<DiplomacyContactDialog>(ModalDialogSize.width90(), ModalDialogSize.def())
{
	
	private val diplomacyAgreement : DiplomacyAgreement
	private var tradeItemsLayout = Table()
	
	private val demandGoldBox : GoldBox
	private val offerGoldBox : GoldBox
	private val demandColonyBox : ColonyBox
	private val offerColonyBox : ColonyBox
	
	private val demandInciteBox : InciteBox
	private val offerInciteBox : InciteBox
	
	private val offerStanceBox : StanceBox
	
	private val sendButton : TextButton
	
	init {
		diplomacyAgreement = DiplomacyAgreement(game, player, contactPlayer)
		
		demandGoldBox = GoldBox(contactPlayer, player, skin, this::onAddTradeItem)
		demandColonyBox = ColonyBox(contactPlayer, player, skin, this::onAddTradeItem, diplomacyAgreement.demands)
		demandInciteBox = InciteBox(game, contactPlayer, player, skin, this::onAddTradeItem, diplomacyAgreement.demands)
		
		offerGoldBox = GoldBox(player, contactPlayer, skin, this::onAddTradeItem)
		offerColonyBox = ColonyBox(player, contactPlayer, skin, this::onAddTradeItem, diplomacyAgreement.offers)
		offerInciteBox = InciteBox(game, player, contactPlayer, skin, this::onAddTradeItem, diplomacyAgreement.offers)
		offerStanceBox = StanceBox(player, contactPlayer, skin, this::onAddTradeItem, diplomacyAgreement.offers)
		
		createLayout()
		
		val cancelButton = TextButton(Messages.msg("negotiationDialog.cancel"), skin)
		cancelButton.addListener { _, _ ->
			hideWithFade()
		}
		
		sendButton = TextButton("", skin)
		sendButton.addListener { _, _ ->
			diplomacyAgreement.acceptTrade()
			screenMap.resetMapModel()
			hideWithFade()
		}
		tradeStatusRejectedButtonMsg()

		getButtonTable().add(cancelButton).pad(10f).fillX().expandX()
        getButtonTable().add(sendButton).pad(10f).fillX().expandX()
		
		refreshSummaryBox()
		
		kAddOnCloseListener {
		    humanPlayerInteractionSemaphore.release()
		}
	}
	
	private fun createLayout() {
		val headerLabel = Label(Messages.msg("negotiationDialog.title.diplomatic"), skin)

		val demandLabelStr = Messages.message(
			StringTemplate.template("negotiationDialog.demand")
				.addStringTemplate("%nation%", player.getNationName())
				.addStringTemplate("%otherNation%", contactPlayer.getNationName())
		)
		val offerLabelStr = Messages.message(
			StringTemplate.template("negotiationDialog.offer")
				.addStringTemplate("%nation%", player.getNationName())
				.addStringTemplate("%otherNation%", contactPlayer.getNationName())
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
		demandLayout.add(demandGoldBox.box).row()
		demandLayout.add(demandColonyBox.box).row()
		demandLayout.add(demandInciteBox.box).row()
		
		val sentItemsLayout = Table()
		sentItemsLayout.defaults()
			.padTop(10f).padLeft(10f).padRight(10f)
			.align(Align.top or Align.left)
		sentItemsLayout.add(situationLabel).row()
		sentItemsLayout.add(tradeItemsLayout).row()
		
		var tradeItemsScrollPane = ScrollPane(sentItemsLayout, skin)
        tradeItemsScrollPane.setFlickScroll(false)
        tradeItemsScrollPane.setScrollingDisabled(true, false)
        tradeItemsScrollPane.setForceScroll(false, false)
        tradeItemsScrollPane.setFadeScrollBars(false)
        tradeItemsScrollPane.setOverscroll(true, true)
        tradeItemsScrollPane.setScrollBarPositions(false, true)
		
		val offerLayout = Table()
		offerLayout.defaults().fillX().expandX().padTop(10f).padLeft(10f).padRight(10f)
		offerLayout.add(offerLabel).row()
		offerLayout.add(offerGoldBox.box).row()
		offerLayout.add(offerColonyBox.box).row()
		offerLayout.add(offerStanceBox.box).row()
		offerLayout.add(offerInciteBox.box).row()
		
		val layoutTable = Table()
		layoutTable.defaults().align(Align.top or Align.left)
		layoutTable.add(demandLayout)
		layoutTable.add(tradeItemsScrollPane)
		layoutTable.add(offerLayout)
				
		getContentTable().add(headerLabel).row()
		getContentTable().add(layoutTable).expandX().fillX().row()
	}
	
	fun addPeaceOffer() : DiplomacyContactDialog {
		offerStanceBox.addPeace()
		return this
	}
	
	fun disableColonyTrade() : DiplomacyContactDialog {
		offerColonyBox.disable()
		demandColonyBox.disable()
		return this
	}
	
	fun addDemandTributeAggrement(gold : Int) : DiplomacyContactDialog {
		offerStanceBox.addPeace()
		demandGoldBox.addGold(gold)
		return this
	}
	
	private fun onAddTradeItem(item : TradeItem) {
		diplomacyAgreement.add(item)
		refreshSummaryBox()
		refreshSendButton()
	}
	
	private fun refreshSendButton() {
		val tradeStatus = diplomacyAgreement.calculate()
		when (tradeStatus) {
			TradeStatus.ACCEPT -> {
				tradeStatusAcceptedButtonMsg()
			}
			TradeStatus.REJECT -> {
				tradeStatusRejectedButtonMsg()
			}
		}
	}

	private fun tradeStatusAcceptedButtonMsg() {
		var buttonText = Messages.message(
			StringTemplate.template("negotiationDialog.offerAccepted")
				.addStringTemplate("%nation%", contactPlayer.getNationName())
		)
		sendButton.setText(buttonText)
		sendButton.setDisabled(false)
	}
	
	private fun tradeStatusRejectedButtonMsg() {
		var buttonText = Messages.message(
			StringTemplate.template("negotiationDialog.offerRejected")
				.addStringTemplate("%nation%", contactPlayer.getNationName())
		)
		sendButton.setText(buttonText)
		sendButton.setDisabled(true)
	}
	
	private fun refreshSummaryBox() {
		tradeItemsLayout.clear()
		
		val offerLabelStr = Messages.message(
			StringTemplate.template("negotiationDialog.offer")
				.addStringTemplate("%nation%", player.getNationName())
				.addStringTemplate("%otherNation%", contactPlayer.getNationName())
		)
		val demandLabelStr = Messages.message(
			StringTemplate.template("negotiationDialog.demand")
				.addStringTemplate("%nation%", player.getNationName())
				.addStringTemplate("%otherNation%", contactPlayer.getNationName())
		)
		val exchangeLabelStr = Messages.msg("negotiationDialog.exchange")
		
		tradeItemsLayout.defaults().align(Align.top or Align.left)
		if (diplomacyAgreement.offers.isEmpty()) {
			if (diplomacyAgreement.demands.isNotEmpty()) {
				tradeItemsLayout.add(Label(demandLabelStr, skin)).row()
				for (item : TradeItem in diplomacyAgreement.demands) {
					addTradeItemDescription(item)
				}
			}
		} else {
			tradeItemsLayout.add(Label(offerLabelStr, skin)).row()
			for (item : TradeItem in diplomacyAgreement.offers) {
				addTradeItemDescription(item)
			}
			if (diplomacyAgreement.demands.isNotEmpty()) {
				tradeItemsLayout.add(Label(exchangeLabelStr, skin)).row()
				for (item : TradeItem in diplomacyAgreement.demands) {
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
			diplomacyAgreement.remove(tradeItem) 
			when (tradeItem) {
				is ColonyTradeItem -> {
					demandColonyBox.refreshList()
					offerColonyBox.refreshList()
				}
				is InciteTradeItem -> {
					demandInciteBox.refreshList()
					offerInciteBox.refreshList()
				}
				is StanceTradeItem -> {
					offerStanceBox.refresh()
				}
			}
			refreshSummaryBox()
			refreshSendButton()
		}
		tradeItemsLayout.add(deleteButton).padLeft(20f).row()
	}
	
	override fun init(shapeRenderer : ShapeRenderer) {
	}
	
	override fun show(stage : Stage) {
		super.show(stage)
		resetPositionToCenter()
	}
	
}