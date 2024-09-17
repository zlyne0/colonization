package net.sf.freecol.common.model.ai.missions

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.ColonyId
import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.UnitId
import net.sf.freecol.common.model.player.Player
import promitech.colonization.savegame.XmlNodeAttributes
import promitech.colonization.savegame.XmlNodeAttributesWriter

class ReplaceColonyWorkerMission : AbstractMission {

    val colonyId: ColonyId
    val colonyWorkerUnitId: UnitId
    val replaceByUnitId: UnitId

    constructor(colony: Colony, colonyWorkerUnit: Unit, replaceByUnit: Unit) : super(Game.idGenerator.nextId(ReplaceColonyWorkerMission::class.java)) {
        this.colonyId = colony.id
        this.colonyWorkerUnitId = colonyWorkerUnit.id
        this.replaceByUnitId = replaceByUnit.id
    }

    private constructor(missionId: String, replaceByUnitId: UnitId, colonyId: ColonyId, workerUnitId: UnitId) : super(missionId) {
        this.replaceByUnitId = replaceByUnitId
        this.colonyId = colonyId
        this.colonyWorkerUnitId = workerUnitId
    }

    override fun blockUnits(unitMissionsMapping: UnitMissionsMapping) {
        unitMissionsMapping.blockUnit(replaceByUnitId, this)
        unitMissionsMapping.blockUnit(colonyWorkerUnitId, this)
    }

    override fun unblockUnits(unitMissionsMapping: UnitMissionsMapping) {
        unitMissionsMapping.unblockUnitFromMission(replaceByUnitId, this)
        unitMissionsMapping.unblockUnitFromMission(colonyWorkerUnitId, this)
    }

    internal fun isWorkerExistsInColony(player: Player): Boolean {
        val colony = player.settlements.getByIdOrNull(colonyId)
        if (colony == null) {
            return false
        }
        return colony.asColony().isUnitInColony(colonyWorkerUnitId)
    }

    override fun toString(): String {
        return "ReplaceColonyWorkerMission: missionId: ${id}, replaceByUnit: ${replaceByUnitId}, colonyId: ${colonyId}, workerUnitId: ${colonyWorkerUnitId}"
    }

    class Xml : AbstractMission.Xml<ReplaceColonyWorkerMission>() {

        private val ATTR_UNIT = "unit"
        private val ATTR_COLONY = "colony"
        private val ATTR_WORKER = "worker"

        override fun startElement(attr: XmlNodeAttributes) {
            nodeObject = ReplaceColonyWorkerMission(
                attr.id,
                attr.getStrAttribute(ATTR_UNIT),
                attr.getStrAttribute(ATTR_COLONY),
                attr.getStrAttribute(ATTR_WORKER)
            )
            super.startElement(attr)
        }

        override fun startWriteAttr(mission: ReplaceColonyWorkerMission, attr: XmlNodeAttributesWriter) {
            super.startWriteAttr(mission, attr)
            attr.setId(mission)
            attr.set(ATTR_UNIT, mission.replaceByUnitId)
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