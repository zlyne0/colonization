package promitech.colonization.screen.debug

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.IndianSettlement
import net.sf.freecol.common.model.Settlement
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.TileImprovementType
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.UnitFactory
import net.sf.freecol.common.model.UnitRole
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.ai.missions.SeekAndDestroyMission
import net.sf.freecol.common.model.ai.missions.TransportUnitMission
import net.sf.freecol.common.model.ai.missions.foreachMission
import net.sf.freecol.common.model.ai.missions.goodsToSell.ColoniesProductionValue
import net.sf.freecol.common.model.ai.missions.goodsToSell.TransportGoodsToSellMission
import net.sf.freecol.common.model.ai.missions.indian.DemandTributeMission
import net.sf.freecol.common.model.ai.missions.indian.IndianBringGiftMission
import net.sf.freecol.common.model.ai.missions.pioneer.AddImprovementPolicy
import net.sf.freecol.common.model.ai.missions.pioneer.PioneerDestination
import net.sf.freecol.common.model.ai.missions.pioneer.PioneerMission
import net.sf.freecol.common.model.ai.missions.pioneer.PioneerMissionPlaner
import net.sf.freecol.common.model.ai.missions.pioneer.ReplaceColonyWorkerMission
import net.sf.freecol.common.model.ai.missions.pioneer.TakeRoleEquipmentMission
import net.sf.freecol.common.model.ai.missions.scout.ScoutMission
import net.sf.freecol.common.model.ai.missions.scout.ScoutMissionPlaner
import net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission
import net.sf.freecol.common.model.ai.missions.workerrequest.ColonistsPurchaseRecommendations
import net.sf.freecol.common.model.ai.missions.workerrequest.ColonyWorkerMission
import net.sf.freecol.common.model.ai.missions.workerrequest.ColonyWorkerRequestPlaceCalculator
import net.sf.freecol.common.model.ai.missions.workerrequest.EntryPointTurnRange
import net.sf.freecol.common.model.ai.missions.workerrequest.ScorePolicy
import net.sf.freecol.common.model.colonyproduction.ColonyPlan
import net.sf.freecol.common.model.colonyproduction.GoodsCollection
import net.sf.freecol.common.model.map.generator.MapGenerator
import net.sf.freecol.common.model.map.generator.SmoothingTileTypes
import net.sf.freecol.common.model.map.path.PathFinder
import net.sf.freecol.common.model.player.Player
import net.sf.freecol.common.model.player.Tension
import net.sf.freecol.common.model.specification.AbstractGoods
import net.sf.freecol.common.model.specification.GoodsType
import net.sf.freecol.common.model.specification.GoodsTypeId
import promitech.colonization.DI
import promitech.colonization.Direction
import promitech.colonization.ai.MissionExecutor
import promitech.colonization.ai.MissionExecutorDebugRun
import promitech.colonization.ai.MissionPlaner
import promitech.colonization.ai.NavyExplorer
import promitech.colonization.ai.SeekAndDestroyMissionHandler
import promitech.colonization.ai.UnitTypeId
import promitech.colonization.ai.findCarrier
import promitech.colonization.infrastructure.ThreadsResources
import promitech.colonization.savegame.SaveGameList
import promitech.colonization.screen.colony.ColonyApplicationScreen
import promitech.colonization.screen.ff.ContinentalCongress
import promitech.colonization.screen.map.MapActor
import promitech.colonization.screen.map.hud.DiplomacyContactDialog
import promitech.colonization.screen.map.hud.GUIGameModel

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
				mapTurnRange(di, mapActor, tileDebugView)
			}			 
		}

		command("ai_generateTileScoresForNewColony") {
			generateTileScoresForNewColony(di, guiGameModel, tileDebugView)
		}
		
		command("ai_generateTheBestPlaceToBuildColony") {
			generateTheBestPlaceToBuildColony(di, guiGameModel, tileDebugView)
		}

		command("ai_generateWorkerReqScoreByValue") {
			generateWorkerReqScoreByValue(di, guiGameModel, tileDebugView)
		}

		command("ai_generateWorkerReqScoreByPriceToValue") {
			generateWorkerReqScoreByPriceToValue(di, guiGameModel, tileDebugView)
		}

		command("ai_generateWorkerReqBuyRecommendations") {
			generateWorkerReqBuyRecommendations(di, guiGameModel, tileDebugView)
		}

		command("ai_transport_goods_to_sell_mission_example") {
			ai_transport_goods_to_sell_mission_example(guiGameModel, mapActor)
		}
		
		command("ai_explore") {
			aiExplore(di, tileDebugView)
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
				colony.updateProductionToMaxPossible(colony.tile)

				val plan = ColonyPlan.Plan.valueOf(args[1])
				ColonyPlan(colonyApplicationScreen.colony)
					.withConsumeWarehouseResources(true)
					.withIgnoreIndianOwner()
					.execute(plan)

				colonyApplicationScreen.initColony(colony)
			}
		}.addParams {
			listOf<String>(
				"food",
				"bell",
				"building",
				"mostvaluable",
				"tools",
				"muskets"
			)
		}

		commandArg("colonies_production_value") {
			val coloniesProductionValue = ColoniesProductionValue(guiGameModel.game.playingPlayer)
			val gold = coloniesProductionValue.goldValue()
			print(String.format("player[%s] colonies production gold value %s", guiGameModel.game.playingPlayer.id, gold))
		}

		commandArg("indian_bring_gift") {
			indianBringGiftExample(di, guiGameModel, mapActor)
		}
		
		commandArg("indian_demand_tribute") {
			indianDemandTributeExample(di, guiGameModel, mapActor)
		}

		commandArg("show_missions") {
			showMissions(guiGameModel, tileDebugView)
		}

		commandArg("clear_all_missions") {
			val missionContainer = guiGameModel.game.aiContainer.missionContainer(guiGameModel.game.playingPlayer)
			missionContainer.clearAllMissions()
			println("cleat all missions")
		}

		commandArg("reset_debug") {
			resetDebug(tileDebugView)
		}

		command("simpleTest") {
			simpleTest(di, guiGameModel, tileDebugView, mapActor)
		}

		command("simpleTest2") {
			simpleTest2(di, guiGameModel, tileDebugView, mapActor)
		}

    	command("nothing") {
		}
	}
}

fun generateTheBestPlaceToBuildColony(di: DI, guiGameModel: GUIGameModel, tileDebugView: TileDebugView) {
	tileDebugView.reset()

	val player = guiGameModel.game.playingPlayer
	val transportUnit = player.findCarrier()
	val sut = ColonyWorkerRequestPlaceCalculator(player, guiGameModel.game.map, EntryPointTurnRange(guiGameModel.game.map, di.pathFinder, player, transportUnit))

	val missionContainer = guiGameModel.game.aiContainer.missionContainer(player)
	val createColonyTiles = missionContainer.findMissions(ColonyWorkerMission::class.java).map { it -> it.tile }
	sut.debugTheBestBlaceToBuildColony(tileDebugView, createColonyTiles)
}

fun generateTileScoresForNewColony(di: DI, guiGameModel: GUIGameModel, tileDebugView: TileDebugView) {
	tileDebugView.reset()

	val player = guiGameModel.game.playingPlayer
	val transportUnit = player.findCarrier()
	val entryPointTurnRange = EntryPointTurnRange(guiGameModel.game.map, di.pathFinder, player, transportUnit)
	val sut = ColonyWorkerRequestPlaceCalculator(player, guiGameModel.game.map, entryPointTurnRange)

	sut.debugGenerateTileScoresForNewColony(tileDebugView)
}

fun generateWorkerReqScoreByValue(di: DI, guiGameModel: GUIGameModel, tileDebugView: TileDebugView) {
	tileDebugView.reset()

	val player = guiGameModel.game.playingPlayer
	val transportUnit = player.findCarrier()
	val entryPointTurnRange = EntryPointTurnRange(guiGameModel.game.map, di.pathFinder, player, transportUnit)

	val sut = ColonyWorkerRequestPlaceCalculator(player, guiGameModel.game.map, entryPointTurnRange)
	val colonyWorkerRequestScores = sut.score(PlayerMissionsContainer(player))

	colonyWorkerRequestScores.prettyPrint()

	val scorePolicy = ScorePolicy.WorkerProductionValue(entryPointTurnRange)
	scorePolicy.calculateScore(colonyWorkerRequestScores)

	colonyWorkerRequestScores.prettyPrint()

	sut.debug(tileDebugView)
}

fun generateWorkerReqScoreByPriceToValue(di: DI, guiGameModel: GUIGameModel, tileDebugView: TileDebugView) {
	tileDebugView.reset()

	val player = guiGameModel.game.playingPlayer
	val transportUnit = player.findCarrier()
	val entryPointTurnRange = EntryPointTurnRange(guiGameModel.game.map, di.pathFinder, player, transportUnit)
	val sut = ColonyWorkerRequestPlaceCalculator(player, guiGameModel.game.map, entryPointTurnRange)

	// create new player mission container to ignore actual mission destination
	val colonyWorkerRequestScores = sut.score(PlayerMissionsContainer(player))

	val scorePolicy = ScorePolicy.WorkerPriceToValue(entryPointTurnRange, player)
	scorePolicy.calculateScore(colonyWorkerRequestScores)

	sut.debug(tileDebugView)
}

fun generateWorkerReqBuyRecommendations(di: DI, guiGameModel: GUIGameModel, tileDebugView: TileDebugView) {
	tileDebugView.reset()

	val player = guiGameModel.game.playingPlayer
	val playerMissionContainer = guiGameModel.game.aiContainer.missionContainer(player)
	val transportUnit = player.findCarrier()
	val entryPointTurnRange = EntryPointTurnRange(guiGameModel.game.map, di.pathFinder, player, transportUnit)
	val workerPlaceCalculator = ColonyWorkerRequestPlaceCalculator(player, guiGameModel.game.map, entryPointTurnRange)

	val purchaseColonists = ColonistsPurchaseRecommendations(player, playerMissionContainer)
	val buyRecomendations = purchaseColonists.generateRecommendations(workerPlaceCalculator, entryPointTurnRange, transportUnit!!)
	purchaseColonists.printToLog(buyRecomendations, entryPointTurnRange)
	purchaseColonists.printToMap(buyRecomendations, tileDebugView)
}

fun aiExplore(di: DI, tileDebugView: TileDebugView) {
	System.out.println("aiExplore - theBestMove")
	
    var guiGameModel = di.guiGameModel
    var moveController = di.moveController
    
    var unit = guiGameModel.getActiveUnit();
    if (unit == null) {
        System.out.println("no unit selected");
        return;
    }
	if (!unit.unitType.isNaval) {
		System.out.println("selected unit is not ship");
		return
	}

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

	//println("navyExplorer " + navyExplorer)
	//pathFinder.printTurnCost(tileDebugView)
    //navyExplorer.toStringsBorderValues(tileDebugView);
}

	fun mapTurnRange(di: DI, mapActor: MapActor, tileDebugView: TileDebugView) {
		var guiGameModel = di.guiGameModel
		if (!guiGameModel.isViewMode()) {
			return
		}
		
		val dutch = guiGameModel.game.players.getById("player:1")

	    var pathFinder = PathFinder()
	    pathFinder.generateRangeMap(
			guiGameModel.game.map, mapActor.mapDrawModel().selectedTile,
			pathFinder.createPathUnit(dutch, Specification.instance.unitTypes.getById(UnitType.GALLEON)),
			PathFinder.excludeUnexploredTiles
		)
		pathFinder.printTurnCost(tileDebugView)
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
		//missionHandler.handle(null, mission)
	}

	fun indianBringGiftExample(di: DI, guiGameModel: GUIGameModel, mapActor: MapActor?) {
		val tile = guiGameModel.game.map.getSafeTile(19, 78)
		//val colonyTile = guiGameModel.game.map.getSafeTile(20, 79)
		val colonyTile = guiGameModel.game.map.getSafeTile(21, 72)
		
		val mission : IndianBringGiftMission
		
		val indianAiContainer = guiGameModel.game.aiContainer.missionContainer(tile.getSettlement().getOwner())
		if (!indianAiContainer.hasMission(IndianBringGiftMission::class.java)) {
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
				MissionExecutorDebugRun(di.guiGameModel, di.moveService, mapActor, di.combatService, di.guiGameController, di.pathFinder, PathFinder())
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
		if (!indianAiContainer.hasMission(DemandTributeMission::class.java)) {
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
				MissionExecutorDebugRun(di.guiGameModel, di.moveService, mapActor, di.combatService, di.guiGameController, di.pathFinder, PathFinder())
					.runMission(tile.getSettlement().getOwner(), mission)
			}
		})
	}

	fun playerTurnAsAi(di: DI, guiGameModel: GUIGameModel, mapActor: MapActor) {
		val pathFinder2 = PathFinder()
		val missionExecutor = MissionExecutor(
			guiGameModel.game,
			di.moveService,
			di.combatService,
			di.guiGameController,
			di.pathFinder,
			pathFinder2
		)
		val missionPlaner = MissionPlaner(guiGameModel.game, di.pathFinder, missionExecutor, pathFinder2)
		val player = guiGameModel.game.playingPlayer
		val missionContainer = guiGameModel.game.aiContainer.missionContainer(player)

		guiGameModel.setActiveUnit(null)
		mapActor.mapDrawModel().setSelectedUnit(null)

		ThreadsResources.instance.executeMovement(object : Runnable {
			override fun run() {
				player.setAi(true)
				SaveGameList().saveAsAutosave(guiGameModel.game)

				di.newTurnService.newTurn(player)

				missionPlaner.planMissions(player)
				missionExecutor.executeMissions(player)
//				missionExecutor.executeMissions(missionContainer, PioneerMission::class.java)
//				missionExecutor.executeMissions(missionContainer, RequestGoodsMission::class.java)

				guiGameModel.game.turn.increaseTurnNumber()
				mapActor.resetMapModel()
				mapActor.resetUnexploredBorders()
				player.setAi(false)
			}
		})
	}

	fun ai_transport_goods_to_sell_mission_example(guiGameModel: GUIGameModel, mapActor: MapActor?) {
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

	// key 9
	fun showMissions(guiGameModel: GUIGameModel, tileDebugView: TileDebugView) {
		tileDebugView.reset()
		val player = guiGameModel.game.playingPlayer
		val missionContainer = guiGameModel.game.aiContainer.missionContainer(player)

		fun showOnMap(mission: TransportUnitMission) {
			for (unitDest in mission.unitsDest) {
				tileDebugView.appendStr(unitDest.dest.x, unitDest.dest.y, "TransportUnit")
			}
		}

		fun showOnMap(mission: TransportGoodsToSellMission) {
			if (mission.phase == TransportGoodsToSellMission.Phase.MOVE_TO_EUROPE) {
				tileDebugView.appendStr(player.entryLocation.x, player.entryLocation.y, "SellGoods")
			} else {
				val settlement = mission.firstSettlementToVisit(player)
				tileDebugView.appendStr(settlement.tile.x, settlement.tile.y, "TakeForSale")
			}
		}

		fun showOnMap(mission: ScoutMission) {
			if (mission.scoutDistantDestination != null) {
				val tile: Tile = mission.scoutDistantDestination!!
				tileDebugView.appendStr(tile.x, tile.y, "ScoutDest")
			}
		}

		fun showOnMap(mission: TransportUnitRequestMission) {
			tileDebugView.appendStr(mission.destination.x, mission.destination.y, "TranUnitReq")
		}

		fun showOnMap(mission: ReplaceColonyWorkerMission) {
			val tile = mission.colony().tile
			tileDebugView.appendStr(tile.x, tile.y, "RplcWorker")
		}

		fun showOnMap(mission: PioneerMission) {
			val tile = mission.colony().tile
			tileDebugView.appendStr(tile.x, tile.y, "Pioneer")
		}

		for (mission in missionContainer.missions.entities()) {
			if (mission.isDone) {
				continue
			}
			when (mission) {
				is ColonyWorkerMission -> tileDebugView.strIfNull(
					mission.tile.x,
					mission.tile.y,
					"Worker"
				)
				is TransportUnitMission -> showOnMap(mission)
				is TransportGoodsToSellMission -> showOnMap(mission)
				is ScoutMission -> showOnMap(mission)
				is TransportUnitRequestMission -> showOnMap(mission)
				is ReplaceColonyWorkerMission -> showOnMap(mission)
				is PioneerMission -> showOnMap(mission)
				else -> println("Can not print mission on map for " + mission.javaClass.name)
			}
		}
	}

	// key 7
	fun simpleTest(
		di: DI,
		guiGameModel: GUIGameModel,
		tileDebugView: TileDebugView,
		mapActor: MapActor?
	) {
		tileDebugView.reset()

		val game = guiGameModel.game
		val player = game.playingPlayer
		val dutch = game.playingPlayer
		val pathFinder = di.pathFinder
		val missionContainer = game.aiContainer.missionContainer(player)
		//val fortNassau = game.map.getTile(20, 79).settlement.asColony()
		val fortOranjeTile = game.map.getTile(25, 75)
		val nieuwAmsterdamTile = game.map.getTile(24, 78)

		val pioneer = DebugPioneer(di, guiGameModel, tileDebugView, mapActor!!)
		pioneer.showImprovementsPlan()
/*
		missionContainer.clearAllMissions()

		val sourceTile = game.map.getTile(29, 71)
		val ship = UnitFactory.create(UnitType.CARAVEL, player, player.europe)
		ship.goodsContainer.increaseGoodsQuantity(GoodsType.SILVER, 100)
		val freeColonist = UnitFactory.create(UnitType.ELDER_STATESMAN, player, ship)

		val transportUnitMission = TransportUnitMission(ship)
		transportUnitMission.addUnitDest(freeColonist, fortOranjeTile)
		transportUnitMission.addCargoDest(nieuwAmsterdamTile, goodsType(GoodsType.SILVER), 100)
		missionContainer.addMission(transportUnitMission)
*/
		player.fogOfWar.resetFogOfWar(guiGameModel.game, player)
        mapActor?.resetMapModel()
        mapActor?.resetUnexploredBorders()
	}

	// key 8
	fun simpleTest2(
		di: DI,
		guiGameModel: GUIGameModel,
		tileDebugView: TileDebugView,
		mapActor: MapActor?
	) {
		val game = guiGameModel.game
		val player = game.playingPlayer
		val dutch = game.playingPlayer
		val missionContainer = game.aiContainer.missionContainer(player)
		val playerAiContainer = game.aiContainer.playerAiContainer(player)
		val pathFinder = di.pathFinder
		val pathFinder2 = di.pathFinder2
		val pathFinder3 = PathFinder()

//		val scout = Scout(di, guiGameModel, tileDebugView, mapActor!!)
//		scout.createScoutMission()

		missionContainer.clearAllMissions()

		val nieuAmsterdamTile = game.map.getTile(24, 78)
		val fortOrange = game.map.getTile(25, 75).settlement.asColony()
		val pioneerUnitRole = Specification.instance.unitRoles.getById(UnitRole.PIONEER)
		val workerFreeColonist = dutch.units.getById("unit:6436")


		fortOrange.goodsContainer.increaseGoodsQuantity(GoodsType.TOOLS, 100)
		val freeColonist = UnitFactory.create(UnitType.EXPERT_FARMER, player, nieuAmsterdamTile)

		val pioneerMission = PioneerMission(freeColonist, fortOrange)
		missionContainer.addMission(pioneerMission)

//		val takeRoleMission = TakeRoleEquipmentMission(freeColonist, fortOrange, pioneerUnitRole, 4)
//		missionContainer.addMission(pioneerMission, takeRoleMission)
//
//		val replaceColonyWorkerMission = ReplaceColonyWorkerMission(fortOrange, workerFreeColonist, freeColonist)
//		missionContainer.addMission(takeRoleMission, replaceColonyWorkerMission)

		player.fogOfWar.resetFogOfWar(guiGameModel.game, player)
		mapActor?.resetMapModel()
		mapActor?.resetUnexploredBorders()
	}

	fun tileFrom(game: Game, settlement: Settlement, direction: Direction): Tile {
		return game.map.getTile(settlement.tile, direction)
	}

	fun unitRole(unitRoleId: String): UnitRole {
		return Specification.instance.unitRoles.getById(unitRoleId)
	}

	fun goodsType(goodsTypeId: GoodsTypeId): GoodsType {
		return Specification.instance.goodsTypes.getById(goodsTypeId)
	}

	fun resetDebug(tileDebugView: TileDebugView) {
		tileDebugView.reset()
	}

class Scout(
	val di: DI,
	val guiGameModel: GUIGameModel,
	val tileDebugView: TileDebugView,
	val mapActor: MapActor
) {
	val game = guiGameModel.game
	val player = game.playingPlayer
	val missionContainer = game.aiContainer.missionContainer(player)

	fun createScoutMission() {
		val mission = missionContainer.findFirstMission(ScoutMission::class.java)
		if (mission == null) {
			var scout: Unit? = null
			for (unit in player.units) {
				if (unit.unitRole.equalsId(UnitRole.SCOUT)) {
					scout = unit
					break;
				}
			}
			if (scout == null) {
				// scout next3 savegame
				val tile = game.map.getTile(32, 19)
				scout = UnitFactory.create(UnitType.SCOUT, UnitRole.SCOUT, player, tile)
			}

			val scoutMission = ScoutMission(scout!!)
			missionContainer.addMission(scoutMission)
		}
	}

	fun printDestinations() {
		val scoutMissionPlaner = ScoutMissionPlaner(
			guiGameModel.game,
			di.pathFinder,
			di.pathFinder2
		)
		scoutMissionPlaner.printAllCandidates(player, tileDebugView)
		//printFirstDestination
	}

}

class DebugPioneer(
	val di: DI,
	val guiGameModel: GUIGameModel,
	val tileDebugView: TileDebugView,
	val mapActor: MapActor
) {

	val plowedType = Specification.instance.tileImprovementTypes.getById(TileImprovementType.PLOWED_IMPROVEMENT_TYPE_ID)
	val roadType = Specification.instance.tileImprovementTypes.getById(TileImprovementType.ROAD_MODEL_IMPROVEMENT_TYPE_ID)
	val clearForestType = Specification.instance.tileImprovementTypes.getById(TileImprovementType.CLEAR_FOREST_IMPROVEMENT_TYPE_ID)

	val game = guiGameModel.game
	val player = game.playingPlayer
	val missionContainer = game.aiContainer.missionContainer(player)

	fun createPioneerMission() {
		var pioneerMission = missionContainer.findFirstMission(PioneerMission::class.java)
		if (pioneerMission == null) {
			missionContainer.clearAllMissions()

			// scout next3 savegame
			val isabellaTile = game.map.getTile(32, 17)
			val isabellaColony = isabellaTile.settlement.asColony()

			//isabellaColony.goodsContainer.increaseGoodsQuantity(GoodsType.TOOLS, 100)

			val pioneer = UnitFactory.create(
				UnitType.HARDY_PIONEER,
				UnitRole.PIONEER,
				player,
				isabellaColony.tile
			)
			pioneerMission = PioneerMission(pioneer, isabellaColony)
			missionContainer.addMission(pioneerMission)
		}
	}

	fun showImprovementsPlan() {
		val game = guiGameModel.game
		val player = game.playingPlayer
		val balanced = AddImprovementPolicy.Balanced()

		val pionierMissionPlaner = PioneerMissionPlaner(game, di.pathFinder)
		val generateImprovementsPlanScore = pionierMissionPlaner.generateImprovementsPlanScore(
			player,
			balanced
		)

		println("generateImprovementsPlanScore.size = " + generateImprovementsPlanScore.size())
		for (objectScore in generateImprovementsPlanScore) {
			println("colony: " + objectScore.obj.colony.name + ", score: " + objectScore.score())
			objectScore.obj.printToMap(tileDebugView)
		}
		mapActor.resetMapModel()
	}

	fun showOnMapImprovementsDestinations() {
		val game = guiGameModel.game

		val pionnerMissionPlaner = PioneerMissionPlaner(game, di.pathFinder)

		missionContainer.foreachMission(PioneerMission::class.java) { mission ->
			val improvementDestination = pionnerMissionPlaner.findImprovementDestination(mission, missionContainer)

			when (improvementDestination) {
				is PioneerDestination.OtherIsland -> improvementDestination.plan.printToMap(tileDebugView)
				is PioneerDestination.TheSameIsland -> improvementDestination.plan.printToMap(tileDebugView)
			}
		}
	}

	fun removeAllImprovements(colony: Colony) {
		for (colonyTile in colony.colonyTiles) {
			colonyTile.tile.removeTileImprovement(TileImprovementType.PLOWED_IMPROVEMENT_TYPE_ID)
			if (!colonyTile.tile.equalsCoordinates(colony.tile)) {
				colonyTile.tile.removeTileImprovement(TileImprovementType.ROAD_MODEL_IMPROVEMENT_TYPE_ID)
			}
		}
	}
}

