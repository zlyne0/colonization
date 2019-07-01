package promitech.colonization.screen

import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.IdGenerator
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.TradeRouteDefinition
import net.sf.freecol.common.model.TradeRouteStop
import net.sf.freecol.common.model.player.Player
import promitech.colonization.GameResources
import promitech.colonization.screen.map.diplomacy.ColonySelectItem
import promitech.colonization.toGdxArray
import promitech.colonization.ui.ModalDialog
import promitech.colonization.ui.ModalDialogSize
import promitech.colonization.ui.STable
import promitech.colonization.ui.STableSelectListener
import promitech.colonization.ui.addListener
import promitech.colonization.ui.resources.Messages
import net.sf.freecol.common.model.specification.GoodsType

class RouteStopListItem(val routeStop : TradeRouteStop) {
	val goodsPanel = HorizontalGroup()

	init {
		for (goodsType in ArrayList(routeStop.goodsType)) {
			addGoodsImg(goodsType)
		}
	}
	
	private fun addGoodsImg(goodsType : GoodsType) {
		val goodsImg = ImageButton(TextureRegionDrawable(GameResources.instance.goodsImage(goodsType).texture))
		goodsImg.addListener { ->
			goodsImg.getParent().removeActor(goodsImg)
			routeStop.removeGoodsType(goodsType)
		}
		goodsPanel.addActor(goodsImg)
	}
	
	fun addGoods(goodsType : GoodsType) {
		routeStop.addGoodsType(goodsType)
		addGoodsImg(goodsType)
	}
}

class TradeRouteDialog(
	val shapeRenderer : ShapeRenderer,
	val player : Player,
	val routeDef : TradeRouteDefinition,
	val idGenerator : IdGenerator
) : ModalDialog<TradeRouteDialog>(ModalDialogSize.width50(), ModalDialogSize.height75())
{
	private val stopsItemAlligment = intArrayOf(Align.left, Align.left, Align.left)
	private val tradeRouteNameField = TextField(routeDef.getName(), skin)
	
	val tradeRouteStops = STable(shapeRenderer)
	val layout = Table()
	var maxCargoSlots = 0

	init {
		for (unitType in Specification.instance.unitTypes.entities()) {
			if (unitType.getSpace() > maxCargoSlots) {
				maxCargoSlots = unitType.getSpace() 
			}
		}
		
		layout.add(createTradeRouteDefinitionPanel()).top().expandX().fillX().row()
		layout.add(goodsTypesPanel()).expandX().fillX().row()
		layout.add(routeStopsPanel()).expand().fill()
		
		getContentTable().add(layout)
			.pad(20f)
			.top().expand().fill()
				
		val okButton = TextButton(Messages.msg("ok"), skin)
		okButton.addListener { ->
			routeDef.setName(tradeRouteNameField.getText())
			hideWithFade()
		}
		getButtonTable().add(okButton).pad(10f).fillX().expandX()
	}
	
	private fun createTradeRouteDefinitionPanel() : Table {
		val nameLabel = Label(Messages.msg("tradeRouteInputPanel.nameLabel"), skin)
		
		val destinationLabel = Label(Messages.msg("tradeRouteInputPanel.destinationLabel"), skin) 
		val colonies = player.settlements.entities()
			.map { ColonySelectItem(it.getColony()) }
			.toGdxArray(player.settlements.size())
		val allStopsSelectBox = SelectBox<ColonySelectItem>(skin)
		allStopsSelectBox.setItems(colonies)
		
		var addStopButton = TextButton(Messages.msg("tradeRouteInputPanel.addStop"), skin)
		addStopButton.addListener { ->
			if (!allStopsSelectBox.getSelection().isEmpty()) {
				val selectedColony = allStopsSelectBox.getSelection().first()
				onAddRouteStop(selectedColony.colony)
			}
		}
		if (player.settlements.isEmpty()) {
			addStopButton.setDisabled(true)
		}

		val panel = Table()
		panel.defaults().padTop(10f)
		
		panel.add(nameLabel)
			.align(Align.right)
			.padRight(10f)
		panel.add(tradeRouteNameField)
			.expandX().fillX()
			.align(Align.left)
			.row()
		panel.add(destinationLabel)
			.align(Align.right)
			.padRight(10f)
		panel.add(allStopsSelectBox)
			.expandX().fillX()
			.align(Align.left)
		panel.add(addStopButton)
			.align(Align.left)
			.padLeft(10f)
		return panel		
	}

	private fun onAddRouteStop(colony : Colony) {
		val routeStop = routeDef.addRouteStop(colony)
		addRouteStopListElement(routeStop)		
	} 
	
	private fun routeStopsPanel() : Table {
		for (routeStop in routeDef.getTradeRouteStops()) {
			addRouteStopListElement(routeStop)
		}

		var scrollPane = ScrollPane(tradeRouteStops, skin)		
		scrollPane.setFlickScroll(false)
		scrollPane.setScrollingDisabled(true, false)
		scrollPane.setForceScroll(false, false)
		scrollPane.setFadeScrollBars(false)
		scrollPane.setScrollBarPositions(false, true)
		
		val panel = Table()
		panel.add(scrollPane)
			.top().left()
			.fillX().expand()
			.pad(10f, 10f, 0f, 10f)
		panel.row()
		
		val removeStop = TextButton(Messages.msg("tradeRouteInputPanel.removeStop"), skin)
		removeStop.setDisabled(true)
		removeStop.addListener { ->
			val selectedStop = tradeRouteStops.getSelectedPayload()
			if (selectedStop != null && selectedStop is RouteStopListItem) {
				routeDef.removeStop(selectedStop.routeStop)
				tradeRouteStops.removeSelectedItems()
				removeStop.setDisabled(true)
			}
		}
		
		panel.add(removeStop)
			.right()
			.padLeft(10f).padRight(10f)
		
		tradeRouteStops.addSingleClickSelectListener(object : STableSelectListener {
			override fun onSelect(payload: Any?) {
				removeStop.setDisabled(false)
			}
		})
		
		return panel
	}
	
	private fun addRouteStopListElement(routeStop : TradeRouteStop) {
		val colony = player.settlements.getById(routeStop.getTradeLocationId()).asColony()
		val stopImg = Image(GameResources.instance.getFrame(colony.getImageKey()).texture)
		stopImg.setAlign(Align.left)
		val stopNameLabel = Label(colony.getName(), skin)
		
		val stopItem = RouteStopListItem(routeStop)
		
		tradeRouteStops.addRow(
			stopItem,
			stopsItemAlligment,
			stopImg, stopNameLabel, stopItem.goodsPanel
		)
	}
	
	private fun goodsTypesPanel() : Table {
		var goodsTypesBox = Table()
		goodsTypesBox.defaults().pad(10f, 20f, 10f, 20f)
		
		for (goodsType in Specification.instance.goodsTypes.entities()) {
			if (!goodsType.isStorable()) {
				continue
			}
			var goodsTypeImgButton = ImageButton(TextureRegionDrawable(GameResources.instance.goodsImage(goodsType).texture))
			goodsTypeImgButton.addListener { ->
				val item = tradeRouteStops.getSelectedPayload()
				if (item != null) {
					addGoodsToStop(item as RouteStopListItem, goodsType) 
				} else {
					println("tradeRoute[${routeDef.getId()}] no route stop selected for add goods $goodsType")
				}
			}
			goodsTypesBox.add(goodsTypeImgButton)
			if (goodsTypesBox.getCells().size % 8 == 0) {
				goodsTypesBox.row()
			}
		}
		
		return goodsTypesBox
	}
	
	private fun addGoodsToStop(selectedStopItem : RouteStopListItem, goodsType : GoodsType) {
		if (selectedStopItem.routeStop.getGoodsType().size >= maxCargoSlots) {
			println("tradeRoute[${routeDef.getId()}] stop ${selectedStopItem.routeStop.getTradeLocationId()} full of cargo slots")
			return
		} 
		println("tradeRoute[${routeDef.getId()}] add $goodsType to stop ${selectedStopItem.routeStop.getTradeLocationId()}")
		selectedStopItem.addGoods(goodsType)
	}
	
}