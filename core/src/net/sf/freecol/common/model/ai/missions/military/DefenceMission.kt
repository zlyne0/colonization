package net.sf.freecol.common.model.ai.missions.military

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.Map
import net.sf.freecol.common.model.Tile
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
    var tile: Tile
        private set

    constructor(tile: Tile, unit: Unit) : super(Game.idGenerator.nextId(DefenceMission::class.java)) {
        this.tile = tile
        this.unit = unit
    }

    private constructor(missionId: String, unit: Unit, tile: Tile) : super(missionId) {
        this.unit = unit
        this.tile = tile
    }

    internal fun isDefenceUnitExists(): Boolean = CommonMissionHandler.isUnitExists(unit.owner, unit)

    internal fun isTileAccessible(): Boolean {
        if (tile.hasSettlement()) {
            return tile.settlement.owner.equals(unit.owner)
        }
        if (tile.units.isEmpty) {
            return true
        }
        return tile.units.first().owner.equalsId(unit.owner)
    }

    internal fun isDestinationOnTheSameIsland(map: Map): Boolean = map.isTheSameArea(tile, unit.tile)

    internal fun changeDefenceDestination(colony: Colony) {
        this.tile = colony.tile
    }

    override fun blockUnits(unitMissionsMapping: UnitMissionsMapping) {
        unitMissionsMapping.blockUnit(unit, this)
    }

    override fun unblockUnits(unitMissionsMapping: UnitMissionsMapping) {
        unitMissionsMapping.unblockUnitFromMission(unit, this)
    }

    override fun toString(): String {
        return "defenceMission: ${unit}, tile: [${tile.toStringCords()}]"
    }

    fun ifNotThenFortified() {
        val state = unit.state
        if (!(state == Unit.UnitState.FORTIFIED || state == Unit.UnitState.FORTIFYING)) {
            unit.state = Unit.UnitState.FORTIFIED
        }
    }

    class Xml : AbstractMission.Xml<DefenceMission>() {
        private val ATTR_UNIT = "unit"
        private val ATTR_TILE = "tile"

        override fun startElement(attr: XmlNodeAttributes) {
            val unit = PlayerMissionsContainer.Xml.getPlayerUnit(attr.getStrAttribute(ATTR_UNIT))
            val tile = game.map.getSafeTile(attr.getPoint(ATTR_TILE))
            nodeObject = DefenceMission(attr.id, unit, tile)
            super.startElement(attr)
        }

        override fun startWriteAttr(mission: DefenceMission, attr: XmlNodeAttributesWriter) {
            super.startWriteAttr(mission, attr)
            attr.setId(mission)
            attr.set(ATTR_UNIT, mission.unit)
            attr.setPoint(ATTR_TILE, mission.tile.x, mission.tile.y)
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