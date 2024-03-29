package promitech.colonization.screen.debug

import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.utils.Align
import net.sf.freecol.common.model.Tile
import promitech.colonization.screen.map.hud.GUIGameController
import promitech.colonization.screen.map.hud.GUIGameModel
import promitech.colonization.GameResources
import promitech.colonization.ui.ClosableDialog
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import net.sf.freecol.common.model.map.generator.MapGenerator
import net.sf.freecol.common.model.UnitIterator
import net.sf.freecol.common.model.Unit
import promitech.colonization.DI

class DebugConsole(val commands : Commands)
	: ClosableDialog<DebugConsole>(), ConsoleOutput
{
	private val dialogLayout = Table()
	private val textField = TextField("", GameResources.instance.getUiSkin())
	private val label = Label("", GameResources.instance.getUiSkin())
	private val scrollPane = ScrollPane(label)
			
	var keepOpenConsoleAfterExecute : Boolean = false
	
	init {
		label.setAlignment(Align.top or Align.left);
		//label.setFillParent(true)
		
		scrollPane.setForceScroll(false, false)
		scrollPane.setFadeScrollBars(false)
		scrollPane.setOverscroll(true, true)
		scrollPane.setScrollBarPositions(false, true)
		
		dialogLayout.add(scrollPane).expand().fill().row()
		dialogLayout.add(textField).fillX().expandX()
		getContentTable().add(dialogLayout)

		
		dialog.addListener(object : InputListener() {
			override fun keyDown(event: InputEvent, keycode: Int) : Boolean {
				if (keycode == Keys.ENTER) {
					executeCommand()
					return true
				}
				if (keycode == Keys.TAB) {
					hintCommand()
					return true
				}
				return false
			}
		})
				
		withHidingOnEsc();
	}
	
	fun executeCommand() {
		var cmd = textField.getText()
		textField.setText("")
		out(cmd)
		
		val executedCmd = commands.execute(cmd)
		if (executedCmd != null && !keepOpenConsoleAfterExecute) {
			hideWithFade()
		}
		keepOpenConsoleAfterExecute = false
	}
	
	fun hintCommand() {
		var enteredCmd = textField.getText();
		out("  hints: ")
		val filteredCommands = commands.filterCommandsByPrefix(enteredCmd)

		filteredCommands.forEach { cmdName ->
			out(cmdName)
		}

		var enlargedCommand = commands.enlargeHintCommandToBetterMatch(enteredCmd)		
		textField.setText(enlargedCommand)
		if (filteredCommands.size == 1) {
			textField.setText(textField.getText() + " ")
		}
		textField.setCursorPosition(textField.getText().length)
	}
	
	override fun out(line: String) : ConsoleOutput {
		if (label.getText().toString().equals("")) {
			label.setText(line)			
		} else {
			label.setText(label.getText().toString() + "\n" + line)
		}
		scrollPane.setScrollPercentY(100f)
		scrollPane.layout()
		return this
	}
	
	override fun keepOpen() : ConsoleOutput {
		keepOpenConsoleAfterExecute = true
		return this
	}
	
	override fun show(stage: Stage) {
		getContentTable().getCell(dialogLayout)
			.width(stage.getWidth() * 0.75f)
			.height(stage.getHeight() * 0.75f)
		
		super.show(stage)
		stage.setKeyboardFocus(textField)
	}
}
