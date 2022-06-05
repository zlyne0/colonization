package net.sf.freecol.common.model.ai.missions.transportunit

import net.sf.freecol.common.model.MoveType
import net.sf.freecol.common.model.ai.missions.AbstractMission
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer

interface MoveNoAccessNotificationMissionHandler {

    fun moveNoAccessNotification(
        playerMissionsContainer: PlayerMissionsContainer,
        parentMission: AbstractMission,
        mission: TransportUnitRequestMission,
        moveType: MoveType
    )

}