package promitech.colonization.ai.military

import net.sf.freecol.common.model.UnitFactory
import net.sf.freecol.common.model.UnitRole
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.map.path.PathFinder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import promitech.colonization.savegame.Savegame1600BaseClass

class DefencePlanerTest : Savegame1600BaseClass() {

    @Test
    fun `should determine colony to protect`() {
        // given
        val defencePlaner = DefencePlaner(game, PathFinder())
        val dragoon = UnitFactory.create(UnitType.VETERAN_SOLDIER, UnitRole.DRAGOON, dutch, dutch.europe)

        // when
        val colonyToProtect = defencePlaner.findColonyToProtect(dragoon.owner)

        // then
        assertThat(colonyToProtect!!.id).isEqualTo(fortNassau.id)
    }

}