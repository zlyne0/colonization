package net.sf.freecol.common.model.ai.missions.indian

import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.ai.missions.AbstractMission
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.ai.missions.UnitMissionsMapping
import promitech.colonization.Direction
import promitech.colonization.savegame.XmlNodeAttributes
import promitech.colonization.savegame.XmlNodeAttributesWriter

class WanderMission : AbstractMission {

    val unit: Unit
    var previewDirection: Direction? = null

    constructor(unit: Unit) : super(Game.idGenerator.nextId(WanderMission::class.java)) {
        this.unit = unit
    }

    private constructor(missionId: String, unit: Unit) : super(missionId) {
        this.unit = unit
    }

    fun previewReverseDirection(): Direction? {
        if (previewDirection != null) {
            return previewDirection!!.reverseDirection
        }
        return null
    }

    override fun blockUnits(unitMissionsMapping: UnitMissionsMapping) {
        unitMissionsMapping.blockUnit(unit, this)
    }

    override fun unblockUnits(unitMissionsMapping: UnitMissionsMapping) {
        unitMissionsMapping.blockUnit(unit, this)
    }

    override fun toString(): String {
        return "WanderMission unit: $unit, previewDirection: $previewDirection"
    }

    class Xml : AbstractMission.Xml<WanderMission>() {

        override fun startElement(attr: XmlNodeAttributes) {
            val unitId = attr.getStrAttributeNotNull(ATTR_UNIT)
            val unit = PlayerMissionsContainer.Xml.getPlayerUnit(unitId)
            val m = WanderMission(attr.id, unit)
            nodeObject = m
            super.startElement(attr)
        }

        override fun startWriteAttr(m: WanderMission, attr: XmlNodeAttributesWriter) {
            super.startWriteAttr(m, attr)
            attr.setId(m)
            attr.set(ATTR_UNIT, m.unit)
        }

        override fun getTagName(): String {
            return tagName()
        }

        companion object {
            private const val ATTR_UNIT = "unit"

            @JvmStatic
            fun tagName(): String {
                return "wanderMission"
            }
        }
    }
}