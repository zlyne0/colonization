package net.sf.freecol.common.model.ai.missions.scout

import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.UnitFactory
import net.sf.freecol.common.model.UnitRole
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.ai.missions.findFirstMissionKt
import promitech.colonization.DI
import promitech.colonization.screen.debug.TileDebugView
import promitech.colonization.screen.map.MapActor
import promitech.colonization.screen.map.hud.GUIGameModel

class DebugScout(
	val di: DI,
	val guiGameModel: GUIGameModel,
	val tileDebugView: TileDebugView,
	val mapActor: MapActor
) {
	val game = guiGameModel.game
	val player = game.playingPlayer
	val missionContainer = game.aiContainer.missionContainer(player)

	fun createScoutMission() {
		val mission = missionContainer.findFirstMissionKt(ScoutMission::class.java)
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

	fun printFirstDestination(scout: Unit) {
		val scoutMissionPlaner = ScoutMissionPlaner(
            guiGameModel.game,
            di.pathFinder,
            di.pathFinder2
        )
		val destination = scoutMissionPlaner.printFirstDestination(scout, tileDebugView)
		println("FirstDestination: $destination")
	}

}