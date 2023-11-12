package net.sf.freecol.common.model.ai.missions

fun PlayerMissionsContainer.hasMission(missionId: String): Boolean {
    return missions.containsId(missionId)
}

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

@Suppress("UNCHECKED_CAST")
inline fun <T : AbstractMission> PlayerMissionsContainer.findFirstMissionKt(
    missionClass: Class<T>,
    predicate: (T) -> Boolean
): T? {
    for (playerMission in this.missions.entities()) {
        if (playerMission.`is`(missionClass) && predicate(playerMission as T)) {
            return playerMission
        }
    }
    return null
}

@Suppress("UNCHECKED_CAST")
inline fun <T : AbstractMission> PlayerMissionsContainer.foreachMission(
    missionClass: Class<T>,
    consumer: (T) -> Unit
) {
    for (playerMission in this.missions.entities()) {
        if (playerMission.`is`(missionClass)) {
            consumer(playerMission as T)
        }
    }
}

@Suppress("UNCHECKED_CAST")
inline fun <T : AbstractMission> PlayerMissionsContainer.findMissions(
    missionClass: Class<T>,
    predicate: (T) -> Boolean
): List<T> {
    var result : MutableList<T>? = null

    for (playerMission in this.missions.entities()) {
        if (playerMission.`is`(missionClass) && predicate(playerMission as T)) {
            if (result == null) {
                result = mutableListOf<T>()
            }
            result.add(playerMission)
        }
    }
    if (result == null) {
        return emptyList()
    }
    return result
}

fun PlayerMissionsContainer.findDeepDependMissions(mission: AbstractMission): List<AbstractMission> {
    val result: MutableList<AbstractMission> = mutableListOf()
    val buf: MutableList<AbstractMission> = mutableListOf(mission)

    while (buf.isNotEmpty()) {
        val m = buf.removeAt(0)
        if (m.notEqualsId(mission)) {
            result.add(m)
        }
        for (dependMissionId in m.dependMissions2) {
            val dependMission = this.missions.getByIdOrNull(dependMissionId)
            if (dependMission != null) {
                buf.add(dependMission)
            }
        }
    }
    return result
}

fun PlayerMissionsContainer.findMissionToExecute(parentMission: AbstractMission): List<AbstractMission> {
    val dependMissions = findDeepDependMissions(parentMission)
    if (dependMissions.isEmpty()) {
        return emptyList()
    }

    var result = mutableListOf<AbstractMission>()
    for (dependMission in dependMissions) {
        if (dependMission.hasDependMission() || dependMission.isDone) {
            continue
        }
        result.add(dependMission)
    }
    return result
}

fun PlayerMissionsContainer.findParentMission(childMission: AbstractMission): AbstractMission? {
    if (childMission.parentMissionId == null) {
        return null
    }
    return this.missions.getByIdOrNull(childMission.parentMissionId)
}

