package net.sf.freecol.common.model.ai.missions.pionier

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.ai.missions.AbstractMission
import net.sf.freecol.common.model.ai.missions.UnitMissionsMapping
import promitech.colonization.savegame.XmlNodeAttributes
import promitech.colonization.savegame.XmlNodeAttributesWriter
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer

class PionierMission : AbstractMission {

    val pionier: Unit
    private var colonyId: String? = null

    constructor(pionier: Unit, colony: Colony) : super(Game.idGenerator.nextId(PionierMission::class.java)) {
        this.pionier = pionier
        this.colonyId = colony.id
    }

    private constructor(pionier: Unit, missionId: String) : super(missionId) {
        this.pionier = pionier
    }

    fun isToColony(colony: Colony): Boolean = colony.equalsId(colonyId)

    override fun blockUnits(unitMissionsMapping: UnitMissionsMapping) {
        unitMissionsMapping.blockUnit(pionier, this)
    }

    override fun unblockUnits(unitMissionsMapping: UnitMissionsMapping) {
        unitMissionsMapping.unblockUnitFromMission(pionier, this)
    }

    fun isSpecialist(): Boolean {
        return pionier.unitType.isType(UnitType.HARDY_PIONEER)
    }

    class Xml : AbstractMission.Xml<PionierMission>() {
        private val ATTR_UNIT = "unit"
        private val ATTR_COLONY = "colony"

        override fun startElement(attr: XmlNodeAttributes) {
            val pionier = PlayerMissionsContainer.Xml.getPlayerUnit(attr.getStrAttribute(ATTR_UNIT))
            nodeObject = PionierMission(pionier, attr.id)
            nodeObject.colonyId = attr.getStrAttribute(ATTR_COLONY)
        }

        override fun startWriteAttr(mission: PionierMission, attr: XmlNodeAttributesWriter) {
            attr.setId(mission)
            attr.set(ATTR_UNIT, mission.pionier)
            attr.set(ATTR_COLONY, mission.colonyId)
        }

        override fun getTagName(): String {
            return tagName()
        }

        companion object {
            @JvmStatic
            fun tagName(): String {
                return "pionierMission"
            }
        }
    }
}