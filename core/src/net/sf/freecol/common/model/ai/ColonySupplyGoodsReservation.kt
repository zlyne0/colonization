package net.sf.freecol.common.model.ai

import net.sf.freecol.common.model.ObjectWithId
import net.sf.freecol.common.model.ai.missions.MissionId
import net.sf.freecol.common.model.colonyproduction.GoodsCollection
import promitech.colonization.savegame.ObjectFromNodeSetter
import promitech.colonization.savegame.XmlNodeAttributes
import promitech.colonization.savegame.XmlNodeAttributesWriter
import promitech.colonization.savegame.XmlNodeParser

class ColonySupplyGoodsReservation(val missionId: MissionId): ObjectWithId(missionId) {

    var goods: GoodsCollection = GoodsCollection()
        private set

    class Xml : XmlNodeParser<ColonySupplyGoodsReservation>() {

        init {
            addNode(GoodsCollection::class.java, object: ObjectFromNodeSetter<ColonySupplyGoodsReservation, GoodsCollection> {
                override fun set(target: ColonySupplyGoodsReservation, entity: GoodsCollection) {
                    target.goods = entity
                }
                override fun generateXml(source: ColonySupplyGoodsReservation, xmlGenerator: ObjectFromNodeSetter.ChildObject2XmlCustomeHandler<GoodsCollection>) {
                    xmlGenerator.generateXml(source.goods)
                }
            })
        }

        override fun startElement(attr: XmlNodeAttributes) {
            nodeObject = ColonySupplyGoodsReservation(attr.getStrAttribute(ATTR_MISSION))
        }

        override fun startWriteAttr(node: ColonySupplyGoodsReservation, attr: XmlNodeAttributesWriter) {
            attr.set(ATTR_MISSION, node.missionId)
        }

        override fun getTagName(): String {
            return tagName()
        }

        companion object {
            val ATTR_MISSION = "mission"

            @JvmStatic
            fun tagName(): String {
                return "colonySupplyGoodsReservation"
            }
        }
    }

}