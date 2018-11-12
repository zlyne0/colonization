package promitech.colonization.screen.map.diplomacy

import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import promitech.colonization.ui.ClosableDialog
import promitech.colonization.ui.addListener
import promitech.colonization.ui.resources.Messages
import promitech.colonization.ui.resources.StringTemplate
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.scenes.scene2d.ui.Label
import promitech.colonization.ui.ModalDialogSize

public class SpeakResultMsgDialog(msg : String)
	: ClosableDialog<SpeakResultMsgDialog>(ModalDialogSize.width50(), ModalDialogSize.def())
{
	init {
		withHidingOnEsc()
		
		val label = Label(msg, skin)
		label.setWrap(true)
		
		val layoutTable = Table()
		layoutTable.defaults().align(Align.top or Align.left)
		layoutTable.add(label).fillX().expandX().pad(20f)
		
		getContentTable().add(layoutTable).expandX().fillX().row()
		
		val okButton = TextButton(Messages.msg("ok"), skin)
		okButton.addListener { _, _ ->
			hideWithFade()
		}
		getButtonTable().add(okButton).pad(10f).fillX().expandX()
	}
	
}