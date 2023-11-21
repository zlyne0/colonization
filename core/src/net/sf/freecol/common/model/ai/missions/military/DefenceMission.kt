package net.sf.freecol.common.model.ai.missions.military

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.Map
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.ai.missions.AbstractMission
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.ai.missions.UnitMissionsMapping
import promitech.colonization.ai.CommonMissionHandler
import promitech.colonization.savegame.XmlNodeAttributes
import promitech.colonization.savegame.XmlNodeAttributesWriter

class DefenceMission: AbstractMission {

    var unit: Unit
        private set
    var colonyId: String
        private set

    constructor(colony: Colony, unit: Unit) : super(Game.idGenerator.nextId(DefenceMission::class.java)) {
        this.colonyId = colony.id
        this.unit = unit
    }

    private constructor(missionId: String, unit: Unit, colonyId: String) : super(missionId) {
        this.unit = unit
        this.colonyId = colonyId
    }

    internal fun isDefenceUnitExists(): Boolean = CommonMissionHandler.isUnitExists(unit.owner, unit)
    internal fun isColonyOwner(): Boolean = CommonMissionHandler.isColonyOwner(unit.owner, colonyId)
    internal fun colony(): Colony = unit.owner.settlements.getById(colonyId).asColony()
    internal fun isDestinationOnTheSameIsland(map: Map): Boolean = map.isTheSameArea(colony().tile, unit.tile)

    internal fun changeColony(colony: Colony) {
        this.colonyId = colony.id
    }

    override fun blockUnits(unitMissionsMapping: UnitMissionsMapping) {
        unitMissionsMapping.blockUnit(unit, this)
    }

    override fun unblockUnits(unitMissionsMapping: UnitMissionsMapping) {
        unitMissionsMapping.unblockUnitFromMission(unit, this)
    }

    override fun toString(): String {
        return "defenceMission: ${unit}, colonyId: ${colonyId}"
    }

    fun ifNotThenFortified() {
        val state = unit.state
        if (!(state == Unit.UnitState.FORTIFIED || state == Unit.UnitState.FORTIFYING)) {
            unit.state = Unit.UnitState.FORTIFIED
        }
    }

    class Xml : AbstractMission.Xml<DefenceMission>() {
        private val ATTR_UNIT = "unit"
        private val ATTR_COLONY = "colony"

        override fun startElement(attr: XmlNodeAttributes) {
            val unit = PlayerMissionsContainer.Xml.getPlayerUnit(attr.getStrAttribute(ATTR_UNIT))
            nodeObject = DefenceMission(attr.id, unit, attr.getStrAttribute(ATTR_COLONY))
            super.startElement(attr)
        }

        override fun startWriteAttr(mission: DefenceMission, attr: XmlNodeAttributesWriter) {
            super.startWriteAttr(mission, attr)
            attr.setId(mission)
            attr.set(ATTR_UNIT, mission.unit)
            attr.set(ATTR_COLONY, mission.colonyId)
        }

        override fun getTagName(): String {
            return tagName()
        }

        companion object {
            @JvmStatic
            fun tagName(): String {
                return "defenceMission"
            }
        }
    }
}