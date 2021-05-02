package net.sf.freecol.common.model.colonyproduction

import com.badlogic.gdx.utils.ObjectIntMap
import net.sf.freecol.common.model.specification.GoodsType

typealias GoodsEntry = ObjectIntMap.Entry<GoodsType>
typealias GoodsEntries = ObjectIntMap.Entries<GoodsType>

inline fun GoodsEntry.type(): GoodsType {
    return this.key
}

inline fun GoodsEntry.amount(): Int {
    return this.value
}

class GoodsCollection : Iterable<ObjectIntMap.Entry<GoodsType>> {

    private val goods : ObjectIntMap<GoodsType> = ObjectIntMap()

    fun amount(type : GoodsType) : Int {
        return goods.get(type, 0)
    }

    fun add(type: GoodsType, amount: Int) {
        this.goods.getAndIncrement(type, 0, amount)
    }

    fun first() : GoodsEntry {
        return this.goods.entries().next();
    }

    fun entries(): GoodsEntries {
        return this.goods.entries()
    }

    fun clear() {
        goods.clear()
    }

    override fun iterator(): Iterator<GoodsEntry> {
        return this.goods.entries();
    }
}

