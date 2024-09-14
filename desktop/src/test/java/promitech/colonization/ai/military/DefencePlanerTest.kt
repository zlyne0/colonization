package promitech.colonization.ai.military

import net.sf.freecol.common.model.ColonyAssert.assertThat
import net.sf.freecol.common.model.UnitFactory
import net.sf.freecol.common.model.UnitRole
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.map.path.PathFinder
import org.junit.jupiter.api.Test
import promitech.colonization.savegame.Savegame1600BaseClass

class DefencePlanerTest : Savegame1600BaseClass() {

    @Test
    fun `should determine colony to protect`() {
        // given
        val defencePlaner = DefencePlaner(game, PathFinder())
        val dragoon = UnitFactory.create(UnitType.VETERAN_SOLDIER, UnitRole.DRAGOON, dutch, dutch.europe)

        // when
        val priorities = defencePlaner.calculateColonyDefencePriorities(dragoon.owner)

        // then
        assertThat(priorities.get(0).colony).isEqualId(fortNassau)
        assertThat(priorities.get(1).colony).isEqualId(fortMaurits)
        assertThat(priorities.get(2).colony).isEqualId(fortOranje)
        assertThat(priorities.get(3).colony).isEqualId(nieuwAmsterdam)
    }

}