package net.sf.freecol.common.model.ai.missions.pioneer

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.ai.missions.AbstractMission
import net.sf.freecol.common.model.ai.missions.UnitMissionsMapping
import promitech.colonization.savegame.XmlNodeAttributes
import promitech.colonization.savegame.XmlNodeAttributesWriter
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.UnitId
import net.sf.freecol.common.model.UnitRole
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.player.Player

class PioneerMission : AbstractMission {

    var pioneerId: UnitId
        private set
    var colonyId: String
        private set

    constructor(pioneer: Unit, colony: Colony) : super(Game.idGenerator.nextId(PioneerMission::class.java)) {
        this.pioneerId = pioneer.id
        this.colonyId = colony.id
    }

    private constructor(pioneerId: UnitId, missionId: String, colonyId: String) : super(missionId) {
        this.pioneerId = pioneerId
        this.colonyId = colonyId
    }

    fun isToColony(colony: Colony): Boolean = colony.equalsId(colonyId)

    override fun blockUnits(unitMissionsMapping: UnitMissionsMapping) {
        unitMissionsMapping.blockUnit(pioneerId, this)
    }

    override fun unblockUnits(unitMissionsMapping: UnitMissionsMapping) {
        unitMissionsMapping.unblockUnitFromMission(pioneerId, this)
    }

    internal fun colony(player: Player): Colony {
        return player.settlements.getById(colonyId).asColony()
    }

    internal fun colonyTile(player: Player): Tile = player.settlements.getById(colonyId).tile

    internal fun waitOrResolveFreeColonistPioneer(colony: Colony, pioneer: Unit) {
        if (pioneer.unitType.equalsId(UnitType.FREE_COLONIST)) {
            colony.changeUnitRole(pioneer, Specification.instance.unitRoles.getById(UnitRole.DEFAULT_ROLE_ID))
            setDone()
        } // else wait for new colony to improve
    }

    internal fun changeColony(colony: Colony) {
        this.colonyId = colony.id
    }

    internal fun changeUnit(unitToReplace: Unit, replaceBy: Unit, playerMissionsContainer: PlayerMissionsContainer) {
        if (unitToReplace.equalsId(pioneerId)) {
            playerMissionsContainer.unblockUnitFromMission(pioneerId, this)
            pioneerId = replaceBy.id
            playerMissionsContainer.blockUnitForMission(pioneerId, this)
        }
    }

    override fun toString(): String {
        return "pioneerMission: ${pioneerId}, colonyId: ${colonyId}"
    }

    class Xml : AbstractMission.Xml<PioneerMission>() {
        private val ATTR_UNIT = "unit"
        private val ATTR_COLONY = "colony"

        override fun startElement(attr: XmlNodeAttributes) {
            nodeObject = PioneerMission(attr.getStrAttribute(ATTR_UNIT), attr.id, attr.getStrAttribute(ATTR_COLONY))
            super.startElement(attr)
        }

        override fun startWriteAttr(mission: PioneerMission, attr: XmlNodeAttributesWriter) {
            super.startWriteAttr(mission, attr)
            attr.setId(mission)
            attr.set(ATTR_UNIT, mission.pioneerId)
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