package promitech.colonization.ai.military

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import promitech.colonization.ai.military.DefencePlaner.Companion.FLOAT_UNKNOWN_VALUE
import promitech.colonization.ai.military.DefencePlaner.Companion.UNKNOWN_VALUE
import promitech.colonization.savegame.Savegame1600BaseClass
import java.util.Collections.sort

class ColonyDefenceTest : Savegame1600BaseClass() {

    @Test
    fun `should sort by require more defence`() {
        // given
        val miquelon = ColonyThreatWeights(2000, 1f, 3)
        val miquelon2 = ColonyThreatWeights(2000, -2f, 3)
        val charlesfort = ColonyThreatWeights(8725, 1f, 1)
        val villedeQubec = ColonyThreatWeights(8530, 1f, 2)
        val tadoussac = ColonyThreatWeights(1750, 1f, 2)

        val list = listOf(
            miquelon,
            miquelon2,
            charlesfort,
            villedeQubec,
            tadoussac
        )
        // when
        sort(list, ColonyThreatWeights.REQUIRE_MORE_DEFENCE_COMPARATOR)

        println("colonyWealth, threadPower, threadProjection")
        list.forEach { it ->
            println("${it.colonyWealth}, ${it.threatPower}, ${it.threatProjection}")
        }

        // then
        assertTrue(list.get(0) == miquelon)
        assertTrue(list.get(1) == villedeQubec)
        assertTrue(list.get(2) == tadoussac)
        assertTrue(list.get(3) == charlesfort)
        assertTrue(list.get(4) == miquelon2)
    }

    @Test
    fun `should sort by require more defence 2`() {
        // given
        val miquelon = ColonyThreatWeights(2000, 1f, 3)
        val miquelon2 = ColonyThreatWeights(2000, -2f, 3)
        val charlesfort = ColonyThreatWeights(8725, 1f, 1)
        val villedeQubec = ColonyThreatWeights(8530, 1f, 2)
        val tadoussac = ColonyThreatWeights(1750, 1f, 2)

        // when
        // then
        assertTrue(miquelon.isRequireMoreDefence(villedeQubec))
        assertTrue(villedeQubec.isRequireMoreDefence(tadoussac))
        assertTrue(tadoussac.isRequireMoreDefence(charlesfort))
        assertTrue(charlesfort.isRequireMoreDefence(miquelon2))
    }

    @Test
    fun `should sort by require more defence without threat power`() {
        // given
        val miquelon = ColonyThreatWeights(2000, FLOAT_UNKNOWN_VALUE, UNKNOWN_VALUE)
        val miquelon2 = ColonyThreatWeights(2000, -2f, 3)
        val charlesfort = ColonyThreatWeights(8725, 1f, 1)
        val villedeQubec = ColonyThreatWeights(8530, 1f, 2)
        val tadoussac = ColonyThreatWeights(1750, FLOAT_UNKNOWN_VALUE, UNKNOWN_VALUE)

        val list = listOf(
            miquelon,
            miquelon2,
            charlesfort,
            villedeQubec,
            tadoussac
        )
        // when
        sort(list, ColonyThreatWeights.REQUIRE_MORE_DEFENCE_COMPARATOR)

        println("colonyWealth, threadPower, threadProjection")
        list.forEach { it ->
            println("${it.colonyWealth}, ${it.threatPower}, ${it.threatProjection}")
        }

        // then
        assertTrue(list.get(0) == villedeQubec)
        assertTrue(list.get(1) == charlesfort)
        assertTrue(list.get(2) == miquelon2)
        assertTrue(list.get(3) == miquelon)
        assertTrue(list.get(4) == tadoussac)
    }

}