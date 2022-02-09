package net.sf.freecol.common.model.ai.missions.pioneer

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.ai.missions.AbstractMission
import net.sf.freecol.common.model.ai.missions.UnitMissionsMapping
import promitech.colonization.savegame.XmlNodeAttributes
import promitech.colonization.savegame.XmlNodeAttributesWriter
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer

class PioneerMission : AbstractMission {

    val pioneer: Unit
    private var colonyId: String? = null

    constructor(pioneer: Unit, colony: Colony) : super(Game.idGenerator.nextId(PioneerMission::class.java)) {
        this.pioneer = pioneer
        this.colonyId = colony.id
    }

    private constructor(pioneer: Unit, missionId: String) : super(missionId) {
        this.pioneer = pioneer
    }

    fun isToColony(colony: Colony): Boolean = colony.equalsId(colonyId)

    override fun blockUnits(unitMissionsMapping: UnitMissionsMapping) {
        unitMissionsMapping.blockUnit(pioneer, this)
    }

    override fun unblockUnits(unitMissionsMapping: UnitMissionsMapping) {
        unitMissionsMapping.unblockUnitFromMission(pioneer, this)
    }

    fun isSpecialist(): Boolean {
        return pioneer.unitType.isType(UnitType.HARDY_PIONEER)
    }

    class Xml : AbstractMission.Xml<PioneerMission>() {
        private val ATTR_UNIT = "unit"
        private val ATTR_COLONY = "colony"

        override fun startElement(attr: XmlNodeAttributes) {
            val pioneer = PlayerMissionsContainer.Xml.getPlayerUnit(attr.getStrAttribute(ATTR_UNIT))
            nodeObject = PioneerMission(pioneer, attr.id)
            nodeObject.colonyId = attr.getStrAttribute(ATTR_COLONY)
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