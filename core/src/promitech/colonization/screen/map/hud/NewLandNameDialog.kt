package promitech.colonization.screen.map.hud

import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.utils.Align
import net.sf.freecol.common.model.player.Player
import promitech.colonization.GameResources
import promitech.colonization.ui.ClosableDialog
import promitech.colonization.ui.ClosableDialogSize
import promitech.colonization.ui.addKeyTypedListener
import promitech.colonization.ui.addListener
import promitech.colonization.ui.resources.Messages

class NewLandNameDialog(
	val player : Player,
	val defaultName : String
)
	: ClosableDialog<NewLandNameDialog>(ClosableDialogSize.width50(), ClosableDialogSize.def())
{

	val skin : Skin
	val okButton : TextButton
	val nameField : TextField
	
	init {
		skin = GameResources.instance.getUiSkin()
		
        nameField = TextField(defaultName, skin)
        okButton = TextButton(Messages.msg("newLand.yes"), skin)

		nameField.addKeyTypedListener{ _, _ -> 
			if (nameField.getText().isNullOrBlank()) {
				okButton.setDisabled(true)
			} else {
				okButton.setDisabled(false)
			}
		}
		okButton.addListener { _, _ ->
			player.setNewLandName(nameField.getText())
			hideWithFade()
		}
		layout()		
	}
	
	private fun layout() {
	    getContentTable()
    	    .add(Label(Messages.msg("newLand.text"), skin))
    	    .align(Align.left)
    	    .pad(20f, 20f, 0f, 20f)
    	    .row()
	    
	    getContentTable()
    	    .add(nameField)
    	    .fillX()
    	    .expandX()
    	    .pad(20f, 20f, 0f, 20f)
    	    .row()
	    
	    buttonTableLayoutExtendX()
	    getButtonTable().add(okButton).pad(10f).fillX().expandX()
	}
	
	override fun init(shapeRenderer : ShapeRenderer) {
	}
	
	override fun show(stage : Stage) {
		super.show(stage)
		resetPositionToCenter()
	}
	
}