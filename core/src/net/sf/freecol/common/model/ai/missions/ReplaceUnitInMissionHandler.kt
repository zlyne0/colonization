package net.sf.freecol.common.model.ai.missions

import net.sf.freecol.common.model.Unit

interface ReplaceUnitInMissionHandler {
    fun replaceUnitInMission(mission: AbstractMission, unitToReplace: Unit, replaceBy: Unit)
}