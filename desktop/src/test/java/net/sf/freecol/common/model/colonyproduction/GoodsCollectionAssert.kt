package net.sf.freecol.common.model.colonyproduction

import net.sf.freecol.common.model.specification.GoodsType
import org.assertj.core.api.AbstractAssert

class GoodsCollectionAssert(goodsCollection: GoodsCollection)
    : AbstractAssert<GoodsCollectionAssert, GoodsCollection>(goodsCollection, GoodsCollectionAssert::class.java)
{

    fun has(goodsType: GoodsType, amount: Int): GoodsCollectionAssert {
        val actualAmount = actual.amount(goodsType)
        if (actualAmount != amount) {
            failWithMessage("expected goods collection has %s amount but it has %s of goods type %s", amount, actualAmount, goodsType)
        }
        return this
    }

    companion object {
        @JvmStatic
        fun assertThat(goodsCollection: GoodsCollection): GoodsCollectionAssert {
            return GoodsCollectionAssert(goodsCollection)
        }
    }
}