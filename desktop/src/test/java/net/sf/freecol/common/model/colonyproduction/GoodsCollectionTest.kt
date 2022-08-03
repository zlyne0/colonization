package net.sf.freecol.common.model.colonyproduction

import net.sf.freecol.common.model.colonyproduction.GoodsCollectionAssert.Companion.assertThat
import net.sf.freecol.common.model.specification.GoodsType
import org.junit.jupiter.api.Test
import promitech.colonization.savegame.Savegame1600BaseClass

internal class GoodsCollectionTest : Savegame1600BaseClass() {

    @Test
    fun should_add_goods() {
        // given
        val goodsCollection = GoodsCollection()

        // when
        goodsCollection.add(goodsType(GoodsType.MUSKETS), 10)

        // then
        assertThat(goodsCollection).has(goodsType(GoodsType.MUSKETS), 10)
    }

}

