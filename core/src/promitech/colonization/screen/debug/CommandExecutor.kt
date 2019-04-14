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
import promitech.colonization.screen.map.hud.FirstContactDialog
import promitech.colonization.screen.map.hud.DiplomacyContactDialog
import net.sf.freecol.common.model.player.Player
import net.sf.freecol.common.model.Settlement
import net.sf.freecol.common.model.IndianSettlement
import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.UnitRole
import promitech.colonization.orders.diplomacy.FirstContactService
import promitech.colonization.orders.move.MoveContext
import promitech.colonization.screen.ChooseFoundingFatherDialog
import promitech.colonization.screen.FoundingFatherService
import net.sf.freecol.common.model.player.RecruitFoundingFatherNotification
import net.sf.freecol.common.model.player.FoundingFather
import promitech.colonization.screen.ContinentalCongress

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
    val gameController = di.guiGameController
	var guiGameModel = di.guiGameModel
	val firstContactService = FirstContactService(di.firstContactController, guiGameModel)
			
	var tasks : List<Task> = mutableListOf<Task>(
		object : Task("add gold") {
			override fun run(console: ConsoleOutput) : Boolean {
			    guiGameModel.game.playingPlayer.addGold(5000)
				return true
			}
		},
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
		object : Task("map scout all") {
			override fun run(console: ConsoleOutput): Boolean {
				for (player : Player in guiGameModel.game.players.entities()) {
					if (player.isIndian()) {
						for (indianSettlement : Settlement in player.settlements.entities()) {
							if (indianSettlement is IndianSettlement) {
								indianSettlement.scoutBy(guiGameModel.game.playingPlayer)
							}
						}
					}
				}
				return true
			}
		},
		object : Task("map hide owners") {
			override fun run(console: ConsoleOutput) : Boolean {
			    gameController.hideTilesOwners();
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
		},
		object : Task("firstContactDialog") {
			override fun run(console: ConsoleOutput) : Boolean {
				val player = guiGameModel.game.players.getById("player:1")
				//val contactPlayer = guiGameModel.game.players.getById("player:9")
				val contactPlayer = guiGameModel.game.players.getById("player:133")
				
				//gameController.showDialog(FirstContactDialog(guiGameModel.game.playingPlayer, contactPlayer))
				gameController.showDialog(DiplomacyContactDialog(mapActor, guiGameModel.game, player, contactPlayer))
				return true
			}
		},
		object : Task("foundingFather") {
			override fun run(console: ConsoleOutput): Boolean {
				val player = guiGameModel.game.playingPlayer
				player.modifyLiberty(20)
//				player.addFoundingFathers(Specification.instance.foundingFathers.getById(FoundingFather.FERDINAND_MAGELLAN))
//				player.addFoundingFathers(Specification.instance.foundingFathers.getById(FoundingFather.PETER_MINUIT))
//				player.addFoundingFathers(Specification.instance.foundingFathers.getById(FoundingFather.PETER_STUYVESANT))
//				player.addFoundingFathers(guiGameModel.game,
//					Specification.instance.foundingFathers.getById("model.foundingFather.hernandoDeSoto")
//				)
//				player.addFoundingFathers(guiGameModel.game,
//					Specification.instance.foundingFathers.getById("model.foundingFather.henryHudson")
//				)
				player.setTax(50)
				player.addFoundingFathers(guiGameModel.game,
					Specification.instance.foundingFathers.getById("model.foundingFather.bartolomeDeLasCasas")
				)
				//gameController.showDialog(ChooseFoundingFatherDialog(player, guiGameModel.game.turn))
				//FoundingFatherService().checkFoundingFathers(player)
				//gameController.showDialog(ChooseFoundingFatherDialog(player, guiGameModel.game.turn))
				//player.eventsNotifications.addMessageNotification(RecruitFoundingFatherNotification())
				gameController.resetMapModel();
				return true
			}
		},
		object : Task("add immigration") {
			override fun run(console: ConsoleOutput): Boolean {
				var player = guiGameModel.game.playingPlayer
				player.modifyImmigration(player.getImmigrationRequired() + 1)
				player.getEurope().handleImmigrationOnNewTurn()
				return true
			}
		},
		object : Task("add conversion") {
			override fun run(console: ConsoleOutput): Boolean {
				var tile = guiGameModel.game.map.getTile(19, 78)
				tile.getSettlement().getIndianSettlement().setConvertProgress(100)
				return true
			}
		},
		object : Task("show continental congress") {
			override fun run(console: ConsoleOutput): Boolean {
				gameController.showDialog(ContinentalCongress(guiGameModel.game.playingPlayer))
				return true
			}
		},
		object : Task("add all founding fathers") {
			override fun run(console: ConsoleOutput): Boolean {
				val player = guiGameModel.game.playingPlayer
				for (ff in Specification.instance.foundingFathers.entities()) {
					player.addFoundingFathers(guiGameModel.game, ff)
				}
				gameController.resetMapModel();
				return true
			}
		}
	);

	init {
		tasks = tasks.plus(createAlias("p", "pools"))
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
		// private attack
//		var srcTile = guiGameModel.game.map.getSafeTile(12, 80)
//		val mission = SeekAndDestroyMission(srcTile.units.first())
		
		// brave attack on dutch colony
//		var srcTile = guiGameModel.game.map.getSafeTile(20, 80)
//		val mission = SeekAndDestroyMission(srcTile.units.first())

		// brave attack on spanish colony
		var srcTile = guiGameModel.game.map.getSafeTile(27, 55)
		val mission = SeekAndDestroyMission(srcTile.units.first())
				
		val missionHandler = SeekAndDestroyMissionHandler(guiGameModel.game, di.moveService, di.combatService)
		missionHandler.handle(null, mission) 
	}
}
