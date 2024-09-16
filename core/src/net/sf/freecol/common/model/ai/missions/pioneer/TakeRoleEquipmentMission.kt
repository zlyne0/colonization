package net.sf.freecol.common.model.ai.missions.pioneer

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.ColonyId
import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.UnitId
import net.sf.freecol.common.model.UnitRole
import net.sf.freecol.common.model.ai.missions.AbstractMission
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.ai.missions.UnitMissionsMapping
import net.sf.freecol.common.model.player.Player
import promitech.colonization.savegame.XmlNodeAttributes
import promitech.colonization.savegame.XmlNodeAttributesWriter

class TakeRoleEquipmentMission : AbstractMission {

    var unitId: UnitId private set
    val colonyId: ColonyId
    val role: UnitRole
    val roleCount: Int

    constructor(unit: Unit, colony: Colony, role: UnitRole, roleCount: Int = role.maximumCount)
        : super(Game.idGenerator.nextId(TakeRoleEquipmentMission::class.java))
    {
        this.unitId = unit.id
        this.colonyId = colony.id
        this.role = role
        this.roleCount = roleCount
    }

    private constructor(missionId: String, unitId: UnitId, colonyId: ColonyId, role: UnitRole, roleCount: Int) : super(missionId) {
        this.unitId = unitId
        this.colonyId = colonyId
        this.role = role
        this.roleCount = roleCount
    }

    internal fun isColonyExist(player: Player): Boolean {
        return player.settlements.containsId(colonyId)
    }

    override fun blockUnits(unitMissionsMapping: UnitMissionsMapping) {
        unitMissionsMapping.blockUnit(unitId, this)
    }

    override fun unblockUnits(unitMissionsMapping: UnitMissionsMapping) {
        unitMissionsMapping.unblockUnitFromMission(unitId, this)
    }

    internal fun changeUnit(unitToReplace: Unit, replaceBy: Unit, playerMissionsContainer: PlayerMissionsContainer) {
        if (unitToReplace.equalsId(unitId)) {
            playerMissionsContainer.unblockUnitFromMission(unitId, this)
            unitId = replaceBy.id
            playerMissionsContainer.blockUnitForMission(unitId, this)
        }
    }

    fun makeReservation(game: Game, player: Player) {
        val requiredGoods = role.requiredGoodsForRoleCount(roleCount)
        val playerAiContainer = game.aiContainer.playerAiContainer(player)
        val colonySupplyGoods = playerAiContainer.findOrCreateColonySupplyGoods(colonyId)
        colonySupplyGoods.makeReservation(getId(), requiredGoods)
    }

    override fun toString(): String {
        return "TakeRoleEquipmentMission unitId: ${unitId}, colonyId: ${colonyId}, role: ${role.id}, roleCount: ${roleCount}"
    }

    class Xml : AbstractMission.Xml<TakeRoleEquipmentMission>() {

        private val ATTR_UNIT = "unit"
        private val ATTR_COLONY = "colony"
        private val ATTR_ROLE_TYPE = "role"
        private val ATTR_ROLE_COUNT = "roleCount"

        override fun startElement(attr: XmlNodeAttributes) {
            nodeObject = TakeRoleEquipmentMission(
                missionId = attr.id,
                unitId = attr.getStrAttribute(ATTR_UNIT),
                colonyId = attr.getStrAttribute(ATTR_COLONY),
                role = attr.getEntity(ATTR_ROLE_TYPE, Specification.instance.unitRoles),
                roleCount = attr.getIntAttribute(ATTR_ROLE_COUNT)
            )
            super.startElement(attr)
        }

        override fun startWriteAttr(mission: TakeRoleEquipmentMission, attr: XmlNodeAttributesWriter) {
            super.startWriteAttr(mission, attr)
            attr.setId(mission)
            attr.set(ATTR_UNIT, mission.unitId)
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