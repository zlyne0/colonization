package net.sf.freecol.common.model.ai

import net.sf.freecol.common.model.ColonyId
import net.sf.freecol.common.model.MapIdEntities
import net.sf.freecol.common.model.ObjectWithId
import net.sf.freecol.common.model.ai.missions.MissionId
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.ai.missions.hasMission
import net.sf.freecol.common.model.colonyproduction.GoodsCollection
import net.sf.freecol.common.model.getByIdOrCreate
import net.sf.freecol.common.model.specification.GoodsType
import promitech.colonization.savegame.XmlNodeAttributes
import promitech.colonization.savegame.XmlNodeAttributesWriter
import promitech.colonization.savegame.XmlNodeParser

class ColonySupplyGoods(
    val colonyId: ColonyId
): ObjectWithId(colonyId) {

    val supplyReservations: MapIdEntities<ColonySupplyGoodsReservation> = MapIdEntities()

    fun removeSupplyReservation(missionId: MissionId) {
        supplyReservations.removeId(missionId)
    }

    fun isEmpty(): Boolean = supplyReservations.isEmpty()

    fun makeReservation(missionId: MissionId, goodsCollection: GoodsCollection) {
        supplyReservations.getByIdOrCreate(missionId, { ColonySupplyGoodsReservation(missionId) } )
            .goods.add(goodsCollection)
    }

    fun reservationSum(goodsType: GoodsType, ignoreReservationsForMission: MissionId?): Int {
        var reservationSum = 0
        for (supplyReservation in supplyReservations) {
            if (ignoreReservationsForMission != null && supplyReservation.missionId.equals(ignoreReservationsForMission)) {
                continue
            }
            reservationSum += supplyReservation.goods.amount(goodsType)
        }
        return reservationSum
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
                supplyReservations.removeId(colonySupplyGoodsReservation)
            }
        }
    }

    class Xml : XmlNodeParser<ColonySupplyGoods>() {

        init {
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