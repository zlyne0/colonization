package promitech.colonization.ai

enum class MissionPlanStatus {
    MISSION_CREATED,
    NO_MISSION;

    inline fun whenNoMission(action: () -> MissionPlanStatus): MissionPlanStatus {
        if (this == NO_MISSION) {
            return action()
        }
        return MISSION_CREATED
    }
}