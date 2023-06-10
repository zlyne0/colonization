package net.sf.freecol.common.model.ai.missions.indian

import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.forEachTileInRange
import net.sf.freecol.common.model.player.Player
import net.sf.freecol.common.model.player.Stance
import net.sf.freecol.common.model.player.Tension
import net.sf.freecol.common.model.specification.NationType
import promitech.colonization.orders.combat.OffencePower

class AttackDecision {

    companion object {

        @JvmField
        val attackMatrix: Map<Tension.Level, Set<NationType.AggressionLevel>> = mapOf(
            Tension.Level.HAPPY to setOf(),
            Tension.Level.CONTENT to setOf(NationType.AggressionLevel.HIGH),
            Tension.Level.DISPLEASED to setOf(NationType.AggressionLevel.HIGH, NationType.AggressionLevel.AVERAGE),
            Tension.Level.ANGRY to setOf(NationType.AggressionLevel.HIGH, NationType.AggressionLevel.AVERAGE, NationType.AggressionLevel.LOW),
            Tension.Level.HATEFUL to setOf(NationType.AggressionLevel.HIGH, NationType.AggressionLevel.AVERAGE, NationType.AggressionLevel.LOW)
        )

        @JvmStatic
        private fun aggression(unit: Unit, player: Player): Boolean {
            val tension = tensionForPlayer(unit, player)
            if (tension == null) {
                return false
            }
            return aggression(unit.owner.getStance(player), tension.level, unit.owner.nationType().aggressionLevel)
        }

        @JvmStatic
        private fun tensionForPlayer(unit: Unit, player: Player): Tension? {
            var tension: Tension? = null
            if (unit.indianSettlementId != null) {
                val settlement = unit.owner.settlements.getById(unit.indianSettlementId)
                if (settlement != null) {
                    tension = settlement.asIndianSettlement().getTension(player)
                }
            }
            if (tension == null) {
                tension = unit.owner.getTension(player)
            }
            return tension
        }

        @JvmStatic
        private fun aggression(stance: Stance, tension: Tension.Level, aggressionLevel: NationType.AggressionLevel): Boolean {
            return when(stance) {
                Stance.WAR -> true
                Stance.ALLIANCE, Stance.CEASE_FIRE -> false
                Stance.UNCONTACTED, Stance.PEACE -> attackBasedOnTension(tension, aggressionLevel)
            }
        }

        @JvmStatic
        private fun attackBasedOnTension(tension: Tension.Level, aggressionLevel: NationType.AggressionLevel): Boolean {
            val levels = attackMatrix[tension] ?: return false
            return levels.contains(aggressionLevel)
        }

        @JvmStatic
        fun determineTileToAggression(map: net.sf.freecol.common.model.Map, unit: Unit): Tile? {
            var minDefencePower = Float.MAX_VALUE
            var tileMinDefencePower: Tile? = null

            map.forEachTileInRange(unit.tile, unit.lineOfSight()) { tile ->
                val tileUnitOwner: Player? = tile.unitOccupierOwner()

                if (tileUnitOwner != null && !tileUnitOwner.equalsId(unit.owner) && tileUnitOwner.isEuropean) {
                    val isAggression = aggression(unit, tileUnitOwner)
                    if (isAggression) {
                        val defencePower = OffencePower.calculateTileDefencePowerForAttacker(unit, tile)
                        if (defencePower < minDefencePower) {
                            minDefencePower = defencePower
                            tileMinDefencePower = tile
                        }
                    }
                }
            }
            return tileMinDefencePower
        }
    }
}