package net.sf.freecol.common.model.colonyproduction

import net.sf.freecol.common.model.specification.GoodsType
import org.junit.jupiter.api.Test
import promitech.colonization.savegame.Savegame1600BaseClass

internal class GoodsCollectionTest : Savegame1600BaseClass() {

    @Test
    fun x() {
        val goodsCollection = GoodsCollection()
        goodsCollection.add(goodsType(GoodsType.MUSKETS), 10)

        for (entry in goodsCollection) {
            println("${entry.type()}  ${entry.amount()}")
        }
    }

}

