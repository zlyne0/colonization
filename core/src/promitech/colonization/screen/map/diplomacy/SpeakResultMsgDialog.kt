package promitech.colonization.screen.map.diplomacy

import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import net.sf.freecol.common.model.Settlement
import promitech.colonization.GameResources
import promitech.colonization.ui.ClosableDialog
import promitech.colonization.ui.ModalDialogSize
import promitech.colonization.ui.addListener
import promitech.colonization.ui.resources.Messages
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup

public class SpeakResultMsgDialog(val settlement : Settlement, msg : String)
	: ClosableDialog<SpeakResultMsgDialog>(ModalDialogSize.width50(), ModalDialogSize.def())
{
	init {
		withHidingOnEsc()
		
		val img = Image(TextureRegionDrawable(
			GameResources.instance.getCenterAdjustFrameTexture(settlement.getImageKey()).texture
		), Scaling.none, Align.center)
		
		var settlementInfo = HorizontalGroup()
		settlementInfo.addActor(img)
		settlementInfo.addActor(Label(settlement.getName(), skin))
		
		val label = Label(msg, skin)
		label.setWrap(true)
		
		val layoutTable = Table()
		layoutTable.defaults().align(Align.top or Align.left)
		layoutTable.add(settlementInfo).align(Align.center).row()
		layoutTable.add(label).fillX().expandX().pad(20f)
		
		getContentTable().add(layoutTable).expandX().fillX().row()
		
		val okButton = TextButton(Messages.msg("ok"), skin)
		okButton.addListener { _, _ ->
			hideWithFade()
		}
		getButtonTable().add(okButton).pad(10f).fillX().expandX()
	}
	
}