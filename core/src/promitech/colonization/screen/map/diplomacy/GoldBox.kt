package promitech.colonization.screen.map.diplomacy

import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldFilter
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener
import net.sf.freecol.common.model.player.Player
import promitech.colonization.GameResources
import promitech.colonization.screen.ui.FrameWithCornersDrawableSkin
import promitech.colonization.ui.addListener
import promitech.colonization.ui.resources.Messages

internal class GoldBox(
	val fromPlayer : Player,
	val toPlayer : Player,
	val skin : Skin,
	val addListener : (TradeItem) -> Unit
)
{
	private val goldAmountTextField = TextField("", skin)
	private val addButton = TextButton(Messages.msg("negotiationDialog.add"), skin)
	
	val box = Table()
	
	init {
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
			addGold(goldAmountTextField.getText().toInt())
		}

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
	}
	
	fun addGold(gold : Int) {
		val item = GoldTradeItem(gold, fromPlayer, toPlayer)
		addListener(item)
		goldAmountTextField.setText("")
		addButton.setDisabled(true)
	}
}