package net.sf.freecol.common.model.colonyproduction

import com.badlogic.gdx.utils.ObjectIntMap
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.specification.GoodsType
import promitech.colonization.savegame.XmlNodeAttributes
import promitech.colonization.savegame.XmlNodeAttributesWriter
import promitech.colonization.savegame.XmlNodeParser

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

    fun amount(type: GoodsType) : Int {
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

    fun toPrettyString(): String {
        if (goods.size == 0) {
            return ""
        }
        var str = ""
        for (goodEntry in goods) {
            if (str.isNotEmpty()) {
                str += ", "
            }
            str += goodEntry.type().id + ":" + goodEntry.amount()
        }
        return str
    }

    override fun iterator(): Iterator<GoodsEntry> {
        return this.goods.entries();
    }

    class Xml : XmlNodeParser<GoodsCollection>() {
        private val ATTR_AMOUNT = "amount"
        private val ATTR_TYPE = "type"
        private val ELEMENT_GOODS = "goods"

        override fun startElement(attr: XmlNodeAttributes) {
            nodeObject = GoodsCollection()
        }

        override fun startReadChildren(attr: XmlNodeAttributes) {
            if (attr.isQNameEquals(ELEMENT_GOODS)) {
                nodeObject.add(
                    attr.getEntity(ATTR_TYPE, Specification.instance.goodsTypes),
                    attr.getIntAttribute(ATTR_AMOUNT)
                )
            }
        }

        override fun startWriteAttr(gc: GoodsCollection, attr: XmlNodeAttributesWriter) {
            for (goodsEntry in gc.entries()) {
                if (goodsEntry.amount() != 0) {
                    attr.xml.element(ELEMENT_GOODS)
                    attr.set(ATTR_TYPE, goodsEntry.type())
                    attr.set(ATTR_AMOUNT, goodsEntry.amount())
                    attr.xml.pop()
                }
            }
        }

        override fun getTagName(): String {
            return tagName()
        }

        companion object {
            @JvmStatic
            fun tagName(): String {
                return "goodsCollection"
            }
        }
    }

}

