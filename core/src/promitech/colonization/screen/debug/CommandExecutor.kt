package promitech.colonization.screen.debug

import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.UnitIterator
import net.sf.freecol.common.model.ai.missions.SeekAndDestroyMission
import net.sf.freecol.common.model.map.generator.MapGenerator
import net.sf.freecol.common.model.map.path.PathFinder
import promitech.colonization.DI
import promitech.colonization.GameCreator
import promitech.colonization.ai.AILogicDebugRun
import promitech.colonization.ai.BuildColony
import promitech.colonization.ai.NavyExplorer
import promitech.colonization.ai.SeekAndDestroyMissionHandler
import promitech.colonization.infrastructure.ThreadsResources
import promitech.colonization.screen.map.MapActor

abstract class Task(var cmd: String) {
	abstract fun run(console: ConsoleOutput) : Boolean
	
	fun isMatchToExecute(enteredCmd: String) : Boolean {
		return cmd.equals(enteredCmd);
	}
	
	fun isMatchToHint(enteredCmd: String) : Boolean {
		return cmd.startsWith(enteredCmd, true)
	}
}

class Alias(cmd: String, val task: Task) : Task(cmd) {
	override fun run(console: ConsoleOutput) : Boolean {
		return task.run(console)
	}
}

class CommandExecutor(var di: DI, val mapActor: MapActor) {
    var gameController = di.guiGameController
	var guiGameModel = di.guiGameModel
		
	var tasks : List<Task> = mutableListOf<Task>(
		object : Task("map show") {
			override fun run(console: ConsoleOutput) : Boolean {
			    guiGameModel.game.playingPlayer.explorAllTiles();
			    guiGameModel.game.playingPlayer.fogOfWar.removeFogOfWar();
			    gameController.resetMapModel();
				return true
			}
		},
		object : Task("map generate") {
			override fun run(console: ConsoleOutput) : Boolean {
                guiGameModel.game.map = MapGenerator().generate(guiGameModel.game.players);
                gameController.resetMapModel();
				return true
			}
		},
		object : Task("m") {
			override fun run(console: ConsoleOutput) : Boolean {
			    guiGameModel.game.playingPlayer.explorAllTiles();
			    guiGameModel.game.playingPlayer.fogOfWar.removeFogOfWar();
			    gameController.resetMapModel();
			    
			    guiGameModel.game.map = MapGenerator().generate(guiGameModel.game.players);
			    gameController.resetMapModel();
				return true
			}
		},
		object : Task("map show owners") {
			override fun run(console: ConsoleOutput) : Boolean {
			    gameController.showTilesOwners();
				return true
			}
		},
		object : Task("map hide owners") {
			override fun run(console: ConsoleOutput) : Boolean {
			    gameController.hideTilesOwners();
				return true
			}
		},
		object : Task("new game") {
			override fun run(console: ConsoleOutput) : Boolean {
			    GameCreator(guiGameModel).initNewGame();
			    gameController.resetMapModel();
			    gameController.nextActiveUnit();
				return true
			}
		},
		object : Task("load game") {
			override fun run(console: ConsoleOutput) : Boolean {
			    GameCreator(guiGameModel).initGameFromSavegame();
			    gameController.resetMapModel();
			    gameController.nextActiveUnit();
				return true
			}
		},
		object : Task("sp") {
			override fun run(console: ConsoleOutput) : Boolean {
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
		},
		object : Task("ai settlements") {
		    override fun run(console: ConsoleOutput) : Boolean {
				theBestPlaceToBuildColony()
				return true
			}
		},
		object : Task("ai explore") {
			override fun run(console: ConsoleOutput) : Boolean {
				theBestMove()
				return true
			}
		},
		object : Task("ai move") {
			override fun run(console: ConsoleOutput) : Boolean {
				aiMove()
				return true
			}
		},
		object : Task("ai attack") {
			override fun run(console: ConsoleOutput) : Boolean {
        		ThreadsResources.instance.executeMovement(object : Runnable {
        			override fun run() {
        			    aiAttack()
        			}
        		})
				return true
			}
		},
		object : Task("pools") {
			override fun run(console: ConsoleOutput) : Boolean {
				console.addConsoleLine(PoolsStat.Stat.header())
				PoolsStat().readStats()
					.map { it.toFormatedString() }
					.forEach { console.addConsoleLine(it)}
				return false
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
	
	fun execute(cmd: String, console: ConsoleOutput) : Boolean {
	    var execTask = tasks
			.filter { t -> t.isMatchToExecute(cmd) }
			.singleOrNull()
		if (execTask != null) {
			return execTask.run(console)
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
			setOf(BuildColony.TileSelection.WITHOUT_UNEXPLORED)
			//setOf(BuildColony.TileSelection.ONLY_SEASIDE, BuildColony.TileSelection.WITHOUT_UNEXPLORED)
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
		
		ThreadsResources.instance.executeMovement(object : Runnable {
			override fun run() {
				AILogicDebugRun(di.guiGameModel, di.moveService, mapActor).run()
			}
		})
	}
	
	fun aiAttack() {
		var srcTile = guiGameModel.game.map.getSafeTile(12, 80)
		val mission = SeekAndDestroyMission(srcTile.units.first())
		
		val missionHandler = SeekAndDestroyMissionHandler(guiGameModel.game, di.moveService, di.combatService)
		missionHandler.handle(null, mission) 
	}
}