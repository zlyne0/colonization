package promitech.colonization.screen.debug

import net.sf.freecol.common.model.IndianSettlement
import net.sf.freecol.common.model.Settlement
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.ai.missions.SeekAndDestroyMission
import net.sf.freecol.common.model.map.generator.MapGenerator
import net.sf.freecol.common.model.map.path.PathFinder
import net.sf.freecol.common.model.player.Player
import promitech.colonization.DI
import promitech.colonization.ai.AILogicDebugRun
import promitech.colonization.ai.BuildColony
import promitech.colonization.ai.NavyExplorer
import promitech.colonization.ai.SeekAndDestroyMissionHandler
import promitech.colonization.infrastructure.ThreadsResources
import promitech.colonization.screen.ff.ContinentalCongress
import promitech.colonization.screen.map.MapActor
import promitech.colonization.screen.map.hud.DiplomacyContactDialog
import promitech.colonization.screen.map.hud.GUIGameModel
import net.sf.freecol.common.model.TradeRoute
import promitech.colonization.ai.TradeRouteMissionHandler
import promitech.colonization.orders.move.MoveInThreadService
import promitech.colonization.orders.move.MoveService.AfterMoveProcessor
import net.sf.freecol.common.util.Consumer
import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.map.generator.SmoothingTileTypes
import net.sf.freecol.common.model.ai.ColonyPlan
import net.sf.freecol.common.model.specification.GoodsType
import promitech.colonization.screen.colony.ColonyApplicationScreen
import net.sf.freecol.common.model.ai.missions.IndianBringGiftMission
import net.sf.freecol.common.model.specification.AbstractGoods


fun createCommands(
	di : DI, console : ConsoleOutput,
	mapActor: MapActor?,
	colonyApplicationScreen: ColonyApplicationScreen?
) : Commands {
	val guiGameModel = di.guiGameModel
	val gameController = di.guiGameController
	
	return Commands().define {
    	commandArg("add_gold") { args ->
			if (args.size == 1) {
				console.keepOpen()
				console.out("no args with gold amount")
			} else {
				val gold = args[1].toInt()
				guiGameModel.game.playingPlayer.addGold(gold)
				console.out("add $gold")
			}
    	}
    	command("map_show") {
		    guiGameModel.game.playingPlayer.explorAllTiles();
		    guiGameModel.game.playingPlayer.fogOfWar.removeFogOfWar();
		    gameController.resetMapModel();
    	}
    	command("map_generate") {
            guiGameModel.game.map = MapGenerator().generate(guiGameModel.game.players)
            gameController.resetMapModel()
    	}
		command("map_smooth_tile_types") {
			val smoothing = SmoothingTileTypes(guiGameModel.game.map, Specification.instance.tileTypes)
			smoothing.smoothing(1)
			gameController.resetMapModel()
		}
    	command("map_show_owners") {
			gameController.showTilesOwners()
		} 
    	command("map_hide_owners") {
			gameController.hideTilesOwners()
		}
    	command("map_scout_all") {
			for (player : Player in guiGameModel.game.players.entities()) {
				if (player.isIndian()) {
					for (indianSettlement : Settlement in player.settlements.entities()) {
						if (indianSettlement is IndianSettlement) {
							indianSettlement.scoutBy(guiGameModel.game.playingPlayer)
						}
					}
				}
			}
		}
    	
		command("ai_settlements") {
			theBestPlaceToBuildColony(guiGameModel, mapActor)
		}
    	
		command("ai_explore") {
			theBestMove(di, mapActor)
		} 
    	
		command("ai_move") {
			aiMove(di, mapActor)
		} 
    	
		command("pools") {
		    console.keepOpen() 
			console.out(PoolsStat.Stat.header())
			PoolsStat().readStats()
				.map { it.toFormatedString() }
				.forEach { console.out(it) }
    	} 
    	
		command("firstContactDialog") {
			val player = guiGameModel.game.players.getById("player:1")
			//val contactPlayer = guiGameModel.game.players.getById("player:9")
			val contactPlayer = guiGameModel.game.players.getById("player:133")
			
			//gameController.showDialog(FirstContactDialog(guiGameModel.game.playingPlayer, contactPlayer))
			if (mapActor != null) {
				gameController.showDialog(DiplomacyContactDialog(mapActor, guiGameModel.game, player, contactPlayer))
			}
		}
    	
		command("add_liberty_for_founding_father") {
			guiGameModel.game.playingPlayer.foundingFathers.fullFillLibertyForFoundingFather()
		}
		
		commandArg("add_foundingFather") { args ->
		    val player = guiGameModel.game.playingPlayer
			val ff = Specification.instance.foundingFathers.getById(args[1])
			player.addFoundingFathers(guiGameModel.game, ff)
			gameController.resetMapModel()
		}.addParams {
			listOf<String>("model.foundingFather.adamSmith", "model.foundingFather.jacobFugger", "model.foundingFather.peterMinuit")
		}
		command("add_all_founding_fathers") {
			val player = guiGameModel.game.playingPlayer
			for (ff in Specification.instance.foundingFathers.entities()) {
				player.addFoundingFathers(guiGameModel.game, ff)
			}
			gameController.resetMapModel();
		}
		 
		command("add_immigration") {
			var player = guiGameModel.game.playingPlayer
			player.modifyImmigration(player.getImmigrationRequired() + 1)
			player.getEurope().handleImmigrationOnNewTurn()
		}
		
    	command("add_conversion") {
			var tile = guiGameModel.game.map.getTile(19, 78)
			tile.getSettlement().asIndianSettlement().setConvertProgress(100)
    	}
    	
		command("show_continental_congress") {
			gameController.showDialog(ContinentalCongress(guiGameModel.game.playingPlayer))
		}
		
		commandArg("colony_plan") { args ->
			if (colonyApplicationScreen == null) {
				console.keepOpen()
					.out("no colony selected")
			} else {
				var colony = colonyApplicationScreen.getColony()
				
				ColonyPlan(colonyApplicationScreen.getColony())
					.withConsumeWarehouseResources(true)
					.execute2(ColonyPlan.Plan.of(args[1]))
				
				colonyApplicationScreen.initColony(colony)
			}
		}.addParams {
			listOf<String>(
				ColonyPlan.Plan.MostValuable.name.toLowerCase(),
				ColonyPlan.Plan.Bell.name.toLowerCase(),
				ColonyPlan.Plan.Food.name.toLowerCase(),
				ColonyPlan.Plan.Building.name.toLowerCase(),
				ColonyPlan.Plan.Muskets.name.toLowerCase()
			)
		}
		
		commandArg("indian_bring_gift") {
			indianBringGiftExample(di, guiGameModel, mapActor)
		}
		
    	command("nothing") {
		}
	}
}

fun theBestPlaceToBuildColony(guiGameModel : GUIGameModel, mapActor : MapActor?) {
    System.out.println("theBestPlaceToBuildColony")
    
    var buildColony = BuildColony(guiGameModel.game.map);
    buildColony.generateWeights(
        guiGameModel.game.playingPlayer,
        setOf(BuildColony.TileSelection.WITHOUT_UNEXPLORED)
        //setOf(BuildColony.TileSelection.ONLY_SEASIDE, BuildColony.TileSelection.WITHOUT_UNEXPLORED)
    );
    
    val tileStrings = Array(guiGameModel.game.map.height, { Array(guiGameModel.game.map.width, {""}) })
    buildColony.toStringValues(tileStrings)
    mapActor?.showTileDebugStrings(tileStrings)
}

fun theBestMove(di : DI, mapActor : MapActor?) {
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
    mapActor?.showTileDebugStrings(tileStrings);
}

	fun aiMove(di : DI, mapActor : MapActor?) {
		if (di.guiGameModel.isActiveUnitNotSet()) {
			System.out.println("no active unit");
			return
		}
		
//		ThreadsResources.instance.executeMovement(object : Runnable {
//			override fun run() {
//				AILogicDebugRun(di.guiGameModel, di.moveService, mapActor, di.combatService).run()
//			}
//		})
	}
	
	fun aiAttack(di : DI) {
		// private attack
//		var srcTile = guiGameModel.game.map.getSafeTile(12, 80)
//		val mission = SeekAndDestroyMission(srcTile.units.first())
		
		// brave attack on dutch colony
//		var srcTile = guiGameModel.game.map.getSafeTile(20, 80)
//		val mission = SeekAndDestroyMission(srcTile.units.first())

		// brave attack on spanish colony
		var srcTile = di.guiGameModel.game.map.getSafeTile(27, 55)
		val mission = SeekAndDestroyMission(srcTile.units.first())
				
		val missionHandler = SeekAndDestroyMissionHandler(di.guiGameModel.game, di.moveService, di.combatService)
		missionHandler.handle(null, mission) 
	}

	fun indianBringGiftExample(di : DI, guiGameModel : GUIGameModel, mapActor : MapActor?) {
		val tile = guiGameModel.game.map.getSafeTile(19, 78)
		//val colonyTile = guiGameModel.game.map.getSafeTile(20, 79)
		val colonyTile = guiGameModel.game.map.getSafeTile(21, 72)
		
		val mission : IndianBringGiftMission
		
		val indianAiContainer = guiGameModel.game.aiContainer.getMissionContainer(tile.getSettlement().getOwner())
		if (!indianAiContainer.hasMissionType(IndianBringGiftMission::class.java)) {
			val transportUnit = tile.getSettlement().asIndianSettlement().getUnits().getById("unit:6351")
			mission = IndianBringGiftMission(
				tile.getSettlement().asIndianSettlement(), colonyTile.getSettlement().asColony(),
				transportUnit, AbstractGoods("model.goods.tobacco", 77)
			)
			indianAiContainer.addMission(mission)
		} else {
			mission = indianAiContainer.firstMissionByType(IndianBringGiftMission::class.java)
		}
		mission.getTransportUnit().resetMovesLeftOnNewTurn()
		
		ThreadsResources.instance.executeMovement(object : Runnable {
			override fun run() {
				AILogicDebugRun(di.guiGameModel, di.moveService, mapActor, di.combatService, di.guiGameController)
					.runMission(tile.getSettlement().getOwner(), mission)
			}
		})
	}
