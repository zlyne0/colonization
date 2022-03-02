package net.sf.freecol.common.model.ai.missions


@Suppress("UNCHECKED_CAST")
inline fun <T : AbstractMission> PlayerMissionsContainer.hasMissionKt(
    missionClass: Class<T>,
    predicate: (T) -> Boolean
): Boolean {
    for (playerMission in this.missions.entities()) {
        if (playerMission.`is`(missionClass) && predicate(playerMission as T)) {
            return true
        }
    }
    return false
}

inline fun <T : AbstractMission> PlayerMissionsContainer.findRecursively(
    missionClass: Class<T>,
    predicate: (T) -> Boolean
): List<T> {
    val buffor = mutableListOf<AbstractMission>()
    val result = mutableListOf<T>()
    buffor.addAll(this.missions.entities())

    while (buffor.isNotEmpty()) {
        val mission = buffor.removeAt(0)

        @Suppress("UNCHECKED_CAST")
        if (mission.`is`(missionClass) && predicate(mission as T)) {
            result.add(mission)
        }
        if (mission.hasDependMissions()) {
            buffor.addAll(mission.getDependMissions())
        }
    }
    return result
}
