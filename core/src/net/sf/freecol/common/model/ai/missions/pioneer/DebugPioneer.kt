package net.sf.freecol.common.model.ai.missions.pioneer

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.TileImprovementType
import net.sf.freecol.common.model.UnitFactory
import net.sf.freecol.common.model.UnitRole
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.ai.missions.findFirstMissionKt
import net.sf.freecol.common.model.ai.missions.foreachMission
import net.sf.freecol.common.util.whenNotNull
import promitech.colonization.DI
import promitech.colonization.screen.debug.TileDebugView
import promitech.colonization.screen.map.MapActor
import promitech.colonization.screen.map.hud.GUIGameModel

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
		var pioneerMission = missionContainer.findFirstMissionKt(PioneerMission::class.java)
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
		val balanced = AddImprovementPolicy.Balanced(player)

		val pioneerMissionPlaner = PioneerMissionPlaner(game, di.pathFinder)
		val generateImprovementsPlanScore = pioneerMissionPlaner.generateImprovementsPlanScore(
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

		val pioneerMissionPlaner = PioneerMissionPlaner(game, di.pathFinder)

		missionContainer.foreachMission(PioneerMission::class.java) { mission ->
			missionContainer.player.units.getByIdOrNull(mission.pioneerId).whenNotNull { pioneer ->
				val improvementDestination = pioneerMissionPlaner.findImprovementDestination(mission, missionContainer, pioneer)
				when (improvementDestination) {
					is PioneerDestination.OtherIsland -> improvementDestination.plan.printToMap(tileDebugView)
					is PioneerDestination.TheSameIsland -> improvementDestination.plan.printToMap(tileDebugView)
				}
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