package net.sf.freecol.common.model.ai.missions.scout

import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.UnitId
import net.sf.freecol.common.model.ai.missions.AbstractMission
import net.sf.freecol.common.model.ai.missions.UnitMissionsMapping
import promitech.colonization.savegame.XmlNodeAttributes
import promitech.colonization.savegame.XmlNodeAttributesWriter

class ScoutMission : AbstractMission {

    enum class Phase {
        SCOUT, WAIT_FOR_TRANSPORT, NOTHING
    }

    val scoutId: UnitId
    var phase: Phase = Phase.SCOUT
        private set

    // scout destination used only for other island
    var scoutDistantDestination: Tile? = null
        private set

    constructor(scout: Unit) : super(Game.idGenerator.nextId(ScoutMission::class.java)) {
        this.scoutId = scout.id
    }

    private constructor(missionId: String, scoutId: UnitId) : super(missionId) {
        this.scoutId = scoutId
    }

    override fun toString(): String {
        return "scout: " + scoutId + ", phase: " + phase
    }

    override fun blockUnits(unitMissionsMapping: UnitMissionsMapping) {
        unitMissionsMapping.blockUnit(scoutId, this)
    }

    override fun unblockUnits(unitMissionsMapping: UnitMissionsMapping) {
        unitMissionsMapping.unblockUnitFromMission(scoutId, this)
    }

    fun waitForTransport(scoutDistantDestination: Tile) {
        this.phase = Phase.WAIT_FOR_TRANSPORT
        this.scoutDistantDestination = scoutDistantDestination
    }

    fun isWaitingForTransport(): Boolean {
        return this.phase == Phase.WAIT_FOR_TRANSPORT
    }

    fun startScoutAfterTransport(game: Game, scout: Unit) {
        if (game.map.isTheSameArea(scoutDistantDestination, scout.tile)) {
            phase = Phase.SCOUT
            scoutDistantDestination = null
        }
    }

    fun setDoNothing() {
        phase = Phase.NOTHING
    }

    class Xml : AbstractMission.Xml<ScoutMission>() {
        private val ATTR_UNIT = "unit"
        private val ATTR_PHASE = "phase"
        private val ATTR_DESTINATION = "dest"

        override fun startElement(attr: XmlNodeAttributes) {
            nodeObject = ScoutMission(attr.id, attr.getStrAttribute(ATTR_UNIT))
            nodeObject.phase = attr.getEnumAttribute(Phase::class.java, ATTR_PHASE)
            if (attr.hasAttr(ATTR_DESTINATION)) {
                nodeObject.scoutDistantDestination = game.map.getSafeTile(attr.getPoint(ATTR_DESTINATION))
            }
            super.startElement(attr)
        }

        override fun startWriteAttr(mission: ScoutMission, attr: XmlNodeAttributesWriter) {
            super.startWriteAttr(mission, attr)

            attr.setId(mission)
            attr.set(ATTR_UNIT, mission.scoutId)
            attr.set(ATTR_PHASE, mission.phase)
            mission.scoutDistantDestination?.let {
                tile -> attr.setPoint(ATTR_DESTINATION, tile.x, tile.y)
            }
        }

        override fun getTagName(): String {
            return tagName()
        }

        companion object {
            @JvmStatic
            fun tagName(): String {
                return "scoutMission"
            }
        }
    }
}