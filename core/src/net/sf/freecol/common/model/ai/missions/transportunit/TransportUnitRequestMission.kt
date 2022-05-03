package net.sf.freecol.common.model.ai.missions.transportunit

import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.ai.missions.AbstractMission
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.ai.missions.UnitMissionsMapping
import promitech.colonization.savegame.XmlNodeAttributes
import promitech.colonization.savegame.XmlNodeAttributesWriter

class TransportUnitRequestMission : AbstractMission {

    val unit: Unit
    val destination: Tile
    var transportUnitMissionId: String? = null

    constructor(
        unit: Unit,
        destination: Tile
    ) : super(Game.idGenerator.nextId(TransportUnitRequestMission::class.java)) {
        this.unit = unit
        this.destination = destination
    }

    private constructor(missionId: String, unit: Unit, destination: Tile) : super(missionId) {
        this.unit = unit
        this.destination = destination
    }

    override fun blockUnits(unitMissionsMapping: UnitMissionsMapping) {
        unitMissionsMapping.blockUnit(this.unit, this)
    }
    override fun unblockUnits(unitMissionsMapping: UnitMissionsMapping) {
        unitMissionsMapping.unblockUnitFromMission(this.unit, this)
    }

    fun hasTransportUnitMission() : Boolean = transportUnitMissionId != null

    fun isUnitAtEuropeLocation() : Boolean = unit.isAtEuropeLocation

    class Xml : AbstractMission.Xml<TransportUnitRequestMission>() {
        private val ATTR_UNIT = "unit"
        private val ATTR_DESTINATION = "dest"
        private val ATTR_TRANSPORT_MISSION_ID = "tranMissionId"

        override fun startElement(attr: XmlNodeAttributes) {
            val unit = PlayerMissionsContainer.Xml.getPlayerUnit(attr.getStrAttribute(ATTR_UNIT))
            val destination = game.map.getSafeTile(attr.getPoint(ATTR_DESTINATION))

            nodeObject = TransportUnitRequestMission(attr.id, unit, destination)
            nodeObject.transportUnitMissionId = attr.getStrAttribute(ATTR_TRANSPORT_MISSION_ID)

            super.startElement(attr)
        }

        override fun startWriteAttr(mission: TransportUnitRequestMission, attr: XmlNodeAttributesWriter) {
            super.startWriteAttr(mission, attr)

            attr.setId(mission)
            attr.set(ATTR_UNIT, mission.unit)
            attr.setPoint(ATTR_DESTINATION, mission.destination.x, mission.destination.y)
            attr.set(ATTR_TRANSPORT_MISSION_ID, mission.transportUnitMissionId)
        }

        override fun getTagName(): String {
            return tagName()
        }

        companion object {
            @JvmStatic
            fun tagName(): String {
                return "transportUnitRequestMission"
            }
        }
    }
}