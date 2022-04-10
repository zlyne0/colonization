package net.sf.freecol.common.model.ai.missions.pioneer

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.ai.missions.AbstractMission
import net.sf.freecol.common.model.ai.missions.UnitMissionsMapping
import promitech.colonization.savegame.XmlNodeAttributes
import promitech.colonization.savegame.XmlNodeAttributesWriter
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.UnitRole
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import promitech.colonization.ai.CommonMissionHandler
import java.lang.IllegalStateException

class PioneerMission : AbstractMission {

    val pioneer: Unit
    var colonyId: String
        private set

    constructor(pioneer: Unit, colony: Colony) : super(Game.idGenerator.nextId(PioneerMission::class.java)) {
        this.pioneer = pioneer
        this.colonyId = colony.id
    }

    private constructor(pioneer: Unit, missionId: String, colonyId: String) : super(missionId) {
        this.pioneer = pioneer
        this.colonyId = colonyId
    }

    fun isToColony(colony: Colony): Boolean = colony.equalsId(colonyId)

    override fun blockUnits(unitMissionsMapping: UnitMissionsMapping) {
        unitMissionsMapping.blockUnit(pioneer, this)
    }

    override fun unblockUnits(unitMissionsMapping: UnitMissionsMapping) {
        unitMissionsMapping.unblockUnitFromMission(pioneer, this)
    }

    fun isPionnerWithoutTools(): Boolean {
        return pioneer.unitRole.equalsId(UnitRole.DEFAULT_ROLE_ID)
    }

    fun isSpecialist(): Boolean {
        return pioneer.unitType.isType(UnitType.HARDY_PIONEER)
    }

    fun isPioneerExists(): Boolean {
        return CommonMissionHandler.isUnitExists(pioneer.owner, pioneer)
    }

    fun isColonyOwner(): Boolean {
        return CommonMissionHandler.isColonyOwner(pioneer.owner, colonyId)
    }

    fun colony(): Colony {
        return pioneer.owner.settlements.getById(colonyId).asColony()
    }

    fun waitOrResolveFreeColonistPionner(colony: Colony) {
        if (pioneer.unitType.equalsId(UnitType.FREE_COLONIST)) {
            colony.changeUnitRole(pioneer, Specification.instance.unitRoles.getById(UnitRole.DEFAULT_ROLE_ID))
            setDone()
        } // else wait for new colony to improve
    }

    fun changeColony(colony: Colony) {
        this.colonyId = colony.id
    }

    override fun toString(): String {
        return "pioneerMission: ${pioneer}, colonyId: ${colonyId}"
    }

    class Xml : AbstractMission.Xml<PioneerMission>() {
        private val ATTR_UNIT = "unit"
        private val ATTR_COLONY = "colony"

        override fun startElement(attr: XmlNodeAttributes) {
            val pioneer = PlayerMissionsContainer.Xml.getPlayerUnit(attr.getStrAttribute(ATTR_UNIT))
            nodeObject = PioneerMission(pioneer, attr.id, attr.getStrAttribute(ATTR_COLONY))
        }

        override fun startWriteAttr(mission: PioneerMission, attr: XmlNodeAttributesWriter) {
            attr.setId(mission)
            attr.set(ATTR_UNIT, mission.pioneer)
            attr.set(ATTR_COLONY, mission.colonyId)
        }

        override fun getTagName(): String {
            return tagName()
        }

        companion object {
            @JvmStatic
            fun tagName(): String {
                return "pioneerMission"
            }
        }
    }
}