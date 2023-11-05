package net.sf.freecol.common.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import promitech.colonization.ai.UnitRoleId
import promitech.colonization.ai.UnitTypeId
import promitech.colonization.savegame.Savegame1600BaseClass

class KEuropeTest : Savegame1600BaseClass() {

    data class UnitPrice(val unitTypeId: UnitTypeId, val unitRoleId: UnitRoleId, val expectedPrice: Int)

    @Test
    fun `should calculate price for units`() {
        // given
        val testCases = listOf(
            UnitPrice(UnitType.ARTILLERY, UnitRole.DEFAULT_ROLE_ID, 500),
            UnitPrice(UnitType.FREE_COLONIST, UnitRole.SOLDIER, 750),
            UnitPrice(UnitType.FREE_COLONIST, UnitRole.DRAGOON, 900),
            UnitPrice(UnitType.VETERAN_SOLDIER, UnitRole.SOLDIER, 2000),
            UnitPrice(UnitType.VETERAN_SOLDIER, UnitRole.DRAGOON, 2150),
        )

        testCases.forEach { aCase ->
            // when
            val price = dutch.europe.aiUnitPrice(unitType(aCase.unitTypeId), unitRole(aCase.unitRoleId))
            // then
            assertThat(price)
                .describedAs("case " + aCase.unitTypeId + " " + aCase.unitRoleId)
                .isEqualTo(aCase.expectedPrice)
        }
    }
}