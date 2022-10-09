package net.sf.freecol.common.model.ai.missions.transportunit

import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.Map
import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.Turn
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.ai.missions.AbstractMission
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.ai.missions.UnitMissionsMapping
import net.sf.freecol.common.model.ai.missions.hasMission
import net.sf.freecol.common.util.Predicate
import promitech.colonization.savegame.XmlNodeAttributes
import promitech.colonization.savegame.XmlNodeAttributesWriter

class TransportUnitRequestMission : AbstractMission {

    companion object {
        @JvmField
        val isFromEurope: Predicate<TransportUnitRequestMission> = object : Predicate<TransportUnitRequestMission> {
            override fun test(mission: TransportUnitRequestMission): Boolean {
                return mission.isUnitAtEuropeLocation()
            }
        }

        @JvmField
        val isFromTileLocation: Predicate<TransportUnitRequestMission> = object : Predicate<TransportUnitRequestMission> {
            override fun test(mission: TransportUnitRequestMission): Boolean {
                return mission.isUnitAtTileLocation()
            }
        }

        @JvmField
        val hasNotTransportUnitMission: Predicate<TransportUnitRequestMission> = object : Predicate<TransportUnitRequestMission> {
            override fun test(mission: TransportUnitRequestMission): Boolean {
                return !mission.hasTransportUnitMission() && mission.worthEmbark
            }
        }

        @JvmStatic
        fun isTransportHasParentType(
            playerMissionContainer: PlayerMissionsContainer,
            clazz: Class<out AbstractMission>
        ): Predicate<TransportUnitRequestMission> {
            return object : Predicate<TransportUnitRequestMission> {
                override fun test(mission: TransportUnitRequestMission): Boolean {
                    return playerMissionContainer.findParentMission(mission, clazz) != null
                }
            }
        }

        @JvmStatic
        fun isAtShipLocation(navyUnit: Unit): Predicate<TransportUnitRequestMission> {
            return object : Predicate<TransportUnitRequestMission> {
                override fun test(mission: TransportUnitRequestMission): Boolean {
                    return mission.isUnitAt(navyUnit)
                }
            }
        }
    }

    val createdTurn: Turn
    val unit: Unit
    val destination: Tile
    var transportUnitMissionId: String? = null
    var allowMoveToDestination: Boolean = false
    private var worthEmbark: Boolean = true
    private var worthEmbarkRange: Int = 0

    constructor(
        createdTurn: Turn,
        unit: Unit,
        destination: Tile,
        allowMoveToDestination: Boolean = false,
        worthEmbark: Boolean = true,
        worthEmbarkRange: Int = 0
    ) : super(Game.idGenerator.nextId(TransportUnitRequestMission::class.java)) {
        this.createdTurn = createdTurn
        this.unit = unit
        this.destination = destination
        this.allowMoveToDestination = allowMoveToDestination
        this.worthEmbark = worthEmbark
        this.worthEmbarkRange = worthEmbarkRange
    }

    private constructor(missionId: String, unit: Unit, destination: Tile, createdTurn: Turn) : super(missionId) {
        this.createdTurn = createdTurn
        this.unit = unit
        this.destination = destination
    }

    override fun blockUnits(unitMissionsMapping: UnitMissionsMapping) {
        unitMissionsMapping.blockUnit(this.unit, this)
    }
    override fun unblockUnits(unitMissionsMapping: UnitMissionsMapping) {
        unitMissionsMapping.unblockUnitFromMission(this.unit, this)
    }

    override fun toString(): String {
        return "transportUnitRequestMission: ${id}, unit: ${unit.id}, dest: ${destination.toPrettyString()}, parent: ${parentMissionId}"
    }

    fun hasTransportUnitMission() : Boolean = transportUnitMissionId != null

    fun isUnitAtEuropeLocation() : Boolean = unit.isAtEuropeLocation
    fun isUnitAtTileLocation() : Boolean = unit.isAtTileLocation
    fun isUnitAtDestination(): Boolean = unit.isAtLocation(destination)
    fun isDestinationOnTheSameIsland(map: Map): Boolean = map.isTheSameArea(destination, unit.tile)
    fun isUnitAt(tile: Tile): Boolean = unit.isAtLocation(tile)
    fun isUnitAt(carrier: Unit): Boolean = carrier.unitContainer.isContainUnit(unit)
    fun isWorthEmbark(): Boolean = worthEmbark

    fun recalculateWorthEmbark(turnsCost: Int) {
        if (turnsCost <= worthEmbarkRange) {
            worthEmbark = false
        }
    }

    fun resetTransportMissionIdWhenDoNotExists(playerMissionsContainer: PlayerMissionsContainer) {
        if (transportUnitMissionId != null && !playerMissionsContainer.hasMission(transportUnitMissionId!!)) {
            transportUnitMissionId = null
        }
    }

    class Xml : AbstractMission.Xml<TransportUnitRequestMission>() {
        private val ATTR_UNIT = "unit"
        private val ATTR_DESTINATION = "dest"
        private val ATTR_TRANSPORT_MISSION_ID = "tranMissionId"
        private val ATTR_ALLOW_MOVE_TO_DEST_ID = "allowMoveToDest"
        private val ATTR_TURN = "turn"

        private val ATTR_WORTH_EMBARK = "worthEmbark"
        private val ATTR_WORTH_EMBARK_RANGE = "worthEmbarkRange"

        override fun startElement(attr: XmlNodeAttributes) {
            val unit = PlayerMissionsContainer.Xml.getPlayerUnit(attr.getStrAttribute(ATTR_UNIT))
            val destination = game.map.getSafeTile(attr.getPoint(ATTR_DESTINATION))

            nodeObject = TransportUnitRequestMission(attr.id, unit, destination, Turn(attr.getIntAttribute(ATTR_TURN)))
            nodeObject.transportUnitMissionId = attr.getStrAttribute(ATTR_TRANSPORT_MISSION_ID)
            nodeObject.allowMoveToDestination = attr.getBooleanAttribute(ATTR_ALLOW_MOVE_TO_DEST_ID, false)
            nodeObject.worthEmbark = attr.getBooleanAttribute(ATTR_WORTH_EMBARK, true)
            nodeObject.worthEmbarkRange = attr.getIntAttribute(ATTR_WORTH_EMBARK_RANGE, 0)
            super.startElement(attr)
        }

        override fun startWriteAttr(
            mission: TransportUnitRequestMission,
            attr: XmlNodeAttributesWriter
        ) {
            super.startWriteAttr(mission, attr)

            attr.setId(mission)
            attr.set(ATTR_UNIT, mission.unit)
            attr.setPoint(ATTR_DESTINATION, mission.destination.x, mission.destination.y)
            attr.set(ATTR_TRANSPORT_MISSION_ID, mission.transportUnitMissionId)
            attr.set(ATTR_ALLOW_MOVE_TO_DEST_ID, mission.allowMoveToDestination, false)
            attr.set(ATTR_WORTH_EMBARK, mission.worthEmbark, true)
            attr.set(ATTR_WORTH_EMBARK_RANGE, mission.worthEmbarkRange, 0)
            attr.set(ATTR_TURN, mission.createdTurn.number)
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