package promitech.colonization.screen.ff

import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import net.sf.freecol.common.model.Turn
import net.sf.freecol.common.model.player.Player
import promitech.colonization.ui.ModalDialog
import promitech.colonization.ui.ModalDialogSize
import promitech.colonization.ui.addListener
import promitech.colonization.ui.resources.Messages

class ChooseFoundingFatherDialog(
	player : Player,
	turn : Turn
)
	: ModalDialog<ChooseFoundingFatherDialog>(ModalDialogSize.width50(), ModalDialogSize.height75())
{
	private val typeButtons = Table()
	private val ffInfoPanel = FoundingFatherInfoPanel(skin)
	
	init {
		val foundingFathers = player.foundingFathers.generateRandomFoundingFathers(turn)
		
		foundingFathers.forEach { (fft, ff) ->
			val button = TextButton(Messages.msg(fft.msgKey()), skin)
			button.addListener { _, _ ->
				ffInfoPanel.update(ff)
			}
			typeButtons.add(button).padLeft(10f)
		}
		
		if (foundingFathers.isNotEmpty()) {
			val first = foundingFathers.iterator().next()
			ffInfoPanel.update(first.value)
		}
		
		val layout = Table()
		layout.setFillParent(true)
		layout.add(typeButtons).pad(10f).row()
		layout.add(ffInfoPanel).left().row()
		
		getContentTable().add(layout).expandX().fillX().row()

		val okButton = TextButton(Messages.msg("ok"), skin)
		okButton.addListener { _, _ ->
			player.foundingFathers.setCurrentFoundingFather(ffInfoPanel.foundingFather)
			hideWithFade()
		}		
		getButtonTable().add(okButton).pad(10f).fillX().expandX()
	}
		
}