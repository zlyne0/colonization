package promitech.colonization.ai

import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.ai.missions.AbstractMission
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.ai.missions.findFirstMissionKt
import net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission
import net.sf.freecol.common.model.map.path.Path
import net.sf.freecol.common.model.map.path.PathFinder
import promitech.colonization.orders.move.MoveContext
import promitech.colonization.orders.move.MoveService

inline fun moveToDestination(
    game: Game,
    moveService: MoveService,
    pathFinder: PathFinder,
    unit: Unit,
    destTile: Tile,
    action: () -> kotlin.Unit = {}
) {
    if (unit.isAtLocation(destTile)) {
        action()
    } else {
        val path: Path = pathFinder.findToTile(
            game.map,
            unit,
            destTile,
            PathFinder.includeUnexploredTiles
        )
        if (path.reachTile(destTile)) {
            val moveContext = MoveContext(unit, path)
            moveService.aiConfirmedMovePath(moveContext)
        }
        if (unit.isAtLocation(destTile)) {
            action()
        }
    }
}

fun createTransportRequest(
    game: Game,
    playerMissionsContainer: PlayerMissionsContainer,
    mission: AbstractMission,
    unit: Unit,
    destinationTile: Tile
): TransportUnitRequestMission {

    val transportMission = playerMissionsContainer.findFirstMissionKt(TransportUnitRequestMission::class.java) { requestMission ->
        requestMission.unit.equalsId(unit)
    }
    if (transportMission == null) {
        val transportRequestMission = TransportUnitRequestMission(game.turn, unit, destinationTile)
        playerMissionsContainer.addMission(mission, transportRequestMission)
        return transportRequestMission
    }
    return transportMission
}
