package net.sf.freecol.common.model.ai.missions.transportunit

import net.sf.freecol.common.model.ai.missions.AbstractMission
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer

interface CheckAvailabilityMissionHandler {

    fun checkAvailability(
        playerMissionsContainer: PlayerMissionsContainer,
        parentMission: AbstractMission,
        transportRequestMission: TransportUnitRequestMission,
    )
}