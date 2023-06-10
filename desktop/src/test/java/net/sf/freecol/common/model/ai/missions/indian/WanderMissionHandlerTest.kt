package net.sf.freecol.common.model.ai.missions.indian

import net.sf.freecol.common.model.TileAssert
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.UnitAssert.assertThat
import net.sf.freecol.common.model.ai.missions.MissionHandlerBaseTestClass
import net.sf.freecol.common.model.player.Player
import net.sf.freecol.common.model.player.Stance
import net.sf.freecol.common.model.player.Tension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import promitech.colonization.orders.combat.givenCombatWinProbability

internal class WanderMissionHandlerTest : MissionHandlerBaseTestClass() {

    @Test
    fun `should not attack when good stance`() {
        // given
        val indian = game.players.getById("player:154")
        val brave = indian.units.getById("unit:6181")

        // when
        val tileToAggression = AttackDecision.determineTileToAggression(game.map, brave)

        // then
        assertThat(tileToAggression).isNull()
    }

    @Test
    fun `should find tile to attack when bad stance`() {
        // given
        val indian = game.players.getById("player:154")
        val brave = indian.units.getById("unit:6181")

        givenWarStance(brave, dutch)

        // when
        val tileToAggression = AttackDecision.determineTileToAggression(game.map, brave)

        // then
        TileAssert.assertThat(tileToAggression).isEquals(20,73)
    }

    @Test
    fun `should attack unit`() {
        // given
        val indian = game.players.getById("player:154")
        val missionContainer = game.aiContainer.missionContainer(indian)
        missionContainer.clearAllMissions()

        val brave = indian.units.getById("unit:6181")
        val wanderMission = WanderMission(brave)
        missionContainer.addMission(wanderMission)

        givenWarStance(brave, dutch)

        val targetUnitLocation = game.map.getTile(20, 73)
        val targetUnit = targetUnitLocation.units.first()

        givenCombatWinProbability()

        // when
        newTurnAndExecuteMission(indian, 1)

        // then
        assertThat(brave).isNotDisposed
        assertThat(targetUnit).isDisposed
            .isNotAtLocation(targetUnitLocation)
    }


    private fun givenWarStance(unit: Unit, player: Player) {
        unit.owner.changeStance(player, Stance.WAR)
        unit.owner.settlements.getById(unit.indianSettlementId)
            .asIndianSettlement()
            .setTension(player, Tension.Level.HATEFUL.limit)

    }
}