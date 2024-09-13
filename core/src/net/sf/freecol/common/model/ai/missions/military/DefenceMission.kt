package net.sf.freecol.common.model.ai.missions.military

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.UnitId
import net.sf.freecol.common.model.ai.missions.AbstractMission
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.ai.missions.UnitMissionsMapping
import net.sf.freecol.common.model.player.Player
import promitech.colonization.savegame.XmlNodeAttributes
import promitech.colonization.savegame.XmlNodeAttributesWriter

class DefenceMission: AbstractMission {

    var unitId: UnitId
        private set
    var tile: Tile
        private set

    constructor(tile: Tile, unit: Unit) : super(Game.idGenerator.nextId(DefenceMission::class.java)) {
        this.tile = tile
        this.unitId = unit.id
    }

    private constructor(missionId: String, unitId: UnitId, tile: Tile) : super(missionId) {
        this.unitId = unitId
        this.tile = tile
    }

    fun isAtDefenceLocation(unit: Unit): Boolean = unit.isAtLocation(tile)

    internal fun isTileAccessible(player: Player): Boolean {
        if (tile.hasSettlement()) {
            return tile.settlement.owner.equals(player)
        }
        if (tile.units.isEmpty) {
            return true
        }
        return tile.units.first().owner.equalsId(player)
    }

    internal fun changeDefenceDestination(colony: Colony) {
        this.tile = colony.tile
    }

    override fun blockUnits(unitMissionsMapping: UnitMissionsMapping) {
        unitMissionsMapping.blockUnit(unitId, this)
    }

    override fun unblockUnits(unitMissionsMapping: UnitMissionsMapping) {
        unitMissionsMapping.unblockUnitFromMission(unitId, this)
    }

    override fun toString(): String {
        return "defenceMission: ${unitId}, tile: [${tile.toStringCords()}]"
    }

    internal fun ifNotThenFortified(unit: Unit) {
        val state = unit.state
        if (state != Unit.UnitState.FORTIFIED && state != Unit.UnitState.FORTIFYING && unit.canChangeState(Unit.UnitState.FORTIFYING)) {
            unit.state = Unit.UnitState.FORTIFYING
        }
    }

    internal fun changeUnit(
        unitToReplace: Unit,
        replaceBy: Unit,
        playerMissionsContainer: PlayerMissionsContainer
    ) {
        if (unitToReplace.equalsId(unitId)) {
            playerMissionsContainer.unblockUnitFromMission(unitId, this)
            unitId = replaceBy.id
            playerMissionsContainer.blockUnitForMission(unitId, this)
        }
    }

    class Xml : AbstractMission.Xml<DefenceMission>() {
        private val ATTR_UNIT = "unit"
        private val ATTR_TILE = "tile"

        override fun startElement(attr: XmlNodeAttributes) {
            val tile = game.map.getSafeTile(attr.getPoint(ATTR_TILE))
            nodeObject = DefenceMission(attr.id, attr.getStrAttribute(ATTR_UNIT), tile)
            super.startElement(attr)
        }

        override fun startWriteAttr(mission: DefenceMission, attr: XmlNodeAttributesWriter) {
            super.startWriteAttr(mission, attr)
            attr.setId(mission)
            attr.set(ATTR_UNIT, mission.unitId)
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