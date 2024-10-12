package net.sf.freecol.common.model.ai.missions.scout

import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.player.PlayerExploredTiles
import promitech.colonization.DI
import promitech.colonization.screen.debug.TileDebugView
import promitech.colonization.screen.map.MapActor
import promitech.colonization.screen.map.hud.GUIGameModel
import promitech.map.forEach

class DebugScoutPatrol(
	val di: DI,
	val guiGameModel: GUIGameModel,
	val tileDebugView: TileDebugView,
	val mapActor: MapActor
) {
	val game = guiGameModel.game
	val player = game.playingPlayer
	val missionContainer = game.aiContainer.missionContainer(player)

	fun printDestinationsToPatrol() {
		val patrolPlaner = PatrolPlaner(game, player)
		val areaToPatrol = patrolPlaner.generateAreaToPatrol()
		areaToPatrol.forEach { x, y, range ->
			val exploredTurn = player.playerExploredTiles.exploredTurn(x, y)
			if (PlayerExploredTiles.isExplored(exploredTurn)) {
				tileDebugView.appendStr(x, y, "R$range $exploredTurn")
			}
		}
		patrolPlaner.printOldestVisitedSpots(areaToPatrol, tileDebugView)
		val patrolDestination = patrolPlaner.findTheClosesSpot(areaToPatrol)
		if (patrolDestination is PatrolPlaner.PatrolDestination.OtherIsland) {
			tileDebugView.appendStr(patrolDestination.tile.x, patrolDestination.tile.y, "DEST")
		}
	}

	fun printDestinationsDependsLocation(sourceTile: Tile) {
		val patrolPlaner = PatrolPlaner(game, player)
		val areaToPatrol = patrolPlaner.generateAreaToPatrol()
		patrolPlaner.printOldestVisitedSpotsPerIsland(areaToPatrol, tileDebugView)
		val patrolDestination = patrolPlaner.findPatrolDestination(
			sourceTile,
            PatrolPlaner.PatrolDestinationPolicy.DifferentIslands
		)
		when (patrolDestination) {
			is PatrolPlaner.PatrolDestination.OtherIsland -> tileDebugView.appendStr(patrolDestination.tile.x, patrolDestination.tile.y, "DEST")
			is PatrolPlaner.PatrolDestination.TheSameIsland -> tileDebugView.appendStr(patrolDestination.tile.x, patrolDestination.tile.y, "DEST")
		}
	}
}