package promitech.colonization.screen.menu

import com.badlogic.gdx.scenes.scene2d.ui.TextField
import promitech.colonization.GameResources
import promitech.colonization.ui.ClosableDialog
import promitech.colonization.ui.ClosableDialogSize
import promitech.colonization.ui.STable
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.scenes.scene2d.ui.Label
import promitech.colonization.ui.resources.Messages
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import promitech.colonization.ui.STableSelectListener
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import promitech.colonization.ui.addSingleClickSelectListener
import promitech.colonization.ui.withButton
import promitech.colonization.ui.addListener

class LoadGameDialog(
	private val shapeRenderer : ShapeRenderer
)
: ClosableDialog<SaveGameDialog>(
	ClosableDialogSize.width50(), ClosableDialogSize.height75()
) {
	
	private val skin : Skin
	private val gameNameLabel : Label
	private var gamesTable : STable
	
	init {
		skin = GameResources.instance.getUiSkin()
		withHidingOnEsc()
		
		gameNameLabel = Label("", skin)
		gamesTable = STable(shapeRenderer)
		
		createLayout()
	}
	
	override fun init(shapeRenderer : ShapeRenderer) {
		// TODO: load games
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
		getContentTable().add(gameNameLabel)
			.pad(10f, 10f, 0f, 10f)
			.fillX().expandX().row()
		
		gamesTable.addSingleClickSelectListener { payload ->
			gameNameLabel.setText(payload as String)
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
			// TODO: load
			hideWithFade()
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