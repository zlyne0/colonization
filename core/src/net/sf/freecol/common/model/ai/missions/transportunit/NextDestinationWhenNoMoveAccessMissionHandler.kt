package net.sf.freecol.common.model.ai.missions.transportunit

import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.ai.missions.AbstractMission
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer

interface NextDestinationWhenNoMoveAccessMissionHandler {

    fun nextDestination(
        playerMissionsContainer: PlayerMissionsContainer,
        mission: AbstractMission,
        transportRequestMission: TransportUnitRequestMission
    ): Tile?

}