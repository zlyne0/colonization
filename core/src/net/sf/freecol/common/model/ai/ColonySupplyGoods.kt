package net.sf.freecol.common.model.ai

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.ColonyId
import net.sf.freecol.common.model.MapIdEntities
import net.sf.freecol.common.model.ObjectWithId
import net.sf.freecol.common.model.ai.missions.MissionId
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.ai.missions.hasMission
import net.sf.freecol.common.model.colonyproduction.GoodsCollection
import net.sf.freecol.common.model.getByIdOrCreate
import net.sf.freecol.common.model.player.Player
import net.sf.freecol.common.model.specification.GoodsType
import promitech.colonization.savegame.ObjectFromNodeSetter
import promitech.colonization.savegame.XmlNodeAttributes
import promitech.colonization.savegame.XmlNodeAttributesWriter
import promitech.colonization.savegame.XmlNodeParser
import java.lang.IllegalStateException

class ColonySupplyGoods(
    val colonyId: ColonyId
): ObjectWithId(colonyId) {

    var supplyGoods: GoodsCollection = GoodsCollection()
        private set

    val supplyReservations: MapIdEntities<ColonySupplyGoodsReservation> = MapIdEntities()

    fun addSupply(goodsCollection: GoodsCollection) {
        supplyGoods.add(goodsCollection)
    }

    fun removeSupplyReservation(missionId: MissionId) {
        supplyReservations.removeId(missionId)
    }

    fun isEmpty(): Boolean = supplyGoods.isEmpty() && supplyReservations.isEmpty()

    fun makeReservation(missionId: MissionId, goodsType: GoodsType, amount: Int) {
        if (!supplyGoods.has(goodsType, amount)) {
            throw IllegalStateException("not supply goods to make reservation: " + goodsType.id + ", amount: " + amount)
        }
        supplyGoods.remove(goodsType, amount)
        supplyReservations.getByIdOrCreate(missionId, { ColonySupplyGoodsReservation(missionId) } )
            .goods.add(goodsType, amount)
    }

    fun makeReservation(missionId: MissionId, goodsCollection: GoodsCollection) {
        if (!supplyGoods.has(goodsCollection)) {
            throw IllegalStateException("not supply goods to make reservation: " + goodsCollection.toPrettyString())
        }
        supplyGoods.remove(goodsCollection)
        supplyReservations.getByIdOrCreate(missionId, { ColonySupplyGoodsReservation(missionId) } )
            .goods.add(goodsCollection)
    }

    fun hasSupplyGoods(goodsCollection: GoodsCollection): Boolean {
        return supplyGoods.has(goodsCollection)
    }

    fun removeOutdatedReservations(playerMissionsContainer: PlayerMissionsContainer) {
        var tmpToRemove: ArrayList<ColonySupplyGoodsReservation>? = null
        for (supplyReservation in supplyReservations) {
            if (!playerMissionsContainer.hasMission(supplyReservation.missionId)) {
                if (tmpToRemove == null) {
                    tmpToRemove = ArrayList<ColonySupplyGoodsReservation>()
                }
                tmpToRemove.add(supplyReservation)
            }
        }
        if (tmpToRemove != null) {
            for (colonySupplyGoodsReservation in tmpToRemove) {
                supplyGoods.add(colonySupplyGoodsReservation.goods)
                supplyReservations.removeId(colonySupplyGoodsReservation)
            }
        }
    }

    fun clearWhenNoColonyGoods(player: Player) {
        val settlement = player.settlements.getByIdOrNull(colonyId)
        if (!settlement.goodsContainer.hasMoreOrEquals(supplyGoods)) {
            supplyGoods.clear()
        }
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