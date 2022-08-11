package net.sf.freecol.common.model.ai.missions.pioneer

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.ColonyId
import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.UnitRole
import net.sf.freecol.common.model.ai.missions.AbstractMission
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.ai.missions.UnitMissionsMapping
import net.sf.freecol.common.model.player.Player
import promitech.colonization.ai.CommonMissionHandler
import promitech.colonization.savegame.XmlNodeAttributes
import promitech.colonization.savegame.XmlNodeAttributesWriter

class TakeRoleEquipmentMission : AbstractMission {

    var unit: Unit private set
    val colonyId: ColonyId
    val role: UnitRole
    val roleCount: Int

    constructor(unit: Unit, colony: Colony, role: UnitRole, roleCount: Int = role.maximumCount)
        : super(Game.idGenerator.nextId(TakeRoleEquipmentMission::class.java))
    {
        this.unit = unit
        this.colonyId = colony.id
        this.role = role
        this.roleCount = roleCount
    }

    private constructor(missionId: String, unit: Unit, colonyId: ColonyId, role: UnitRole, roleCount: Int) : super(missionId) {
        this.unit = unit
        this.colonyId = colonyId
        this.role = role
        this.roleCount = roleCount
    }

    internal fun isUnitExists(player: Player): Boolean {
        return CommonMissionHandler.isUnitExists(player, unit)
    }

    internal fun isColonyExist(player: Player): Boolean {
        return player.settlements.containsId(colonyId)
    }

    internal fun colony(): Colony = unit.owner.settlements.getById(colonyId).asColony()

    override fun blockUnits(unitMissionsMapping: UnitMissionsMapping) {
        unitMissionsMapping.blockUnit(unit, this)
    }

    override fun unblockUnits(unitMissionsMapping: UnitMissionsMapping) {
        unitMissionsMapping.unblockUnitFromMission(unit, this)
    }

    internal fun changeUnit(unitToReplace: Unit, replaceBy: Unit, playerMissionsContainer: PlayerMissionsContainer) {
        if (unit.equalsId(unitToReplace)) {
            playerMissionsContainer.unblockUnitFromMission(unit, this)
            unit = replaceBy
            playerMissionsContainer.blockUnitForMission(unit, this)
        }
    }

    override fun toString(): String {
        return "TakeRoleEquipmentMission unitId: ${unit.id}, colonyId: ${colonyId}, role: ${role.id}, roleCount: ${roleCount}"
    }

    class Xml : AbstractMission.Xml<TakeRoleEquipmentMission>() {

        private val ATTR_UNIT = "unit"
        private val ATTR_COLONY = "colony"
        private val ATTR_ROLE_TYPE = "role"
        private val ATTR_ROLE_COUNT = "roleCount"

        override fun startElement(attr: XmlNodeAttributes) {
            nodeObject = TakeRoleEquipmentMission(
                missionId = attr.id,
                unit = PlayerMissionsContainer.Xml.getPlayerUnit(attr.getStrAttribute(ATTR_UNIT)),
                colonyId = attr.getStrAttribute(ATTR_COLONY),
                role = attr.getEntity(ATTR_ROLE_TYPE, Specification.instance.unitRoles),
                roleCount = attr.getIntAttribute(ATTR_ROLE_COUNT)
            )
            super.startElement(attr)
        }

        override fun startWriteAttr(mission: TakeRoleEquipmentMission, attr: XmlNodeAttributesWriter) {
            super.startWriteAttr(mission, attr)
            attr.setId(mission)
            attr.set(ATTR_UNIT, mission.unit)
            attr.set(ATTR_COLONY, mission.colonyId)
            attr.set(ATTR_ROLE_TYPE, mission.role)
            attr.set(ATTR_ROLE_COUNT, mission.roleCount)
        }

        override fun getTagName(): String {
            return tagName()
        }

        companion object {
            @JvmStatic
            fun tagName(): String {
                return "takeRoleEquipmentMission"
            }
        }
    }
}