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
import promitech.colonization.ui.ModalDialogSize
import promitech.colonization.ui.SimpleMessageDialog
import promitech.colonization.ui.addListener
import promitech.colonization.ui.addSelectListener
import promitech.colonization.ui.withButton
import promitech.colonization.ui.addKeyTypedListener
import promitech.colonization.screen.map.hud.GUIGameModel
import promitech.colonization.savegame.SaveGameList
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener
import net.sf.freecol.common.util.StringUtils

class SaveGameDialog(
	private val shapeRenderer : ShapeRenderer,
	private val guiGameModel : GUIGameModel
)
	: ClosableDialog<SaveGameDialog>(
		ModalDialogSize.width50(), ModalDialogSize.height75()
	)
{
	
	private val gameNameTextField : TextField
	private val okButton : TextButton
	private var gamesTable : STable
	
	init {
		okButton = TextButton(Messages.msg("ok"), skin)
		gamesTable = STable(shapeRenderer)
		
		gameNameTextField = TextField("", skin)
		gameNameTextField.addKeyTypedListener { _, _ ->
			if (gameNameTextField.getText().isNullOrBlank()) {
				okButton.setDisabled(true)
			} else {
				okButton.setDisabled(false)
			}
		}
		
		withHidingOnEsc()
		createLayout()
	}
	
	override fun init(shapeRenderer : ShapeRenderer) {
		var defaultSaveName = Messages.message(guiGameModel.game.playingPlayer.getNationName()) +
			" " + Messages.message(guiGameModel.game.turn.getTurnDateLabel());
		
		gameNameTextField.setText(defaultSaveName)
		
		SaveGameList().loadGameNames()
			.forEach { name ->
				gamesTable.addRow(name, intArrayOf(Align.left), *arrayOf(Label(name, skin)) )
			}
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
		val cancelButton = TextButton(Messages.msg("cancel"), skin)
		getButtonTable().add(cancelButton).pad(10f).fillX().expandX()
		getButtonTable().add(okButton).pad(10f).fillX().expandX()

		okButton.addListener { _, _ ->
			var saves = SaveGameList()
			if (saves.isSaveNameExists(gameNameTextField.getText())) {
				showConfirmation(saves)
			} else {
				saveInWaitDialog(saves)
			}
		}
		cancelButton.addListener { _, _ ->
			hideWithFade()
		}		
	}
	
	private fun showConfirmation(saves : SaveGameList) {
		val confirmationDialog = SimpleMessageDialog()
		confirmationDialog.withContent("saveConfirmationDialog.areYouSure.text")
			.withButton("no")
			.withButton("yes", { confirmDialog ->
				saveInWaitDialog(saves)
				confirmDialog.hideWithoutFade()
			})
		showDialog(confirmationDialog)
	}
	
	private fun saveInWaitDialog(saves : SaveGameList) {
		WaitDialog(Messages.msg("status.savingGame"), {
			saves.saveAs(gameNameTextField.getText(), guiGameModel.game)
		}, { })
			.show(this@SaveGameDialog.dialog.getStage())

		this@SaveGameDialog.hideWithoutFade()
	}
		
	override fun show(stage : Stage) {
		super.show(stage)
		resetPositionToCenter()
	}
}