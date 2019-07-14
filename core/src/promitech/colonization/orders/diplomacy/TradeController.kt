package promitech.colonization.orders.diplomacy

import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.Map
import promitech.colonization.GameResources
import promitech.colonization.orders.move.MoveContext
import promitech.colonization.screen.map.hud.GUIGameController
import promitech.colonization.ui.ClosableDialog
import promitech.colonization.ui.ModalDialogSize
import promitech.colonization.ui.addListener
import promitech.colonization.ui.resources.Messages
import promitech.colonization.ui.resources.StringTemplate
import net.sf.freecol.common.model.specification.GoodsType
import net.sf.freecol.common.model.specification.AbstractGoods

class TradeDialog(val tradeSession : TradeSession)
	: ClosableDialog<TradeDialog>(ModalDialogSize.width50(), ModalDialogSize.def())
{
	
	val layout = Table()
	val messages = mutableListOf<String>()
	
	init {
		getContentTable().add(layout).expandX().fillX().row()
	}

	fun sellGoods(goodsTypeId : String, amount : Int) {
		layout.clear()
		welcomeLabel()
		
		val tradeGoodsType = Specification.instance.goodsTypes.getById(goodsTypeId)
		var goodsOfferPrice = tradeSession.sellOffer(tradeGoodsType, amount)
		
		val sellLabel = Label(sellGoodsLabelStr(tradeGoodsType, amount, goodsOfferPrice), skin)
		val acceptSellOffer = TextButton(Messages.msg("sell.takeOffer"), skin)
		acceptSellOffer.addListener { ->
			tradeSession.acceptSellOfferToIndianSettlement(tradeGoodsType, amount, goodsOfferPrice)
			showTradeChoices()
		}
		
		val askForMoreGold = TextButton(Messages.msg("sell.moreGold"), skin)
		askForMoreGold.addListener { ->
			goodsOfferPrice = tradeSession.haggleSellOffer(tradeGoodsType, amount, goodsOfferPrice)
			if (goodsOfferPrice <= 0) {
				addHaggleResultMessage(goodsOfferPrice)
				showTradeChoices()
			} else {
				sellLabel.setText(sellGoodsLabelStr(tradeGoodsType, amount, goodsOfferPrice))
			}
		}
		
		val offerGift = TextButton(
			StringTemplate.template("sell.gift")
				.addName("%goods%", tradeGoodsType)
				.eval(),
			skin
		).addListener { ->
			deliverGoodsToIndianSettlement(tradeGoodsType, amount)
		}
		
		val cancel = TextButton(Messages.msg("tradeProposition.cancel"), skin)
		cancel.addListener( ::showTradeChoices )
		
		layout.add(sellLabel).fillX().padTop(10f).row()
		layout.add(acceptSellOffer).fillX().padTop(10f).row()
		layout.add(askForMoreGold).fillX().padTop(10f).row()
		layout.add(offerGift).fillX().padTop(10f).row()
		layout.add(cancel).fillX().padTop(10f).row()
		
		dialog.pack()
	}

	private fun deliverGoodsToIndianSettlement(goodsType : GoodsType, amount : Int) {
		tradeSession.deliverGoodsToIndianSettlement(goodsType, amount);
		val msg = StringTemplate.template("model.unit.gift")
			.addStringTemplate("%player%", tradeSession.traderNationName())
			.addAmount("%amount%", amount)
			.addName("%type%", goodsType)
			.add("%settlement%", tradeSession.indianSettlement.getName())
			.eval()
		messages.add(msg)
		showTradeChoices()
	}
	
	private fun addHaggleResultMessage(price : Int) {
		val str = when (price) {
			TradeSession.NO_TRADE -> StringTemplate.template("trade.noTrade")
				.add("%settlement%", tradeSession.indianSettlement.getName())
				.eval()
			TradeSession.NO_TRADE_HAGGLE -> Messages.msg("trade.noTradeHaggle")
			TradeSession.NO_TRADE_HOSTILE -> Messages.msg("trade.noTradeHostile")
			else -> "" 
		}
		if (str.isNotBlank()) {
			messages.add(str)
		}
	}
	
	fun sellGoodsLabelStr(tradeGoodsType : GoodsType, amount : Int, price : Int) : String {
		val goodsAmountStrLabel = StringTemplate.template("model.goods.goodsAmount")
			.addAmount("%amount%", amount)
			.addName("%goods%", tradeGoodsType)
		return StringTemplate.template("sell.text")
			.addStringTemplate("%nation%", tradeSession.indianSettlement.getOwner().getNationName())
			.addStringTemplate("%goods%", goodsAmountStrLabel)
			.addAmount("%gold%", price)
			.eval()
	}
		
	private fun showSellChoices() {
		layout.clear()
		welcomeLabel()
		
		val sellLabel = Label(Messages.msg("sellProposition.text"), skin)
		layout.add(sellLabel).fillX().padTop(10f).row()
		
		for (goods in tradeSession.goodsToSell()) {
			if (goods.value == 0) {
				continue
			}
			val goodsStrLabel = StringTemplate.template("model.goods.goodsAmount")
	            .addAmount("%amount%", goods.value)
	            .addName("%goods%", goods.key)
	            .eval()
			val goodsButton = TextButton(goodsStrLabel, skin)
			var ag = AbstractGoods(goods.key, goods.value)
			goodsButton.addListener { ->
				sellGoods(ag.typeId, ag.quantity)
			}
			layout.add(goodsButton).fillX().padTop(10f).row()
		}
		
		val nothingButton = TextButton(Messages.msg("sellProposition.nothing"), skin)
		nothingButton.addListener { ->
			showTradeChoices()
		}
		layout.add(nothingButton).fillX().padTop(10f).row()
		
		dialog.pack()
	}
	
	fun showDeliverGift() {
		layout.clear()
		welcomeLabel()

		val sellLabel = Label(Messages.msg("gift.text"), skin)
		layout.add(sellLabel).fillX().padTop(10f).row()

		for (goods in tradeSession.goodsToSell()) {
			if (goods.value == 0) {
				continue
			}
			val goodsStrLabel = StringTemplate.template("model.goods.goodsAmount")
	            .addAmount("%amount%", goods.value)
	            .addName("%goods%", goods.key)
	            .eval()
			val goodsButton = TextButton(goodsStrLabel, skin)
			var ag = AbstractGoods(goods.key, goods.value)
			goodsButton.addListener { ->
				val goodsType = Specification.instance.goodsTypes.getById(ag.typeId)
				deliverGoodsToIndianSettlement(goodsType, ag.quantity)
			}
			layout.add(goodsButton).fillX().padTop(10f).row()
		}
				
		val cancel = TextButton(Messages.msg("tradeProposition.cancel"), skin)
		cancel.addListener( ::showTradeChoices )
		layout.add(cancel).fillX().padTop(10f).row()
		
		dialog.pack()
	}
	
	private fun showBuyChoices() {
		layout.clear()
		welcomeLabel()

		val sellLabel = Label(Messages.msg("buyProposition.text"), skin)
		layout.add(sellLabel).fillX().padTop(10f).row()
		
		for (goods in tradeSession.goodsToBuy()) {
			if (goods.getQuantity() == 0) {
				continue
			}
			val goodsStrLabel = StringTemplate.template("model.goods.goodsAmount")
	            .addAmount("%amount%", goods.getQuantity())
	            .addName("%goods%", goods.getTypeId())
	            .eval()
			val goodsButton = TextButton(goodsStrLabel, skin)
			goodsButton.addListener { ->
				val goodsType = Specification.instance.goodsTypes.getById(goods.getTypeId())
				buyGoodsFromSettlement(goodsType, goods.getQuantity())
			}
			layout.add(goodsButton).fillX().padTop(10f).row()
		}
		
		val cancel = TextButton(Messages.msg("buyProposition.nothing"), skin)
		cancel.addListener( ::showTradeChoices )
		layout.add(cancel).fillX().padTop(10f).row()
				
		dialog.pack()
	}
	
	private fun buyGoodsFromSettlement(goodsType : GoodsType, amount : Int) {
		layout.clear()
		welcomeLabel()

		var buyOfferPrice = tradeSession.buyOfferPrice(goodsType, amount)
		
		val buyPriceLabel = Label(buyOfferPriceLabelStr(goodsType, amount, buyOfferPrice), skin)
		layout.add(buyPriceLabel).fillX().padTop(10f).row()	
		
		val acceptSellOffer = TextButton(Messages.msg("buy.takeOffer"), skin)
		acceptSellOffer.setDisabled(tradeSession.tradeUnitHasNotGold(buyOfferPrice))
		acceptSellOffer.addListener { ->
			tradeSession.acceptBuyOffer(goodsType, amount, buyOfferPrice)
			showTradeChoices()
		}
		layout.add(acceptSellOffer).fillX().padTop(10f).row()
		
		val askForLowerPrice = TextButton(Messages.msg("buy.moreGold"), skin)
		askForLowerPrice.addListener { ->
			buyOfferPrice = tradeSession.haggleBuyOfferPrice(goodsType, amount, buyOfferPrice)
			if (buyOfferPrice <= 0) {
				addHaggleResultMessage(buyOfferPrice)
				showTradeChoices()
			} else {
				acceptSellOffer.setDisabled(tradeSession.tradeUnitHasNotGold(buyOfferPrice))
				buyPriceLabel.setText(buyOfferPriceLabelStr(goodsType, amount, buyOfferPrice))
			}
		}
		layout.add(askForLowerPrice).fillX().padTop(10f).row()
		
		val cancel = TextButton(Messages.msg("tradeProposition.cancel"), skin)
		cancel.addListener( ::showTradeChoices )
		layout.add(cancel).fillX().padTop(10f).row()
				
		dialog.pack()
	}

	private fun buyOfferPriceLabelStr(goodsType : GoodsType, amount : Int, price : Int) : String {
		val goodsAmountStrLabel = StringTemplate.template("model.goods.goodsAmount")
			.addAmount("%amount%", amount)
			.addName("%goods%", goodsType)
		val buyPriceLabelStr = StringTemplate.template("buy.text")
			.addStringTemplate("%nation%", tradeSession.indianSettlement.getOwner().getNationName())
			.addStringTemplate("%goods%", goodsAmountStrLabel)
			.addAmount("%gold%", price)
			.eval()
		return buyPriceLabelStr
	}
		
	fun showTradeChoices() {
		tradeSession.updateSettlementProduction()
		layout.clear()
		welcomeLabel()
		
		if (tradeSession.isCanBuy()) {
			val buyButton = TextButton(Messages.msg("tradeProposition.toBuy"), skin)
			buyButton.addListener { ->
				showBuyChoices()
			}
			layout.add(buyButton).fillX().padTop(10f).row()
		}
		if (tradeSession.isCanSell()) {
			val sellButton = TextButton(Messages.msg("tradeProposition.toSell"), skin)
			sellButton.addListener { ->
				showSellChoices()
			}
			layout.add(sellButton).fillX().padTop(10f).row()
		}
		if (tradeSession.isCanGift()) {
			val giftButton = TextButton(Messages.msg("tradeProposition.toGift"), skin)
			giftButton.addListener { ->
				showDeliverGift()
			}
			layout.add(giftButton).fillX().padTop(10f).row()
		}
		
		val cancelButton = TextButton(Messages.msg("tradeProposition.cancel"), skin)
		cancelButton.addListener { -> 
			hideWithFade()
		}
		layout.add(cancelButton).fillX().padTop(10f).row()
		
		for (msg in messages) {
			layout.add(Label(msg, skin)).fillX().padTop(10f).row()
		}
		messages.clear()
		
		dialog.pack()
	}
	
	private fun welcomeLabel() {
		val img = Image(TextureRegionDrawable(
			GameResources.instance.getCenterAdjustFrameTexture(tradeSession.indianSettlement.getImageKey()).texture
		), Scaling.none, Align.center)
		
		val welcomeLabel = StringTemplate.template("tradeProposition.welcome")
			.addStringTemplate("%nation%", tradeSession.indianSettlement.getOwner().getNationName())
			.add("%settlement%", tradeSession.indianSettlement.getName())
			.eval()
		
		var l1 = HorizontalGroup()
		l1.addActor(img)
		l1.addActor(Label(welcomeLabel, skin))
		
		layout.add(l1).row()
	}
	
}

class TradeController(val map : Map, val guiGameController : GUIGameController, val moveContext : MoveContext) {
	fun trade() {
		val tradeSession = TradeSession(map, moveContext.destTile.settlement.asIndianSettlement(), moveContext.unit)
				
		val tradeDialog = TradeDialog(tradeSession)
		tradeDialog.showTradeChoices()
		tradeDialog.addOnCloseListener { ->
			guiGameController.nextActiveUnitWhenNoMovePointsAsGdxPostRunnable()
		}
		guiGameController.showDialog(tradeDialog)
	}
}