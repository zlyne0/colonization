package promitech.colonization.actors.cheat

import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.UnitIterator
import net.sf.freecol.common.model.map.generator.MapGenerator
import promitech.colonization.GUIGameController
import promitech.colonization.GUIGameModel
import promitech.colonization.GameCreator

abstract class Task(var cmd: String) {
	abstract fun run()
	
	fun isMatchToExecute(enteredCmd: String) : Boolean {
		return cmd.equals(enteredCmd);
	}
	
	fun isMatchToHint(enteredCmd: String) : Boolean {
		return cmd.startsWith(enteredCmd, true)
	}
}

class Alias(cmd: String, val task: Task) : Task(cmd) {
	override fun run() {
		task.run()
	}
}

class CommandExecutor(var gameController: GUIGameController, var guiGameModel: GUIGameModel) {

	var tasks : List<Task> = mutableListOf<Task>(
		object : Task("map show") {
			override fun run() {
			    guiGameModel.game.playingPlayer.explorAllTiles();
			    guiGameModel.game.playingPlayer.fogOfWar.removeFogOfWar();
			    gameController.resetMapModel();
			}
		},
		object : Task("map generate") {
			override fun run() {
                guiGameModel.game.map = MapGenerator().generate(guiGameModel.game.players);
                gameController.resetMapModel();
			}
		},
		object : Task("m") {
			override fun run() {
			    guiGameModel.game.playingPlayer.explorAllTiles();
			    guiGameModel.game.playingPlayer.fogOfWar.removeFogOfWar();
			    gameController.resetMapModel();
			    
			    guiGameModel.game.map = MapGenerator().generate(guiGameModel.game.players);
			    gameController.resetMapModel();
			}
		},
		object : Task("map show owners") {
			override fun run() {
			    gameController.showTilesOwners();
			}
		},
		object : Task("map hide owners") {
			override fun run() {
			    gameController.hideTilesOwners();
			}
		},
		object : Task("new game") {
			override fun run() {
			    GameCreator(guiGameModel).initNewGame();
			    gameController.resetMapModel();
			    gameController.nextActiveUnit();
			}
		},
		object : Task("load game") {
			override fun run() {
			    GameCreator(guiGameModel).initGameFromSavegame();
			    gameController.resetMapModel();
			    gameController.nextActiveUnit();
			}
		},
		object : Task("sp") {
			override fun run() {
			    guiGameModel.game.playingPlayer.setAi(true);
			    
			    var newHumanPlayer = guiGameModel.game.players.getById("player:112");
			    guiGameModel.game.setCurrentPlayer(newHumanPlayer);
			    
			    guiGameModel.unitIterator = UnitIterator(guiGameModel.game.playingPlayer, Unit.ActivePredicate());
			    guiGameModel.game.playingPlayer.setAi(false);
			    
			    gameController.resetUnexploredBorders();
			    gameController.resetMapModel();
			    
			    gameController.centerOnTile(guiGameModel.game.playingPlayer.getEntryLocationX(), guiGameModel.game.playingPlayer.getEntryLocationY());
			    
			    gameController.nextActiveUnit();
			}
		}
	);

	init {
		tasks = tasks.plus(createAlias("zzz", "load game"))
		tasks = tasks.sortedBy { task -> task.cmd }
	}
	
	fun createAlias(cmd: String, destCmd: String) : Task {
	    var execTask = tasks
			.filter { t -> t.isMatchToExecute(destCmd) }
			.singleOrNull()
		if (execTask == null) {
			throw IllegalStateException("can not find task: ${destCmd}")
		}
		return Alias(cmd, execTask)
	}		
	
	fun execute(cmd: String) : Boolean {
		
	    var execTask = tasks
			.filter { t -> t.isMatchToExecute(cmd) }
			.singleOrNull()
		if (execTask != null) {
			execTask.run()
			return true
		}
		return false
	}
	
	fun filterTasksForHint(enteredCmd: String) : List<Task> {
		return tasks.filter { t-> t.isMatchToHint(enteredCmd)}
	}
}
