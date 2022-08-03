package net.sf.freecol.common.model.ai

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.ColonyId
import net.sf.freecol.common.model.MapIdEntities
import net.sf.freecol.common.model.ObjectWithId
import net.sf.freecol.common.model.ai.missions.MissionId
import net.sf.freecol.common.model.colonyproduction.GoodsCollection
import net.sf.freecol.common.model.getByIdOrCreate
import net.sf.freecol.common.model.specification.GoodsType
import promitech.colonization.savegame.ObjectFromNodeSetter
import promitech.colonization.savegame.XmlNodeAttributes
import promitech.colonization.savegame.XmlNodeAttributesWriter
import promitech.colonization.savegame.XmlNodeParser

class ColonySupplyGoods(
    val colonyId: ColonyId
): ObjectWithId(colonyId) {

    lateinit var supplyGoods: GoodsCollection
        private set

    val supplyReservations: MapIdEntities<ColonySupplyGoodsReservation> = MapIdEntities()

    fun addSupply(goodsCollection: GoodsCollection) {
        supplyGoods.add(goodsCollection)
    }

    fun removeSupplyReservation(missionId: MissionId) {
        supplyReservations.removeId(missionId)
    }

    fun isEmpty(): Boolean = supplyGoods.isEmpty() && supplyReservations.isEmpty()

    fun addSupplyGoodsReservation(missionId: MissionId, goodsType: GoodsType, amount: Int) {
        supplyReservations.getByIdOrCreate(colonyId, { ColonySupplyGoodsReservation(missionId) } )
            .goods.add(goodsType, amount)
    }

    class Xml : XmlNodeParser<ColonySupplyGoods>() {

        init {
            addNode("supplyGoods", GoodsCollection::class.java, object: ObjectFromNodeSetter<ColonySupplyGoods, GoodsCollection> {
                override fun set(target: ColonySupplyGoods, entity: GoodsCollection) {
                    target.supplyGoods = entity
                }
                override fun generateXml(source: ColonySupplyGoods, xmlGenerator: ObjectFromNodeSetter.ChildObject2XmlCustomeHandler<GoodsCollection>) {
                    xmlGenerator.generateXml(source.supplyGoods)
                }
            })
            addNodeForMapIdEntities("supplyReservations", ColonySupplyGoodsReservation::class.java)
        }

        override fun startElement(attr: XmlNodeAttributes) {
            nodeObject = ColonySupplyGoods(attr.getStrAttribute(ATTR_COLONY))
        }

        override fun startWriteAttr(node: ColonySupplyGoods, attr: XmlNodeAttributesWriter) {
            attr.set(ATTR_COLONY, node.colonyId)
        }

        override fun getTagName(): String {
            return tagName()
        }

        companion object {

            val ATTR_COLONY = "colony"

            @JvmStatic
            fun tagName(): String {
                return "colonySupplyGoods"
            }
        }
    }
}