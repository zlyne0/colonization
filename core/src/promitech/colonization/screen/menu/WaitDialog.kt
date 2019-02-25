package promitech.colonization.screen.menu

import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import promitech.colonization.ui.ClosableDialog
import promitech.colonization.ui.ModalDialogSize
import com.badlogic.gdx.scenes.scene2d.ui.Label
import promitech.colonization.ui.resources.Messages
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.scenes.scene2d.Stage
import promitech.colonization.infrastructure.ThreadsResources
import com.badlogic.gdx.Gdx
import promitech.colonization.ui.ModalDialog

class WaitDialog(
	private val msg : String,
	private val backgroundJob : () -> Unit,
	private val runAfterJob : () -> Unit
)
: ModalDialog<SaveGameDialog>(
	ModalDialogSize.def(), ModalDialogSize.def()
)
{
	init {
		getContentTable().add(Label(msg, skin))
			.align(Align.center)
			.pad(20f, 20f, 20f, 20f)
			.row()
	}

	override fun show(stage : Stage) {
		super.show(stage)
		
		ThreadsResources.instance.execute(object : Runnable {
			override fun run() {
				this@WaitDialog.backgroundJob()
				
				Gdx.app.postRunnable(object : Runnable {
					override fun run() {
						this@WaitDialog.hideWithoutFade()
						this@WaitDialog.runAfterJob()
					}
				})
			}
		})
	}
}