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

class DiplomacyContactDialog(
	val player : Player,
	val contactPlayer : Player
)
	: ModalDialog<DiplomacyContactDialog>()
{

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
		val offerLabel = Label(offerLabelStr, skin)
		
		getContentTable().add(headerLabel).row()
		getContentTable().add(demandLabel).row()
		getContentTable().add(offerLabel).row()

		
		val goldBox = createGoldBox()

		getContentTable().add(goldBox).row()		  
	}

	fun createGoldBox(): Table {
		val goldLabel = Label(Messages.msg("tradeItem.gold"), skin)
		val goldAmountTextField = TextField("0", skin)
		goldAmountTextField.setTextFieldFilter(TextFieldFilter.DigitsOnlyFilter())
		val addButton = TextButton(Messages.msg("negotiationDialog.add"), skin)

		val goldBox = Table()
		goldBox.defaults().pad(10f, 10f, 0f, 10f)
		goldBox.add(goldLabel).align(Align.left).row()
		goldBox.add(goldAmountTextField).row()
		goldBox.add(addButton).expandX().fillX().row()

		return goldBox
	}

	override fun init(shapeRenderer : ShapeRenderer) {
	}
	
	override fun show(stage : Stage) {
		super.show(stage)
		resetPositionToCenter()
	}
	
}