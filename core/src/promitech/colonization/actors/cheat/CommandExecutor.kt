package promitech.colonization.actors.cheat

import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.UnitIterator
import net.sf.freecol.common.model.map.PathFinder
import net.sf.freecol.common.model.map.generator.MapGenerator
import promitech.colonization.DI
import promitech.colonization.GameCreator
import promitech.colonization.actors.map.MapActor
import promitech.colonization.ai.AILogicDebugRun
import promitech.colonization.ai.BuildColony
import promitech.colonization.ai.NavyExplorer
import promitech.colonization.infrastructure.ThreadsResources

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

class CommandExecutor(var di: DI, val mapActor: MapActor) {
    var gameController = di.guiGameController
	var guiGameModel = di.guiGameModel
		
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
		},
		object : Task("ai settlements") {
		    override fun run() {
				theBestPlaceToBuildColony()
			}
		},
		object : Task("ai explore") {
			override fun run() {
				theBestMove()
			}
		},
		object : Task("ai move") {
			override fun run() {
				aiMove()
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
	
    fun theBestPlaceToBuildColony() {
		System.out.println("theBestPlaceToBuildColony")
		
        guiGameModel = di.guiGameModel;
        
        var buildColony = BuildColony(guiGameModel.game.map);
        buildColony.generateWeights(
			guiGameModel.game.playingPlayer,
			setOf(BuildColony.TileSelection.ONLY_SEASIDE, BuildColony.TileSelection.WITHOUT_UNEXPLORED)
        );
		
		val tileStrings = Array(guiGameModel.game.map.height, { Array(guiGameModel.game.map.width, {""}) })
		buildColony.toStringValues(tileStrings)
		mapActor.showTileDebugStrings(tileStrings)
    }

    fun theBestMove() {
		System.out.println("theBestMove")
		
        var guiGameModel = di.guiGameModel
        var moveController = di.moveController
        
        var unit = guiGameModel.getActiveUnit();
        if (unit == null) {
            System.out.println("no unit selected");
            return;
        }
        System.out.println("the best move");

        
        var pathFinder = PathFinder()
        pathFinder.generateRangeMap(guiGameModel.game.map, unit.getTile(), unit);
        
        var navyExplorer = NavyExplorer(guiGameModel.game.map);
        navyExplorer.generateExploreDestination(pathFinder, unit.getOwner());
        
        if (navyExplorer.isFoundExploreDestination()) {
            if (navyExplorer.isExploreDestinationInOneTurn()) {
                var direction = navyExplorer.getExploreDestinationAsDirection();
                System.out.println("exploration destination " + direction);
                moveController.pressDirectionKey(direction);
            } else {
                System.out.println("exploration path " + navyExplorer.getExploreDestinationAsPath());
            }
        } else {
            // maybe is everything explored or blocked in some how
            System.out.println("can not find tile to explore");
        }
        
		val tileStrings = Array(guiGameModel.game.map.height, { Array(guiGameModel.game.map.width, {""}) })
        navyExplorer.toStringsBorderValues(tileStrings);
        mapActor.showTileDebugStrings(tileStrings);
    }
	
	fun aiMove() {
		if (di.guiGameModel.isActiveUnitNotSet()) {
			System.out.println("no active unit");
			return
		}
		
		ThreadsResources.instance.executeAImovement(object : Runnable {
			override fun run() {
				AILogicDebugRun(di.guiGameModel, di.moveLogic).run()
			}
		})
	}
}
