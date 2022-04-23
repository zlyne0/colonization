package net.sf.freecol.common.model.ai.missions.pioneer

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.GoodsContainer
import net.sf.freecol.common.model.ai.missions.AbstractMission
import net.sf.freecol.common.model.ai.missions.UnitMissionsMapping
import net.sf.freecol.common.model.colonyproduction.GoodsCollection
import net.sf.freecol.common.model.colonyproduction.amount
import net.sf.freecol.common.model.colonyproduction.type
import net.sf.freecol.common.model.specification.GoodsType
import promitech.colonization.ai.MissionHandlerLogger.logger
import promitech.colonization.savegame.ObjectFromNodeSetter
import promitech.colonization.savegame.XmlNodeAttributes
import promitech.colonization.savegame.XmlNodeAttributesWriter

class RequestGoodsMission : AbstractMission {

    var colonyId: String
        private set

    private lateinit var goodsCollection: GoodsCollection

    val purpose: String

    constructor(
        colony: Colony,
        goodsCollection: GoodsCollection,
        purpose: String = ""
    ) : super(Game.idGenerator.nextId(RequestGoodsMission::class.java)) {
        this.colonyId = colony.id
        this.goodsCollection = goodsCollection
        this.purpose = purpose
    }

    private constructor(missionId: String, colonyId: String, purpose: String = "") : super(missionId) {
        this.colonyId = colonyId
        this.purpose = purpose
    }

    override fun blockUnits(unitMissionsMapping: UnitMissionsMapping) { }
    override fun unblockUnits(unitMissionsMapping: UnitMissionsMapping) { }

    fun amount(type: GoodsType): Int {
        return goodsCollection.amount(type)
    }

    fun addGoodsToColony(colony: Colony) {
        for (goods in goodsCollection) {
            colony.goodsContainer.increaseGoodsQuantity(goods.type(), goods.amount())
        }
        if (logger.isDebug) {
            logger.debug("player[%s].RequestGoodsMissionHandler.colony[%s] add goods %s",
                colony.owner.id,
                colony.id,
                goodsCollection.toPrettyString()
            )
        }
    }

    class Xml : AbstractMission.Xml<RequestGoodsMission>() {
        private val ATTR_COLONY = "colony"
        private val ATTR_PURPOSE = "purpose"

        init {
            addNode(GoodsCollection::class.java, object: ObjectFromNodeSetter<RequestGoodsMission, GoodsCollection> {
                override fun set(target: RequestGoodsMission, entity: GoodsCollection) {
                    target.goodsCollection = entity
                }
                override fun generateXml(source: RequestGoodsMission, xmlGenerator: ObjectFromNodeSetter.ChildObject2XmlCustomeHandler<GoodsCollection>) {
                    xmlGenerator.generateXml(source.goodsCollection)
                }
            })
        }

        override fun startElement(attr: XmlNodeAttributes) {
            nodeObject = RequestGoodsMission(
                attr.id,
                attr.getStrAttribute(ATTR_COLONY),
                attr.getStrAttribute(ATTR_PURPOSE, "")
            )

            super.startElement(attr)
        }

        override fun startWriteAttr(mission: RequestGoodsMission, attr: XmlNodeAttributesWriter) {
            super.startWriteAttr(mission, attr)

            attr.setId(mission)
            attr.set(ATTR_COLONY, mission.colonyId)
            attr.set(ATTR_PURPOSE, mission.purpose, "")
        }

        override fun getTagName(): String {
            return tagName()
        }

        companion object {
            @JvmStatic
            fun tagName(): String {
                return "requestGoodsMission"
            }
        }
    }
}