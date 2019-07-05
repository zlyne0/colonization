package promitech.colonization.screen

import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.IdGenerator
import net.sf.freecol.common.model.TradeRouteDefinition
import net.sf.freecol.common.model.player.Player
import promitech.colonization.toGdxArray
import promitech.colonization.ui.ModalDialog
import promitech.colonization.ui.ModalDialogSize
import promitech.colonization.ui.addListener
import promitech.colonization.ui.resources.Messages
import net.sf.freecol.common.model.TradeRoute
import promitech.colonization.screen.map.hud.GUIGameController
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Label


data class TradeRouteItem(val tradeRoute : TradeRouteDefinition) {
	override fun toString() : String {
		return tradeRoute.getName()
	}
}

class ColonySelectItem(var colony : Colony) {
	override fun toString() : String {
		return colony.getName()
	}
}

class TradeRouteListDialog(
	val shapeRenderer : ShapeRenderer,		
	val player : Player,
	val idGenerator : IdGenerator,
	val guiGameController : GUIGameController
) : ModalDialog<TradeRouteListDialog>(ModalDialogSize.width50(), ModalDialogSize.def())
{
	
	val layout = Table()
	val tradeRoutes = object : SelectBox<TradeRouteItem>(skin) {
		override fun getPrefWidth() : Float {
			return 200f
		}
	}
	val assignRouteButton = TextButton(Messages.msg("assignTradeRouteAction.name"), skin)
	val msgLabel = Label("", skin)
		
	init {
		layout.add(createTradeRoutesPanel()).row()
		getContentTable().add(layout).top().expand()
		
		val okButton = TextButton(Messages.msg("ok"), skin)
		okButton.addListener { ->
			hideWithFade()
		}
		getButtonTable().add(okButton).pad(10f).fillX().expandX()
		
		tradeRoutes.addListener(object : ChangeListener() {
			override fun changed(event: ChangeListener.ChangeEvent?, actor: Actor?) {
			    System.out.println("tradeRouteList change selected " + tradeRoutes.getSelected());
				determineTradeRouteAssignPossibility()
			}
		})
	}
	
	private fun determineTradeRouteAssignPossibility() {
		val tradeRouteItem = tradeRoutes.getSelected()
		if (tradeRouteItem == null) {
		    assignRouteButton.setDisabled(true)
			msgLabel.setText(Messages.msg("tradeRoutePanel.new.tooltip"))
			return 
		}
		
		val canBeAssignedMsg = tradeRouteItem.tradeRoute.canBeAssignedMsg(player)
		if (canBeAssignedMsg != null) {
			assignRouteButton.setDisabled(true)
			msgLabel.setText(Messages.message(canBeAssignedMsg))
			return 
		}
		assignRouteButton.setDisabled(!canAssignRouteToUnit())
		msgLabel.setText("")
	}

	private fun canAssignRouteToUnit() : Boolean {
		val activeUnit = guiGameController.getActiveUnit()
		return activeUnit != null && activeUnit.canCarryGoods()
	}
		
	private fun createTradeRoutesPanel() : Table {
		updateTradeRouteList()
		
		val createRouteButton = TextButton(Messages.msg("tradeRoutePanel.newRoute"), skin)
		createRouteButton.addListener { ->
			val routeDef = TradeRouteDefinition(idGenerator, "Trade route #" + player.tradeRoutes.size())
			player.tradeRoutes.add(routeDef)
			showTradeRouteDefinitionDialog(routeDef) 
		}
		
		val deleteRouteButton = TextButton(Messages.msg("tradeRoutePanel.deleteRoute"), skin)
		deleteRouteButton.addListener { ->
			val selectedRoute = tradeRoutes.getSelected()
			if (selectedRoute != null) {
				player.tradeRoutes.removeId(selectedRoute.tradeRoute)
				
				var items = tradeRoutes.getItems()
				items.removeValue(selectedRoute, true)
				tradeRoutes.setItems(items)
				
				if (!items.isEmpty()) {
					tradeRoutes.setSelected(items.first())
				}
			}
		}
		
		val editRouteButton = TextButton(Messages.msg("tradeRouteInputPanel.editRoute"), skin)
		editRouteButton.addListener { ->
			val selectedRoute = tradeRoutes.getSelected()
			if (selectedRoute != null) {
				showTradeRouteDefinitionDialog(selectedRoute.tradeRoute)
			}
		}
		
		assignRouteButton.addListener { ->
			val selectedRoute = tradeRoutes.getSelected()
			val activeUnit = guiGameController.getActiveUnit()
			if (selectedRoute != null && activeUnit != null) {
				activeUnit.setTradeRoute(TradeRoute(selectedRoute.tradeRoute.getId()))
				guiGameController.nextActiveUnit()
				hideWithoutFade()
			}
		}
		determineTradeRouteAssignPossibility()
		
		var buttons = Table()
		buttons.defaults()
			.pad(20f, 20f, 0f, 20f)
			.fillX()
		buttons.add(createRouteButton).row()
		buttons.add(editRouteButton).row()
		buttons.add(deleteRouteButton).row()
		buttons.add(assignRouteButton)
		
		val tradeRoutesPanel = Table()
		tradeRoutesPanel.defaults()
			.pad(20f, 20f, 20f, 20f)
		tradeRoutesPanel.add(tradeRoutes)
		tradeRoutesPanel.add(buttons).row()
		tradeRoutesPanel.add(msgLabel).colspan(2)
		
		return tradeRoutesPanel
	}

	private fun showTradeRouteDefinitionDialog(routeDef : TradeRouteDefinition) {
		val dialog = TradeRouteDialog(shapeRenderer, player, routeDef, idGenerator)
		dialog.addOnCloseListener { ->
			updateTradeRouteList()
			determineTradeRouteAssignPossibility()
		}
		showDialog(dialog)
	}

	private fun updateTradeRouteList() {
		tradeRoutes.setItems(player.tradeRoutes.entities()
			.map { TradeRouteItem(it) }
			.toGdxArray(player.tradeRoutes.size())
		)
	}
}