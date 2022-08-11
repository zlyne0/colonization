package net.sf.freecol.common.model.ai.missions.pioneer

import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.ai.missions.AbstractMission

interface ReplaceUnitInMissionHandler {
    fun replaceUnitInMission(mission: AbstractMission, unitToReplace: Unit, replaceBy: Unit)
}