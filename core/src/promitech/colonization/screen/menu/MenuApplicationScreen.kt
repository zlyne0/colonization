package promitech.colonization.screen.menu

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import promitech.colonization.GameResources
import promitech.colonization.savegame.SaveGameList
import promitech.colonization.screen.ApplicationScreen
import promitech.colonization.screen.map.hud.GUIGameController
import promitech.colonization.screen.map.hud.GUIGameModel
import promitech.colonization.ui.ClosableDialog
import promitech.colonization.ui.addListener
import promitech.colonization.ui.resources.Messages

class MenuApplicationScreen (
	private val guiGameController : GUIGameController,
	private val guiGameModel : GUIGameModel
) : ApplicationScreen() {

	private val stage: Stage
	private val newGameButton: TextButton
	private val backButton: TextButton
	private val loadGameButton: TextButton
	private val loadLastGameButton: TextButton
	private val saveGameButton: TextButton
	private val optionsButton: TextButton
	private val exitButton: TextButton

	init {
		stage = Stage()
		
		newGameButton = TextButton(Messages.msg("newAction.name"), GameResources.instance.getUiSkin())
		backButton = TextButton(Messages.msg("mainmenu.back.name"), GameResources.instance.getUiSkin())
		loadGameButton = TextButton(Messages.msg("openAction.name"), GameResources.instance.getUiSkin())
		loadLastGameButton = TextButton(Messages.msg("openAction.name"), GameResources.instance.getUiSkin())
		saveGameButton = TextButton(Messages.msg("saveAction.name"), GameResources.instance.getUiSkin())
		optionsButton = TextButton(Messages.msg("preferencesAction.name"), GameResources.instance.getUiSkin())
		exitButton = TextButton(Messages.msg("quitAction.name"), GameResources.instance.getUiSkin())
		
		newGameButton.addListener { _, _ -> 
			guiGameController.showMapScreenOnStartNewGame()
		}
		backButton.addListener { _, _ ->
			guiGameController.showMapScreenAndActiveNextUnit()
		}
		loadGameButton.addListener { _, _ ->
			showDialog(LoadGameDialog(shape, guiGameController))
		}
		loadLastGameButton.addListener { _, _ ->
			guiGameController.showMapScreenAndLoadLastGame()
		}
		exitButton.addListener { _, _ ->
			exitFromGame()
		}
		saveGameButton.addListener { _, _ ->
			showDialog(SaveGameDialog(shape, guiGameModel))
		}
	}
	
	private fun showDialog(closableDialog : ClosableDialog<*>) {
		closableDialog.init(shape);
		closableDialog.show(stage);
	}
	
	public fun inject() {
		
	}
	
	override fun create() {
		
		val buttonTable = Table()
		
		buttonTable.setFillParent(true)
		buttonTable.defaults()
			.center()
			.width(stage.getWidth() * 0.50f)
			.padTop(20f)
		buttonTable.add(loadLastGameButton).padBottom(40f).row()
		buttonTable.add(newGameButton).row()
		buttonTable.add(backButton).row()
		buttonTable.add(loadGameButton).row()
		buttonTable.add(saveGameButton).row()
		buttonTable.add(optionsButton).row()
		buttonTable.add(exitButton).row()
		
		stage.addActor(buttonTable)
	}

	override fun onShow() {
		if (guiGameModel.game == null) {
			backButton.setDisabled(true)
			saveGameButton.setDisabled(true)
		} else {
			backButton.setDisabled(false)
			saveGameButton.setDisabled(false)
		}
		
		var lastSaveName = SaveGameList().lastOneSaveName() 
		if (lastSaveName != null) {
			loadGameButton.setDisabled(false)
			loadLastGameButton.setDisabled(false)
			loadLastGameButton.setText(Messages.msg("openAction.name") + " " + lastSaveName)
		} else {
			loadLastGameButton.setText(Messages.msg("openAction.name"))
			loadGameButton.setDisabled(true)
			loadLastGameButton.setDisabled(true)
		}
		Gdx.input.setInputProcessor(stage)
	}
	
	override fun onLeave() {
		Gdx.input.setInputProcessor(null)
	}
	
	override fun render() {
		Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
		
        stage.act()
        stage.draw()
	}
	
	override fun resize(width: Int, height : Int) {
		stage.getViewport()?.update(width, height, true);
	}
		
	private fun exitFromGame() {
		Gdx.app.exit()
	}
}