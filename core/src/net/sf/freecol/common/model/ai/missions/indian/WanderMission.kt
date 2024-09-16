package net.sf.freecol.common.model.ai.missions.indian

import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.UnitId
import net.sf.freecol.common.model.ai.missions.AbstractMission
import net.sf.freecol.common.model.ai.missions.UnitMissionsMapping
import promitech.colonization.Direction
import promitech.colonization.savegame.XmlNodeAttributes
import promitech.colonization.savegame.XmlNodeAttributesWriter

class WanderMission : AbstractMission {

    val unitId: UnitId
    var previewDirection: Direction? = null

    constructor(unit: Unit) : super(Game.idGenerator.nextId(WanderMission::class.java)) {
        this.unitId = unit.id
    }

    private constructor(missionId: String, unitId: UnitId) : super(missionId) {
        this.unitId = unitId
    }

    fun previewReverseDirection(): Direction? {
        if (previewDirection != null) {
            return previewDirection!!.reverseDirection
        }
        return null
    }

    override fun blockUnits(unitMissionsMapping: UnitMissionsMapping) {
        unitMissionsMapping.blockUnit(unitId, this)
    }

    override fun unblockUnits(unitMissionsMapping: UnitMissionsMapping) {
        unitMissionsMapping.blockUnit(unitId, this)
    }

    override fun toString(): String {
        return "WanderMission unit: $unitId, previewDirection: $previewDirection"
    }

    class Xml : AbstractMission.Xml<WanderMission>() {

        override fun startElement(attr: XmlNodeAttributes) {
            val m = WanderMission(attr.id, attr.getStrAttributeNotNull(ATTR_UNIT))
            nodeObject = m
            super.startElement(attr)
        }

        override fun startWriteAttr(m: WanderMission, attr: XmlNodeAttributesWriter) {
            super.startWriteAttr(m, attr)
            attr.setId(m)
            attr.set(ATTR_UNIT, m.unitId)
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