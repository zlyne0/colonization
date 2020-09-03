package promitech.colonization.screen.debug

import net.sf.freecol.common.model.IndianSettlement
import net.sf.freecol.common.model.Settlement
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.ai.missions.SeekAndDestroyMission
import net.sf.freecol.common.model.map.generator.MapGenerator
import net.sf.freecol.common.model.map.path.PathFinder
import net.sf.freecol.common.model.player.Player
import promitech.colonization.DI
import promitech.colonization.ai.MissionExecutorDebugRun
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
import net.sf.freecol.common.model.ai.missions.DemandTributeMission
import net.sf.freecol.common.model.player.Tension
import net.sf.freecol.common.model.UnitFactory
import net.sf.freecol.common.model.UnitType
import promitech.colonization.ai.MissionPlaner
import promitech.colonization.ai.MissionExecutor
import promitech.colonization.ai.goodsToSell.TransportGoodsToSellMissionPlaner
import net.sf.freecol.common.model.ai.missions.TransportGoodsToSellMission


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
		command("map_turn_range") {
			if (!di.guiGameModel.isViewMode() || mapActor == null) {
			    console.keepOpen() 
				console.out("no view mode")
			} else {
				mapTurnRange(di, mapActor)
			}			 
		}
    	
		command("ai_settlements") {
			theBestPlaceToBuildColony(guiGameModel, mapActor)
		}
    	
		command("ai_settlements_goods_score") {
			settlementsGoodsScore(di, guiGameModel)
		}
		
		command("ai_transport_goods_to_sell_mission_example") {
			ai_transport_goods_to_sell_mission_example(di, guiGameModel, mapActor)
		}
		
		command("ai_explore") {
			theBestMove(di, mapActor)
		} 
    	
		command("ai_move") {
			aiMove(di, mapActor)
		} 
    	
		command("player_as_ai") {
			if (mapActor != null) {
				playerAsAi(di, guiGameModel, mapActor)
			}
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
		
		commandArg("indian_demand_tribute") {
			indianDemandTributeExample(di, guiGameModel, mapActor)
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
    pathFinder.generateRangeMap(guiGameModel.game.map, unit.getTile(), unit, PathFinder.includeUnexploredTiles);
    
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

	fun mapTurnRange(di : DI, mapActor : MapActor) {
		var guiGameModel = di.guiGameModel
		if (!guiGameModel.isViewMode()) {
			return
		}
		
		val dutch = guiGameModel.game.players.getById("player:1")
		val galleon = UnitFactory.create(UnitType.GALLEON, dutch, mapActor.mapDrawModel().selectedTile)
		
	    var pathFinder = PathFinder()
	    pathFinder.generateRangeMap(guiGameModel.game.map, galleon.getTile(), galleon, PathFinder.excludeUnexploredTiles)
		
		val tileStrings = Array(guiGameModel.game.map.height, { Array(guiGameModel.game.map.width, {""}) })
		pathFinder.turnCostToStringArrays(tileStrings)
		mapActor.showTileDebugStrings(tileStrings);
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
		var pathFinder = PathFinder()
		// private attack
//		var srcTile = guiGameModel.game.map.getSafeTile(12, 80)
//		val mission = SeekAndDestroyMission(srcTile.units.first())
		
		// brave attack on dutch colony
//		var srcTile = guiGameModel.game.map.getSafeTile(20, 80)
//		val mission = SeekAndDestroyMission(srcTile.units.first())

		// brave attack on spanish colony
		var srcTile = di.guiGameModel.game.map.getSafeTile(27, 55)
		val mission = SeekAndDestroyMission(srcTile.units.first())
				
		val missionHandler = SeekAndDestroyMissionHandler(di.guiGameModel.game, di.moveService, di.combatService, pathFinder)
		missionHandler.handle(null, mission) 
	}

	fun indianBringGiftExample(di : DI, guiGameModel : GUIGameModel, mapActor : MapActor?) {
		val tile = guiGameModel.game.map.getSafeTile(19, 78)
		//val colonyTile = guiGameModel.game.map.getSafeTile(20, 79)
		val colonyTile = guiGameModel.game.map.getSafeTile(21, 72)
		
		val mission : IndianBringGiftMission
		
		val indianAiContainer = guiGameModel.game.aiContainer.missionContainer(tile.getSettlement().getOwner())
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
				MissionExecutorDebugRun(di.guiGameModel, di.moveService, mapActor, di.combatService, di.guiGameController, di.pathFinder)
					.runMission(tile.getSettlement().getOwner(), mission)
			}
		})
	}

	fun indianDemandTributeExample(di : DI, guiGameModel : GUIGameModel, mapActor : MapActor?) {
		val tile = guiGameModel.game.map.getSafeTile(19, 78)
		//val colonyTile = guiGameModel.game.map.getSafeTile(20, 79)
		val colonyTile = guiGameModel.game.map.getSafeTile(21, 72)
		
		val mission : DemandTributeMission
		
		val indianAiContainer = guiGameModel.game.aiContainer.missionContainer(tile.getSettlement().getOwner())
		if (!indianAiContainer.hasMissionType(DemandTributeMission::class.java)) {
			val unit = tile.getSettlement().asIndianSettlement().getUnits().getById("unit:6351")
			
			tile.getSettlement().asIndianSettlement().setTension(
				colonyTile.getSettlement().getOwner(),
				Tension.Level.DISPLEASED.getLimit()
			)
			
			mission = DemandTributeMission(
				tile.getSettlement().asIndianSettlement(),
				unit,
				colonyTile.getSettlement().asColony()
			)
			indianAiContainer.addMission(mission)
		} else {
			mission = indianAiContainer.firstMissionByType(DemandTributeMission::class.java)
		}
		mission.getUnitToDemandTribute().resetMovesLeftOnNewTurn()
		
		ThreadsResources.instance.executeMovement(object : Runnable {
			override fun run() {
				MissionExecutorDebugRun(di.guiGameModel, di.moveService, mapActor, di.combatService, di.guiGameController, di.pathFinder)
					.runMission(tile.getSettlement().getOwner(), mission)
			}
		})
	}

	fun playerAsAi(di : DI, guiGameModel : GUIGameModel, mapActor : MapActor) {
		val missionPlaner = MissionPlaner(guiGameModel.game, di.pathFinder)
		val missionExecutor = MissionExecutor(
			guiGameModel.game,
			di.moveService,
			di.combatService,
			di.guiGameController,
			di.pathFinder
		)
		val player = guiGameModel.game.playingPlayer

		ThreadsResources.instance.executeMovement(object : Runnable {
			override fun run() {
				di.newTurnService.newTurn(player)
				
				missionPlaner.planMissions(player)
				missionExecutor.executeMissions(player)
				
				mapActor.resetMapModel()
				mapActor.resetUnexploredBorders()
			}
		})
				
	}

	fun settlementsGoodsScore(di : DI, guiGameModel : GUIGameModel) {
		val planer = TransportGoodsToSellMissionPlaner(guiGameModel.game, di.pathFinder)
		planer.plan(guiGameModel.game.playingPlayer)
	}

	fun ai_transport_goods_to_sell_mission_example(di : DI, guiGameModel : GUIGameModel, mapActor : MapActor?) {
		val player = guiGameModel.game.players.getById("player:1")
		val transporter = player.units.getById("unit:938")
		
		val mission = TransportGoodsToSellMission(
			transporter,
			player.settlements.getById("colony:1182"),
			setOf("colony:1182", "colony:1063")
		)
		
		// move cargo to europe to sell			
//		transporter.getGoodsContainer().increaseGoodsQuantity("model.goods.cloth", 200)
//		transporter.changeUnitLocation(guiGameModel.game.map.getSafeTile(27, 21))
//		mission.changePhase(TransportGoodsToSellMission.Phase.MOVE_TO_EUROPE)
		
		// move from europe to colony to load cargo
		player.settlements.getById("colony:1182").getGoodsContainer().decreaseAllToZero()
		player.settlements.getById("colony:1063").getGoodsContainer()
			.increaseGoodsQuantity("model.goods.rum", 100)
			.increaseGoodsQuantity("model.goods.cigars", 100)
			.increaseGoodsQuantity("model.goods.silver", 100)
			.increaseGoodsQuantity("model.goods.cloth", 100)
		transporter.changeUnitLocation(player.getEurope())
		
		val missionContainer = guiGameModel.game.aiContainer.missionContainer(player)
		missionContainer.addMission(mission)
		
		ThreadsResources.instance.executeMovement(object : Runnable {
			override fun run() {
				MissionExecutorDebugRun(di.guiGameModel, di.moveService, mapActor, di.combatService, di.guiGameController, di.pathFinder)
					.runMission(player, mission)
			}
		})
	}

