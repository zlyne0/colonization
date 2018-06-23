package promitech.colonization.screen.menu

import promitech.colonization.screen.ApplicationScreen
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import promitech.colonization.ui.resources.Messages
import promitech.colonization.GameResources
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.InputEvent
import promitech.colonization.GameCreator
import promitech.colonization.screen.map.hud.GUIGameController
import promitech.colonization.ui.ClosableDialog

class MenuApplicationScreen (
	private val guiGameController : GUIGameController
) : ApplicationScreen() {

	private val stage: Stage
	private val newGameButton: TextButton
	private val loadGameButton: TextButton
	private val saveGameButton: TextButton
	private val optionsButton: TextButton
	private val exitButton: TextButton
	private val buttonListener : InputListener

	init {
		stage = Stage()
		
		newGameButton = TextButton(Messages.msg("newAction.name"), GameResources.instance.getUiSkin())
		loadGameButton = TextButton(Messages.msg("openAction.name"), GameResources.instance.getUiSkin())
		saveGameButton = TextButton(Messages.msg("saveAction.name"), GameResources.instance.getUiSkin())
		optionsButton = TextButton(Messages.msg("preferencesAction.name"), GameResources.instance.getUiSkin())
		exitButton = TextButton(Messages.msg("quitAction.name"), GameResources.instance.getUiSkin())
		
		buttonListener = object : InputListener() {
			override fun touchDown(event : InputEvent, x : Float, y : Float, pointer : Int, button : Int) : Boolean {
				if (event.getListenerActor() == newGameButton) {
					guiGameController.showMapScreenOnStartNewGame()
					return true
				}
				if (event.getListenerActor() == exitButton) {
					exitFromGame()
					return true
				}
				
				if (event.getListenerActor() == saveGameButton) {
					showDialog(SaveGameDialog(shape))
					return true
				}
				return false
			}
		}
	}
	
	private fun showDialog(closableDialog : ClosableDialog<*>) {
		closableDialog.init(shape);
		closableDialog.show(stage);
	}
	
	public fun inject() {
		
	}
	
	override fun create() {
		newGameButton.addListener(buttonListener)
		loadGameButton.addListener(buttonListener)
		saveGameButton.addListener(buttonListener)
		optionsButton.addListener(buttonListener)
		exitButton.addListener(buttonListener)
		
		val buttonTable = Table()
		
		buttonTable.setFillParent(true)
		buttonTable.defaults()
			.center()
			.width(stage.getWidth() * 0.50f)
			.padTop(20f)
		buttonTable.add(newGameButton).row()
		buttonTable.add(loadGameButton).row()
		buttonTable.add(saveGameButton).row()
		buttonTable.add(optionsButton).row()
		buttonTable.add(exitButton).row()
		
		stage.addActor(buttonTable)
//		stage.setDebugAll(true)
	}

	override fun onShow() {
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