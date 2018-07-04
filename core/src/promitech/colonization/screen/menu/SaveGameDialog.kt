package promitech.colonization.screen.menu

import promitech.colonization.ui.ClosableDialog
import promitech.colonization.GameResources
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import promitech.colonization.ui.STable
import promitech.colonization.ui.resources.Messages
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import promitech.colonization.ui.STableSelectListener
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import promitech.colonization.ui.ClosableDialogSize
import promitech.colonization.ui.SimpleMessageDialog
import promitech.colonization.ui.addListener
import promitech.colonization.ui.addSelectListener
import promitech.colonization.ui.withButton

class SaveGameDialog(
	private val shapeRenderer : ShapeRenderer
)
	: ClosableDialog<SaveGameDialog>(
		ClosableDialogSize.width50(), ClosableDialogSize.height75()
	)
{
	
	private val skin : Skin
	private val gameNameTextField : TextField
	private var gamesTable : STable
	
	init {
		skin = GameResources.instance.getUiSkin()
		withHidingOnEsc()
		
		gameNameTextField = TextField("", skin)
		gamesTable = STable(shapeRenderer)
		
		createLayout()
	}
	
	override fun init(shapeRenderer : ShapeRenderer) {
		// TODO: from game nation name, data
		gameNameTextField.setText("save game turn 40")
		for (i in 1 .. 100) {
			gamesTable.addRow("one" + i, intArrayOf(Align.left), *arrayOf(Label("one" + i, skin)) )
		}
//		gamesTable.addRow("two", intArrayOf(Align.left), *arrayOf(Label("two", skin)) )
//		gamesTable.addRow("three", intArrayOf(Align.left), *arrayOf(Label("thre", skin)) )
		
	}
	
	private fun createLayout() {
		getContentTable().add(Label(Messages.msg("save.game.dialog.game.name.label"), skin))
			.align(Align.left)
			.pad(10f, 10f, 0f, 10f)
			.row()
		getContentTable().add(gameNameTextField)
			.pad(10f, 10f, 0f, 10f)
			.fillX().expandX().row()
		
		gamesTable.addSelectListener {
			payload -> gameNameTextField.setText(payload as String)
		}

		var scrollPane = ScrollPane(gamesTable, skin)
		scrollPane.setFlickScroll(false)
		scrollPane.setScrollingDisabled(true, false)
		scrollPane.setForceScroll(false, true)
		scrollPane.setFadeScrollBars(false)
		scrollPane.setScrollBarPositions(false, true)
		
		getContentTable().add(scrollPane)
			.align(Align.top)
			.fillX()
			.expand()
			.pad(10f, 10f, 0f, 10f)
			.row()
				
		createButtons()
	}

	private fun createButtons() {
		buttonTableLayoutExtendX()
		val okButton = TextButton(Messages.msg("ok"), skin)
		val cancelButton = TextButton(Messages.msg("cancel"), skin)
		getButtonTable().add(cancelButton).pad(10f).fillX().expandX()
		getButtonTable().add(okButton).pad(10f).fillX().expandX()

		okButton.addListener { _, _ ->
			// TODO: save
			hideWithFade()
		}
		cancelButton.addListener { _, _ ->
			hideWithFade()
		}		
	}
	
	private fun showConfirmation() {
		val confirmationDialog = SimpleMessageDialog()
		confirmationDialog.withContent("saveConfirmationDialog.areYouSure.text")
			.withButton("no")
			.withButton("yes", { d -> 
				// TODO: save
				d.hideWithoutFade()
				hideWithoutFade()
			})
		// TODO: polaczenie dwuch ponizszych?
		this.addChildDialog(confirmationDialog)
		showDialog(confirmationDialog)
	}
		
	override fun show(stage : Stage) {
		super.show(stage)
		resetPositionToCenter()
	}
}