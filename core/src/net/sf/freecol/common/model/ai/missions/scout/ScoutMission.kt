package net.sf.freecol.common.model.ai.missions.scout

import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.ai.missions.AbstractMission
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.ai.missions.TransportUnitMission
import net.sf.freecol.common.model.ai.missions.UnitMissionsMapping
import promitech.colonization.ai.CommonMissionHandler
import promitech.colonization.savegame.XmlNodeAttributes
import promitech.colonization.savegame.XmlNodeAttributesWriter

class ScoutMission : AbstractMission {

    public enum class Phase {
        SCOUT, WAIT_FOR_TRANSPORT, NOTHING
    }

    val scout : Unit
    var phase: Phase = Phase.SCOUT
        private set

    // scout destination used only for other island
    var scoutDistantDestination: Tile? = null
        private set

    constructor(scout: Unit) : super(Game.idGenerator.nextId(ScoutMission::class.java)) {
        this.scout = scout
    }

    private constructor(scout: Unit, missionId: String) : super(missionId) {
        this.scout = scout
    }

    override fun toString(): String {
        return "scout: " + scout.id + ", phase: " + phase
    }

    override fun blockUnits(unitMissionsMapping: UnitMissionsMapping) {
        unitMissionsMapping.blockUnit(scout, this)
    }

    override fun unblockUnits(unitMissionsMapping: UnitMissionsMapping) {
        unitMissionsMapping.unblockUnitFromMission(scout, this)
    }

    fun isScoutExists(): Boolean {
        return CommonMissionHandler.isUnitExists(scout.owner, scout)
    }

    fun waitForTransport(scoutDistantDestination: Tile) {
        this.phase = ScoutMission.Phase.WAIT_FOR_TRANSPORT
        this.scoutDistantDestination = scoutDistantDestination
    }

    fun isWaitingForTransport(): Boolean {
        return this.phase == ScoutMission.Phase.WAIT_FOR_TRANSPORT
    }

    fun isScoutReadyToEmbark(embarkLocation: Tile): Boolean {
        val scoutLocation = scout.tile
        return scoutLocation.equalsCoordinates(embarkLocation) || scoutLocation.isStepNextTo(embarkLocation)
    }

    fun startScoutAfterTransport(game: Game) {
        if (game.map.isTheSameArea(scoutDistantDestination, scout.tile)) {
            phase = ScoutMission.Phase.SCOUT
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
            val scout = PlayerMissionsContainer.Xml.getPlayerUnit(attr.getStrAttribute(ATTR_UNIT))
            nodeObject = ScoutMission(scout, attr.id)
            nodeObject.phase = attr.getEnumAttribute(Phase::class.java, ATTR_PHASE)
            if (attr.hasAttr(ATTR_DESTINATION)) {
                nodeObject.scoutDistantDestination = game.map.getSafeTile(attr.getPoint(ATTR_DESTINATION))
            }
            super.startElement(attr)
        }

        override fun startWriteAttr(mission: ScoutMission, attr: XmlNodeAttributesWriter) {
            super.startWriteAttr(mission, attr)

            attr.setId(mission)
            attr.set(ATTR_UNIT, mission.scout)
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