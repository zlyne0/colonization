package net.sf.freecol.common.model

import net.sf.freecol.common.model.specification.GoodsType

inline fun UnitRole.requiredGoodsForRoleCount2(roleCount: Int, action: (goodsType: GoodsType, amount: Int) -> kotlin.Unit) {
    if (requiredGoods.isNotEmpty) {
        for (rg in requiredGoods) {
            action.invoke(rg.goodsType, rg.amount * roleCount)
        }
    }
}