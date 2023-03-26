package promitech.colonization.ai.navy

import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.ai.missions.TransportUnitMission
import promitech.colonization.ai.MissionPlanStatus

internal class PlanNavyUnitContext(
    val unit: Unit,
    val playerMissionContainer: PlayerMissionsContainer,
) {
    var tum: TransportUnitMission? = null
        private set

    fun missionPlanStatus(): MissionPlanStatus {
        if (tum != null) {
            return MissionPlanStatus.MISSION_CREATED
        }
        return MissionPlanStatus.NO_MISSION
    }

    inline fun forTransportMission(action: (TransportUnitMission) -> kotlin.Unit) {
        if (tum == null) {
            tum = TransportUnitMission(unit)
        }
        action(tum!!)
    }
}