package promitech.colonization.ai.military

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.Unit

class DefencePlaner(val game: Game) {

    fun findColonyToProtect(unit: Unit): Colony? {
        val threatModel = ThreatModel(game, unit.owner)
        val colonyDefencePriority = threatModel.calculateColonyDefencePriority()
        if (colonyDefencePriority.isEmpty()) {
            return null
        }
        return colonyDefencePriority.first().colony
    }

}