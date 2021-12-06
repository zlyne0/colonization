package promitech.colonization.screen.debug

import net.sf.freecol.common.model.IndianSettlement
import net.sf.freecol.common.model.Settlement
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.UnitFactory
import net.sf.freecol.common.model.UnitRole
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.ai.missions.SeekAndDestroyMission
import net.sf.freecol.common.model.ai.missions.TransportUnitMission
import net.sf.freecol.common.model.ai.missions.goodsToSell.ColoniesProductionValue
import net.sf.freecol.common.model.ai.missions.goodsToSell.TransportGoodsToSellMission
import net.sf.freecol.common.model.ai.missions.indian.DemandTributeMission
import net.sf.freecol.common.model.ai.missions.indian.IndianBringGiftMission
import net.sf.freecol.common.model.ai.missions.scout.ScoutMission
import net.sf.freecol.common.model.ai.missions.scout.ScoutMissionPlaner
import net.sf.freecol.common.model.ai.missions.workerrequest.ColonistsPurchaseRecommendations
import net.sf.freecol.common.model.ai.missions.workerrequest.ColonyWorkerMission
import net.sf.freecol.common.model.ai.missions.workerrequest.ColonyWorkerRequestPlaceCalculator
import net.sf.freecol.common.model.ai.missions.workerrequest.EntryPointTurnRange
import net.sf.freecol.common.model.ai.missions.workerrequest.ScorePolicy
import net.sf.freecol.common.model.colonyproduction.ColonyPlan
import net.sf.freecol.common.model.map.generator.MapGenerator
import net.sf.freecol.common.model.map.generator.SmoothingTileTypes
import net.sf.freecol.common.model.map.path.PathFinder
import net.sf.freecol.common.model.player.Player
import net.sf.freecol.common.model.player.Tension
import net.sf.freecol.common.model.specification.AbstractGoods
import promitech.colonization.DI
import promitech.colonization.ai.MissionExecutor
import promitech.colonization.ai.MissionExecutorDebugRun
import promitech.colonization.ai.MissionPlaner
import promitech.colonization.ai.NavyExplorer
import promitech.colonization.ai.SeekAndDestroyMissionHandler
import promitech.colonization.ai.Units
import promitech.colonization.infrastructure.ThreadsResources
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

		commandArg("reset_debug") {
			resetDebug(tileDebugView)
		}

		command("simpleTest") {
			simpleTest(di, guiGameModel, tileDebugView)
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
	val transportUnit = Units.findCarrier(player)
	val sut = ColonyWorkerRequestPlaceCalculator(player, guiGameModel.game.map, EntryPointTurnRange(guiGameModel.game.map, di.pathFinder, player, transportUnit))

	val missionContainer = guiGameModel.game.aiContainer.missionContainer(player)
	val createColonyTiles = missionContainer.findMissions(ColonyWorkerMission::class.java).map { it -> it.tile }
	sut.debugTheBestBlaceToBuildColony(tileDebugView, createColonyTiles)
}

fun generateTileScoresForNewColony(di: DI, guiGameModel: GUIGameModel, tileDebugView: TileDebugView) {
	tileDebugView.reset()

	val player = guiGameModel.game.playingPlayer
	val transportUnit = Units.findCarrier(player)
	val entryPointTurnRange = EntryPointTurnRange(guiGameModel.game.map, di.pathFinder, player, transportUnit)
	val sut = ColonyWorkerRequestPlaceCalculator(player, guiGameModel.game.map, entryPointTurnRange)

	sut.debugGenerateTileScoresForNewColony(tileDebugView)
}

fun generateWorkerReqScoreByValue(di: DI, guiGameModel: GUIGameModel, tileDebugView: TileDebugView) {
	tileDebugView.reset()

	val player = guiGameModel.game.playingPlayer
	val transportUnit = Units.findCarrier(player)
	val entryPointTurnRange = EntryPointTurnRange(guiGameModel.game.map, di.pathFinder, player, transportUnit)

	val sut = ColonyWorkerRequestPlaceCalculator(player, guiGameModel.game.map, entryPointTurnRange)
	val colonyWorkerRequestScores = sut.score(PlayerMissionsContainer(player))

	val scorePolicy = ScorePolicy.WorkerProductionValue(entryPointTurnRange)
	scorePolicy.calculateScore(colonyWorkerRequestScores)

	sut.debug(tileDebugView)
}

fun generateWorkerReqScoreByPriceToValue(di: DI, guiGameModel: GUIGameModel, tileDebugView: TileDebugView) {
	tileDebugView.reset()

	val player = guiGameModel.game.playingPlayer
	val transportUnit = Units.findCarrier(player)
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
	val transportUnit = Units.findCarrier(player)
	val entryPointTurnRange = EntryPointTurnRange(guiGameModel.game.map, di.pathFinder, player, transportUnit)
	val workerPlaceCalculator = ColonyWorkerRequestPlaceCalculator(player, guiGameModel.game.map, entryPointTurnRange)

	val purchaseColonists = ColonistsPurchaseRecommendations(player, playerMissionContainer)
	val buyRecomendations = purchaseColonists.generateRecommendations(workerPlaceCalculator, entryPointTurnRange)
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
		val missionExecutor = MissionExecutor(
			guiGameModel.game,
			di.moveService,
			di.combatService,
			di.guiGameController,
			di.pathFinder
		)
		val missionPlaner = MissionPlaner(guiGameModel.game, di.pathFinder, missionExecutor)
		val player = guiGameModel.game.playingPlayer

		ThreadsResources.instance.executeMovement(object : Runnable {
			override fun run() {
				player.setAi(true)
				di.newTurnService.newTurn(player)

				missionPlaner.planMissions(player)
				missionExecutor.executeMissions(player)

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

	fun showMissions(guiGameModel: GUIGameModel, tileDebugView: TileDebugView) {
		tileDebugView.reset()
		val player = guiGameModel.game.playingPlayer
		val missionContainer = guiGameModel.game.aiContainer.missionContainer(player)

		fun showOnMap(mission: TransportUnitMission) {
			for (unitDest in mission.unitsDest) {
				tileDebugView.strIfNull(unitDest.dest.x, unitDest.dest.y, "TransportUnit")
			}
		}

		fun showOnMap(mission: TransportGoodsToSellMission) {
			if (mission.phase == TransportGoodsToSellMission.Phase.MOVE_TO_EUROPE) {
				tileDebugView.strIfNull(player.entryLocation.x, player.entryLocation.y, "SellGoods")
			} else {
				val settlement = mission.firstSettlementToVisit(player)
				tileDebugView.strIfNull(settlement.tile.x, settlement.tile.y, "TakeForSale")
			}
		}

		for (mission in missionContainer.missions.entities()) {
			if (mission.isDone) {
				continue
			}
			when (mission) {
				is ColonyWorkerMission -> tileDebugView.strIfNull(mission.tile.x, mission.tile.y, "Worker")
				is TransportUnitMission -> showOnMap(mission)
				is TransportGoodsToSellMission -> showOnMap(mission)
				else -> println("Can not print mission on map for " + mission.javaClass.name)
			}
		}

	}

	fun simpleTest(di: DI, guiGameModel: GUIGameModel, tileDebugView: TileDebugView) {
		val game = guiGameModel.game
		val player = game.playingPlayer
		val missionContainer = game.aiContainer.missionContainer(player)

		val sourceTile = game.map.getTile(29, 14)

		val scoutMissions = missionContainer.findMissions(ScoutMission::class.java)
		val scoutMission: ScoutMission
		if (!scoutMissions.isEmpty()) {
			scoutMission = scoutMissions[0]
		} else {
			val scout = UnitFactory.create(UnitType.SCOUT, UnitRole.SCOUT, player, sourceTile)
			scoutMission = ScoutMission(scout)
			missionContainer.addMission(scoutMission)
		}

		// generate first destination
		tileDebugView.reset()
		val scoutMissionPlaner = ScoutMissionPlaner(game, di.pathFinder, PathFinder())
		scoutMissionPlaner.printAllCandidates(player, tileDebugView)
		scoutMissionPlaner.printFirstDestination(scoutMission.scout, tileDebugView)

		if (scoutMission.phase == ScoutMission.Phase.WAIT_FOR_TRANSPORT) {
			val carrier = Units.findCarrier(player)
			val transportUnitMission = TransportUnitMission(carrier)
			transportUnitMission.addUnitDest(scoutMission.scout, scoutMission.scoutDistantDestination, true)
			scoutMission.addDependMission(transportUnitMission)
			//missionContainer.addMission(transportUnitMission)
		}



        val villageTile = game.map.getTile(24, 25)
        for (neighbourTile in game.map.neighbourLandTiles(villageTile)) {
            UnitFactory.create(UnitType.BRAVE, villageTile.settlement.owner, neighbourTile.tile)
        }
		for (neighbourTile in game.map.neighbourLandTiles(game.map.getTile(24, 22))) {
			UnitFactory.create(UnitType.BRAVE, villageTile.settlement.owner, neighbourTile.tile)
		}
	}

	fun simpleTest2(di: DI, guiGameModel: GUIGameModel, tileDebugView: TileDebugView, mapActor: MapActor?) {
		val game = guiGameModel.game
		val player = game.playingPlayer
		val dutch = game.playingPlayer
		val missionContainer = game.aiContainer.missionContainer(player)
		val pathFinder = di.pathFinder
		val pathFinder2 = PathFinder()

		val scoutLocation = game.map.getTile(25, 86)

		val shipLocation = game.map.getTile(27, 86)



		val scoutMissions = missionContainer.findMissions(ScoutMission::class.java)
		val scoutMission: ScoutMission
		if (!scoutMissions.isEmpty()) {
			scoutMission = scoutMissions[0]
		} else {
			val scout = UnitFactory.create(UnitType.SCOUT, UnitRole.SCOUT, dutch, scoutLocation)
			scoutMission = ScoutMission(scout)
			missionContainer.addMission(scoutMission)
		}

		if (scoutMission.phase == ScoutMission.Phase.WAIT_FOR_TRANSPORT) {
			val carrier = UnitFactory.create(UnitType.CARAVEL, dutch, shipLocation)
			val transportUnitMission = TransportUnitMission(carrier)
			transportUnitMission.addUnitDest(scoutMission.scout, scoutMission.scoutDistantDestination, true)
			scoutMission.addDependMission(transportUnitMission)
		}


//		val sourceTile = game.map.getTile(28, 82)
//		val destInLand = game.map.getTile(24, 78)
//
//		val scoutMissionPlaner = ScoutMissionPlaner(game, di.pathFinder, PathFinder())
//
//		val scoutMissions = missionContainer.findMissions(ScoutMission::class.java)
//		if (!scoutMissions.isEmpty()) {
//			val mission = scoutMissions[0]
//			val scoutDestination = scoutMissionPlaner.findScoutDestination(mission.scout)
//			println("scoutDestination " + scoutDestination.javaClass.name)
//
//			if (scoutDestination is ScoutDestination.OtherIsland) {
//				tileDebugView.str(scoutDestination.tile, "Dest")
//
//				println("scoutDestination.transferLocationPath " + scoutDestination.transferLocationPath)
//
//				for (tile in scoutDestination.transferLocationPath.tiles) {
//					tileDebugView.str(tile, "x")
//				}
//			}
//		}

//		val galleon = UnitFactory.create(UnitType.GALLEON, dutch, sourceTile)
//		val colonist = UnitFactory.create(UnitType.FREE_COLONIST, dutch, galleon)
//		val transportMission = TransportUnitMission(galleon)
//			.addUnitDest(colonist, destInLand)
//		game.aiContainer.missionContainer(dutch).addMission(transportMission)
//
//		val unitDest = transportMission.firstUnitToTransport()
//		val carrier = galleon
//
//		var path = pathFinder.findToTile(game.map, carrier, unitDest.dest, PathFinder.includeUnexploredAndExcludeNavyThreatTiles)
//		println("path " + path)
	}

	fun resetDebug(tileDebugView: TileDebugView) {
		tileDebugView.reset()
	}
