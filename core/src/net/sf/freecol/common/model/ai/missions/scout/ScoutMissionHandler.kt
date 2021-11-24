package net.sf.freecol.common.model.ai.missions.scout

import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.map.path.Path
import promitech.colonization.ai.MissionHandler
import promitech.colonization.ai.MissionHandlerLogger
import promitech.colonization.orders.move.MoveContext
import promitech.colonization.orders.move.MoveService

class ScoutMissionHandler(
    private val scoutMissionPlaner: ScoutMissionPlaner,
    private val moveService: MoveService
): MissionHandler<ScoutMission> {

    override fun handle(playerMissionsContainer: PlayerMissionsContainer, mission: ScoutMission) {
        val player = playerMissionsContainer.player
        if (!mission.isScoutExists()) {
            MissionHandlerLogger.logger.debug("player[%s].ScoutMissionHandler scout does not exists", player.getId())
            mission.setDone()
            return
        }

        if (mission.scout.isAtLocation(Tile::class.java)) {
            val scoutDestination = scoutMissionPlaner.findScoutDestinationFromLandTile(mission.scout)

            when (scoutDestination) {
                is ScoutDestination.TheSameIsland -> moveToDestination(mission, scoutDestination.path)
                is ScoutDestination.OtherIsland -> moveToOtherIsland(scoutDestination, mission.scout)
                is ScoutDestination.Lack -> println("TODO ScoutDestination.Lack")
            }
        }

        if (!mission.isScoutExists()) {
            // scout could die, remove mission
            MissionHandlerLogger.logger.debug("player[%s].ScoutMissionHandler scout does not exists", player.getId())
            mission.setDone()
        }
    }

    private fun moveToOtherIsland(scoutDestination: ScoutDestination.OtherIsland, scout: Unit) {
        if (scoutDestination.transferLocationPath.isReachedDestination()) {
            val moveContext = MoveContext(scout, scoutDestination.transferLocationPath)
            moveService.aiConfirmedMovePath(moveContext)
        } else {
            // TODO: turn wait counter, add to black list
        }
        // first go do settlement, when no settlement go to the best meeting
    }

    fun moveToDestination(mission: ScoutMission, path: Path) {
        if (path.isReachedDestination()) {
            val moveContext = MoveContext(mission.scout, path)
            moveService.aiConfirmedMovePath(moveContext)
        } else {
            // TODO: turn wait counter, add to black list
        }
    }

}