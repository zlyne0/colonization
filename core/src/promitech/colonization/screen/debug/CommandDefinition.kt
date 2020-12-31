package promitech.colonization.screen.debug

import net.sf.freecol.common.model.*
import net.sf.freecol.common.model.ai.ColonyPlan
import net.sf.freecol.common.model.ai.missions.SeekAndDestroyMission
import net.sf.freecol.common.model.ai.missions.TransportUnitMission
import net.sf.freecol.common.model.ai.missions.goodsToSell.TransportGoodsToSellMission
import net.sf.freecol.common.model.ai.missions.goodsToSell.TransportGoodsToSellMissionPlaner
import net.sf.freecol.common.model.ai.missions.indian.DemandTributeMission
import net.sf.freecol.common.model.ai.missions.indian.IndianBringGiftMission
import net.sf.freecol.common.model.map.generator.MapGenerator
import net.sf.freecol.common.model.map.generator.SmoothingTileTypes
import net.sf.freecol.common.model.map.path.PathFinder
import net.sf.freecol.common.model.player.Player
import net.sf.freecol.common.model.player.Tension
import net.sf.freecol.common.model.specification.AbstractGoods
import promitech.colonization.DI
import promitech.colonization.ai.*
import promitech.colonization.infrastructure.ThreadsResources
import promitech.colonization.screen.colony.ColonyApplicationScreen
import promitech.colonization.screen.ff.ContinentalCongress
import promitech.colonization.screen.map.MapActor
import promitech.colonization.screen.map.hud.DiplomacyContactDialog
import promitech.colonization.screen.map.hud.GUIGameModel
import net.sf.freecol.common.model.ai.missions.workerrequest.ColonyWorkerRequestPlaner
import net.sf.freecol.common.model.ai.missions.buildcolony.ColonyPlaceGenerator


fun createCommands(
		di: DI, console: ConsoleOutput,
		mapActor: MapActor?,
		colonyApplicationScreen: ColonyApplicationScreen?
) : Commands {
	val guiGameModel = di.guiGameModel
	val gameController = di.guiGameController
	val tileDebugView = TileDebugView(mapActor, guiGameModel)
	
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

		command("ai_generateTileScoresForNewColony") {
			generateTileScoresForNewColony(guiGameModel, tileDebugView)
		}
		
		command("ai_generateTheBestPlaceToBuildColony") {
			generateTheBestPlaceToBuildColony(guiGameModel, tileDebugView)
		}

		command("ai_generateWorkerReqScore") {
			generateWorkerReqScore(di, guiGameModel, tileDebugView)
		}

		command("ai_settlements_goods_score") {
			settlementsGoodsScore(di, guiGameModel)
		}
						
		command("ai_transport_goods_to_sell_mission_example") {
			ai_transport_goods_to_sell_mission_example(di, guiGameModel, mapActor)
		}
		
		command("aiTransportUnitsFromEuropeToNewWorld") {
			if (mapActor != null) {
				aiTransportUnitsFromEuropeToNewWorld(di, guiGameModel, mapActor)
			}
		}
		
		command("ai_explore") {
			theBestMove(di, mapActor)
		} 
    	
		command("player_turn_as_ai") {
			if (mapActor != null) {
				playerTurnAsAi(di, guiGameModel, mapActor)
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
		
		commandArg("reset_debug") {
			resetDebug(tileDebugView)
		}
		
    	command("nothing") {
		}
	}
}

fun generateTheBestPlaceToBuildColony(guiGameModel: GUIGameModel, tileDebugView: TileDebugView) {
	tileDebugView.reset()
	
	val player = guiGameModel.game.playingPlayer
	val pathFinder = PathFinder()
	
	val rangeSource = guiGameModel.game.map.getSafeTile(player.getEntryLocation());
	val galleon = UnitFactory.create(UnitType.GALLEON, player, rangeSource)
		
	val colonyPlaceGenerator = ColonyPlaceGenerator(pathFinder, guiGameModel.game)
	colonyPlaceGenerator.theBestTiles(galleon, rangeSource)
	colonyPlaceGenerator.theBestTilesWeight(tileDebugView)
	
	player.removeUnit(galleon)
}

fun generateTileScoresForNewColony(guiGameModel: GUIGameModel, tileDebugView: TileDebugView) {
	tileDebugView.reset()
	
	var pathFinder = PathFinder()
	
	val colonyPlaceGenerator = ColonyPlaceGenerator(pathFinder, guiGameModel.game)
	colonyPlaceGenerator.generateWeights(guiGameModel.game.playingPlayer)
	colonyPlaceGenerator.tilesWeights(tileDebugView)
}

fun generateWorkerReqScore(di: DI, guiGameModel: GUIGameModel, tileDebugView: TileDebugView) {
	tileDebugView.reset()
	
	val player = guiGameModel.game.playingPlayer
	val sut = ColonyWorkerRequestPlaner(guiGameModel.game, di.pathFinder)
	sut.score(player, Units.findCarrier(player))
	sut.debug(tileDebugView)
}


fun theBestMove(di: DI, mapActor: MapActor?) {
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
    
	val tileStrings = Array(guiGameModel.game.map.height, { Array(guiGameModel.game.map.width, { "" }) })
    navyExplorer.toStringsBorderValues(tileStrings);
    mapActor?.showTileDebugStrings(tileStrings);
}

	fun mapTurnRange(di: DI, mapActor: MapActor) {
		var guiGameModel = di.guiGameModel
		if (!guiGameModel.isViewMode()) {
			return
		}
		
		val dutch = guiGameModel.game.players.getById("player:1")
		val galleon = UnitFactory.create(UnitType.GALLEON, dutch, mapActor.mapDrawModel().selectedTile)
		
	    var pathFinder = PathFinder()
	    pathFinder.generateRangeMap(guiGameModel.game.map, galleon.getTile(), galleon, PathFinder.excludeUnexploredTiles)
		
		val tileStrings = Array(guiGameModel.game.map.height, { Array(guiGameModel.game.map.width, { "" }) })
		pathFinder.turnCostToStringArrays(tileStrings)
		mapActor.showTileDebugStrings(tileStrings);
	}

	fun aiAttack(di: DI) {
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

	fun indianBringGiftExample(di: DI, guiGameModel: GUIGameModel, mapActor: MapActor?) {
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

	fun indianDemandTributeExample(di: DI, guiGameModel: GUIGameModel, mapActor: MapActor?) {
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

	fun playerTurnAsAi(di: DI, guiGameModel: GUIGameModel, mapActor: MapActor) {
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

	fun settlementsGoodsScore(di: DI, guiGameModel: GUIGameModel) {
		val planer = TransportGoodsToSellMissionPlaner(guiGameModel.game, di.pathFinder)
		planer.plan(guiGameModel.game.playingPlayer)
	}

	fun ai_transport_goods_to_sell_mission_example(di: DI, guiGameModel: GUIGameModel, mapActor: MapActor?) {
		val game = guiGameModel.game
		
		val dutch = game.players.getById("player:1")
		val spain = game.players.getById("player:133")
		val startLocation = game.map.getTile(26, 79)
		val transporter = UnitFactory.create(UnitType.GALLEON, dutch, startLocation)
		
		val fortOranje = game.map.getTile(25, 75).getSettlement()
		val nieuwAmsterdam = game.map.getTile(24, 78).getSettlement()
		
		fortOranje.getGoodsContainer().decreaseAllToZero()
		fortOranje.getGoodsContainer()
			.increaseGoodsQuantity("model.goods.rum", 100)
		nieuwAmsterdam.getGoodsContainer()
			.increaseGoodsQuantity("model.goods.cigars", 100)
			.increaseGoodsQuantity("model.goods.silver", 100)
			.increaseGoodsQuantity("model.goods.cloth", 100)

		val mission = TransportGoodsToSellMission(
			transporter,
			fortOranje.asColony(),
			setOf(fortOranje.getId(), nieuwAmsterdam.getId())
		)
		
		val missionContainer = game.aiContainer.missionContainer(dutch)
		missionContainer.addMission(mission)

		fortOranje.owner = spain;

		dutch.fogOfWar.resetFogOfWar(guiGameModel.game, dutch)
		mapActor?.resetMapModel()
		mapActor?.resetUnexploredBorders()
	}

	fun aiTransportUnitsFromEuropeToNewWorld(di: DI, guiGameModel: GUIGameModel, mapActor: MapActor) {
		val game = guiGameModel.game
		
		val dutch = game.players.getById("player:1")				
        var sourceTile = game.map.getTile(26, 79)
        //var sourceTile = game.map.getTile(30, 80)
        //var sourceTile = game.map.getTile(28, 76)
		
        var disembarkTile = game.map.getTile(27, 76)
        var fortOrangeTile = game.map.getTile(25, 75)

        var galleon = UnitFactory.create(UnitType.GALLEON, dutch, sourceTile);
        var u1 = UnitFactory.create(UnitType.FREE_COLONIST, dutch, dutch.getEurope());
        var u2 = UnitFactory.create(UnitType.FREE_COLONIST, dutch, dutch.getEurope());
//        var u1 = UnitFactory.create(UnitType.FREE_COLONIST, dutch, galleon);
//        var u2 = UnitFactory.create(UnitType.FREE_COLONIST, dutch, galleon);

        var transportMission = TransportUnitMission(galleon)
            .addUnitDest(u1, fortOrangeTile)
            .addUnitDest(u2, disembarkTile);

        game.aiContainer.missionContainer(dutch).addMission(transportMission);

		//UnitFactory.create(UnitType.FREE_COLONIST, game.players.getById("player:133"), disembarkTile);
		//fortOrangeTile.getSettlement().setOwner(game.players.getById("player:133"));

		dutch.fogOfWar.resetFogOfWar(guiGameModel.game, dutch);				
		mapActor.resetMapModel()
		mapActor.resetUnexploredBorders()
	}

	fun resetDebug(tileDebugView: TileDebugView) {
		tileDebugView.reset()
	}
