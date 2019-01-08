package promitech.colonization.screen.menu

import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.utils.Align
import promitech.colonization.GameResources
import promitech.colonization.savegame.SaveGameList
import promitech.colonization.screen.map.hud.GUIGameController
import promitech.colonization.ui.ClosableDialog
import promitech.colonization.ui.ModalDialogSize
import promitech.colonization.ui.STable
import promitech.colonization.ui.addListener
import promitech.colonization.ui.resources.Messages
import promitech.colonization.GameCreator
import promitech.colonization.screen.map.hud.GUIGameModel

class LoadGameDialog(
	private val shapeRenderer : ShapeRenderer,
	private val guiGameController : GUIGameController,
	private val guiGameModel : GUIGameModel
)
: ClosableDialog<SaveGameDialog>(
	ModalDialogSize.width50(), ModalDialogSize.height75()
) {
	
	private val gameNameLabel : Label
	private var gamesTable : STable
	private val okButton : TextButton
	
	init {
		withHidingOnEsc()
		
		okButton = TextButton(Messages.msg("ok"), skin)
		okButton.setDisabled(true)
		gameNameLabel = Label("", skin)
		gamesTable = STable(shapeRenderer)
		
		createLayout()
	}
	
	override fun init(shapeRenderer : ShapeRenderer) {
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
		getContentTable().add(gameNameLabel)
			.pad(10f, 10f, 0f, 10f)
			.fillX().expandX().row()
		
		gamesTable.addSingleClickSelectListener { payload ->
			gameNameLabel.setText(payload as String)
			okButton.setDisabled(false)
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
			
			WaitDialog(Messages.msg("status.loadingGame"), {
				GameCreator(guiGameModel).load(gameNameLabel.text.toString())
			}, {
				guiGameController.resetMapModel()
				guiGameController.showMapScreenAndActiveNextUnit()
			}).show(this@LoadGameDialog.dialog.getStage())
			
			hideWithoutFade()
		}
		cancelButton.addListener { _, _ ->
			hideWithFade()
		}
	}
	
	override fun show(stage : Stage) {
		super.show(stage)
		resetPositionToCenter()
	}
}