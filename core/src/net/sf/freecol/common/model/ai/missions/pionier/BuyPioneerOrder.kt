package net.sf.freecol.common.model.ai.missions.pionier

import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.UnitRole
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.player.Player

sealed class BuyPioneerOrder {
    class BuySpecialistOrder(val hardyPioneerUnitType: UnitType): BuyPioneerOrder() {
        fun buy(player: Player): Unit {
            return player.europe.buyUnitByAI(hardyPioneerUnitType)
        }
    }

    class RecruitColonistOrder(val pioneerUnitRole: UnitRole): BuyPioneerOrder() {
        fun buy(player: Player, game: Game): Unit {
            val freeColonistUnitType = Specification.instance.freeColonistUnitType
            val colonist = player.europe.buyUnitByAI(freeColonistUnitType)
            player.europe.changeUnitRole(game, colonist, pioneerUnitRole)
            return colonist
        }
    }

    class CanNotAfford: BuyPioneerOrder()
}
