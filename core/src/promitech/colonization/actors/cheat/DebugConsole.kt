package promitech.colonization.actors.cheat

import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.utils.Align
import net.sf.freecol.common.model.Tile
import promitech.colonization.GUIGameController
import promitech.colonization.GUIGameModel
import promitech.colonization.GameResources
import promitech.colonization.ui.ClosableDialog
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import net.sf.freecol.common.model.map.generator.MapGenerator
import promitech.colonization.GameCreator
import net.sf.freecol.common.model.UnitIterator
import net.sf.freecol.common.model.Unit

class CommandExecutor(var gameController: GUIGameController, var guiGameModel: GUIGameModel) {
	
	fun execute(cmd: String) : Boolean {
		if (cmd.equals("map show")) {
			guiGameModel.game.playingPlayer.explorAllTiles();
			guiGameModel.game.playingPlayer.fogOfWar.removeFogOfWar();
			gameController.resetMapModel();
			return true
		}
		
		if (cmd.equals("map generate")) {
			guiGameModel.game.map = MapGenerator().generate(guiGameModel.game.players);
			gameController.resetMapModel();
			return true
		}
		
		if (cmd.equals("m")) {
			guiGameModel.game.playingPlayer.explorAllTiles();
			guiGameModel.game.playingPlayer.fogOfWar.removeFogOfWar();
			gameController.resetMapModel();
			
			guiGameModel.game.map = MapGenerator().generate(guiGameModel.game.players);
			gameController.resetMapModel();
			return true
		}
		
		if (cmd.equals("map show owners") || cmd.equals("mso") ) {
			gameController.showTilesOwners();
			return true
		}
		
		if (cmd.equals("map hide owners") || cmd.equals("mho")) {
			gameController.hideTilesOwners();
			return true
		}
		
		if (cmd.equals("new game")) {
		    GameCreator(guiGameModel).initNewGame();
		    gameController.resetMapModel();
		    gameController.nextActiveUnit();
			return true
		}
		
		if (cmd.equals("load game")) {
		    GameCreator(guiGameModel).initGameFromSavegame();
		    gameController.resetMapModel();
		    gameController.nextActiveUnit();
			return true
		}
		
		if (cmd.equals("sp")) {
			guiGameModel.game.playingPlayer.setAi(true);
			
			var newHumanPlayer = guiGameModel.game.players.getById("player:112");
			guiGameModel.game.setCurrentPlayer(newHumanPlayer);
			
			guiGameModel.unitIterator = UnitIterator(guiGameModel.game.playingPlayer, Unit.ActivePredicate());
			guiGameModel.game.playingPlayer.setAi(false);
			
			gameController.resetUnexploredBorders();
			gameController.resetMapModel();
			
			gameController.centerOnTile(guiGameModel.game.playingPlayer.getEntryLocationX(), guiGameModel.game.playingPlayer.getEntryLocationY());

			gameController.nextActiveUnit();
			
			return true
		}
		
		return false
	}
}

class DebugConsole : ClosableDialog<DebugConsole> {
	private val dialogLayout = Table()
	private val textField = TextField("", GameResources.instance.getUiSkin())
	private val label = Label("", GameResources.instance.getUiSkin())
	private val scrollPane = ScrollPane(label)
			
	var selectedTile: Tile? = null
	
	var commandExecutor : CommandExecutor
	
	constructor(gameController: GUIGameController, guiGameModel: GUIGameModel) : super("", GameResources.instance.getUiSkin()) {
		label.setAlignment(Align.top or Align.left);
		label.setFillParent(true)
		
		scrollPane.setForceScroll(false, false);
		scrollPane.setFadeScrollBars(false);
		scrollPane.setOverscroll(true, true);
		scrollPane.setScrollBarPositions(false, true);
		
		dialogLayout.add(scrollPane).expand().fill().row();
		dialogLayout.add(textField).fillX().expandX();
		getContentTable().add(dialogLayout);

		
		dialog.addListener(object : InputListener() {
			override fun keyDown(event: InputEvent, keycode: Int) : Boolean {
				if (keycode == Keys.ENTER) {
					executeCommand();
					return true;
				}
				return false;
			}
		})
				
		withHidingOnEsc();
		commandExecutor = CommandExecutor(gameController, guiGameModel)
	}
	
	fun executeCommand() {
		var cmd = textField.getText();
		textField.setText("");
		addConsoleLine(cmd)
		
		if (commandExecutor.execute(cmd)) {
			hideWithFade();
		}
	}
	
	fun addConsoleLine(line: String) {
		if (label.getText().toString().equals("")) {
			label.setText(line);			
		} else {
			label.setText(label.getText().toString() + "\r\n" + line)
		}
		scrollPane.setScrollPercentY(100f);
		scrollPane.layout();
	}
	
	override fun show(stage: Stage) {
		getContentTable().getCell(dialogLayout)
			.width(stage.getWidth() * 0.75f)
			.height(stage.getHeight() * 0.75f);
		
		super.show(stage)
		stage.setKeyboardFocus(textField);
	}
}
