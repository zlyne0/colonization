package net.sf.freecol.common.model.ai.missions.scout

import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.ai.missions.AbstractMission
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.ai.missions.UnitMissionsMapping
import promitech.colonization.savegame.XmlNodeAttributes
import promitech.colonization.savegame.XmlNodeAttributesWriter

class ScoutMission : AbstractMission {

    val scout : Unit

    constructor(scout: Unit) : super(Game.idGenerator.nextId(ScoutMission::class.java)) {
        this.scout = scout
    }

    constructor(scout: Unit, missionId: String) : super(missionId) {
        this.scout = scout
    }

    override fun blockUnits(unitMissionsMapping: UnitMissionsMapping) {
        unitMissionsMapping.blockUnit(scout, this)
    }

    override fun unblockUnits(unitMissionsMapping: UnitMissionsMapping) {
        unitMissionsMapping.unblockUnitFromMission(scout, this)
    }

    class Xml : AbstractMission.Xml<ScoutMission>() {
        private val ATTR_UNIT = "unit"

        override fun startElement(attr: XmlNodeAttributes) {
            val scout = PlayerMissionsContainer.Xml.getPlayerUnit(attr.getStrAttribute(ATTR_UNIT))
            nodeObject = ScoutMission(scout, attr.id)
        }

        override fun startWriteAttr(mission: ScoutMission, attr: XmlNodeAttributesWriter) {
            attr.setId(mission)
            attr.set(ATTR_UNIT, mission.scout)
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