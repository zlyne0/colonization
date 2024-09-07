package net.sf.freecol.common.model.ai.missions

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.ColonyId
import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.UnitId
import net.sf.freecol.common.model.player.Player
import promitech.colonization.ai.CommonMissionHandler
import promitech.colonization.savegame.XmlNodeAttributes
import promitech.colonization.savegame.XmlNodeAttributesWriter

class ReplaceColonyWorkerMission : AbstractMission {

    val colonyId: ColonyId
    val colonyWorkerUnitId: UnitId
    val replaceByUnit: Unit

    constructor(colony: Colony, colonyWorkerUnit: Unit, replaceByUnit: Unit) : super(Game.idGenerator.nextId(ReplaceColonyWorkerMission::class.java)) {
        this.colonyId = colony.id
        this.colonyWorkerUnitId = colonyWorkerUnit.id
        this.replaceByUnit = replaceByUnit
    }

    private constructor(missionId: String, replaceByUnit: Unit, colonyId: ColonyId, workerUnitId: UnitId) : super(missionId) {
        this.replaceByUnit = replaceByUnit
        this.colonyId = colonyId
        this.colonyWorkerUnitId = workerUnitId
    }

    override fun blockUnits(unitMissionsMapping: UnitMissionsMapping) {
        unitMissionsMapping.blockUnit(replaceByUnit, this)
        unitMissionsMapping.blockUnit(colonyWorkerUnitId, this)
    }

    override fun unblockUnits(unitMissionsMapping: UnitMissionsMapping) {
        unitMissionsMapping.unblockUnitFromMission(replaceByUnit, this)
        unitMissionsMapping.unblockUnitFromMission(colonyWorkerUnitId, this)
    }

    internal fun isUnitExists(player: Player): Boolean {
        return CommonMissionHandler.isUnitExists(player, replaceByUnit)
    }

    fun colony(): Colony {
        return replaceByUnit.owner.settlements.getById(colonyId).asColony()
    }

    internal fun isWorkerExistsInColony(player: Player): Boolean {
        val colony = player.settlements.getByIdOrNull(colonyId)
        if (colony == null) {
            return false
        }
        return colony.asColony().isUnitInColony(colonyWorkerUnitId)
    }

    override fun toString(): String {
        return "ReplaceColonyWorkerMission: missionId: ${id}, replaceByUnit: ${replaceByUnit.id}, colonyId: ${colonyId}, workerUnitId: ${colonyWorkerUnitId}"
    }

    class Xml : AbstractMission.Xml<ReplaceColonyWorkerMission>() {

        private val ATTR_UNIT = "unit"
        private val ATTR_COLONY = "colony"
        private val ATTR_WORKER = "worker"

        override fun startElement(attr: XmlNodeAttributes) {
            nodeObject = ReplaceColonyWorkerMission(
                attr.id,
                PlayerMissionsContainer.Xml.getPlayerUnit(attr.getStrAttribute(ATTR_UNIT)),
                attr.getStrAttribute(ATTR_COLONY),
                attr.getStrAttribute(ATTR_WORKER)
            )
            super.startElement(attr)
        }

        override fun startWriteAttr(mission: ReplaceColonyWorkerMission, attr: XmlNodeAttributesWriter) {
            super.startWriteAttr(mission, attr)
            attr.setId(mission)
            attr.set(ATTR_UNIT, mission.replaceByUnit)
            attr.set(ATTR_COLONY, mission.colonyId)
            attr.set(ATTR_WORKER, mission.colonyWorkerUnitId)
        }

        override fun getTagName(): String {
            return tagName()
        }

        companion object {
            @JvmStatic
            fun tagName(): String {
                return "replaceColonyWorkerMission"
            }
        }
    }
}