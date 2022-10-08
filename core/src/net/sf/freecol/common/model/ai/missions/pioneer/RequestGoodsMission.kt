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
import net.sf.freecol.common.util.Predicate
import promitech.colonization.ai.MissionHandlerLogger.logger
import promitech.colonization.savegame.ObjectFromNodeSetter
import promitech.colonization.savegame.XmlNodeAttributes
import promitech.colonization.savegame.XmlNodeAttributesWriter

class RequestGoodsMission : AbstractMission {

    companion object {
        @JvmField
        val isBoughtPredicate = object: Predicate<RequestGoodsMission> {
            override fun test(mission: RequestGoodsMission): Boolean {
                return mission.boughtInEurope
            }
        }
    }

    var colonyId: String
        private set

    lateinit var goodsCollection: GoodsCollection
        private set

    val purpose: String
    var boughtInEurope = false
        private set

    constructor(
        colony: Colony,
        goodsCollection: GoodsCollection,
        purpose: String = "",
        boughtInEurope: Boolean = false
    ) : super(Game.idGenerator.nextId(RequestGoodsMission::class.java)) {
        this.colonyId = colony.id
        this.goodsCollection = goodsCollection
        this.purpose = purpose
        this.boughtInEurope = boughtInEurope
    }

    private constructor(missionId: String, colonyId: String, purpose: String = "", boughtInEurope: Boolean = false) : super(missionId) {
        this.colonyId = colonyId
        this.purpose = purpose
        this.boughtInEurope = boughtInEurope
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

    override fun toString(): String {
        return "id: " + getId() + ", colonyId: " + colonyId + ", purpose: " + purpose + ", goodsCollection: " + goodsCollection.toPrettyString()
    }

    fun setAsBoughtInEurope() {
        boughtInEurope = true
    }

    class Xml : AbstractMission.Xml<RequestGoodsMission>() {
        private val ATTR_COLONY = "colony"
        private val ATTR_PURPOSE = "purpose"
        private val ATTR_BOUGHT_IN_EUROPE = "bought"

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
                attr.getStrAttribute(ATTR_PURPOSE, ""),
                attr.getBooleanAttribute(ATTR_BOUGHT_IN_EUROPE, false)
            )
            super.startElement(attr)
        }

        override fun startWriteAttr(mission: RequestGoodsMission, attr: XmlNodeAttributesWriter) {
            super.startWriteAttr(mission, attr)

            attr.setId(mission)
            attr.set(ATTR_COLONY, mission.colonyId)
            attr.set(ATTR_PURPOSE, mission.purpose, "")
            attr.set(ATTR_BOUGHT_IN_EUROPE, mission.boughtInEurope, false)
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