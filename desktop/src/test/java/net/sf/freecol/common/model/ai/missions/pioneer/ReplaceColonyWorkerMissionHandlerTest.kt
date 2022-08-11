package net.sf.freecol.common.model.ai.missions.pioneer

import net.sf.freecol.common.model.ColonyAssert.assertThat
import net.sf.freecol.common.model.ColonyTile
import net.sf.freecol.common.model.UnitAssert
import net.sf.freecol.common.model.UnitAssert.assertThat
import net.sf.freecol.common.model.UnitFactory
import net.sf.freecol.common.model.UnitRole
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.ai.missions.MissionHandlerBaseTestClass
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.specification.GoodsType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import promitech.colonization.savegame.AbstractMissionAssert.assertThat

class ReplaceColonyWorkerMissionHandlerTest : MissionHandlerBaseTestClass() {

    lateinit var dutchMissionContainer: PlayerMissionsContainer

    @BeforeEach
    override fun setup() {
        super.setup()

        clearAllMissions(dutch)

        fortOranje.goodsContainer.decreaseAllToZero()
        dutchMissionContainer = game.aiContainer.missionContainer(dutch)
    }

    @Test
    fun `should replace worker in colony`() {
        // given
        val workerFreeColonist = dutch.units.getById("unit:6436")
        assertThat(workerFreeColonist).isAtLocation(ColonyTile::class.java)

        val expertFarmer = UnitFactory.create(UnitType.EXPERT_FARMER, dutch, nieuwAmsterdam.tile)

        val mission = ReplaceColonyWorkerMission(fortOranje, workerFreeColonist, expertFarmer)
        dutchMissionContainer.addMission(mission)


        // when
        newTurnAndExecuteMission(dutch, 1)

        // then
        assertThat(expertFarmer).isAtLocation(ColonyTile::class.java)
        assertThat(workerFreeColonist).isAtLocation(fortOranje.tile)
        assertThat(mission).isDone
    }

    @Test
    fun `should replace worker in colony and transfer tools`() {
        // given
        val workerFreeColonist = dutch.units.getById("unit:6436")
        assertThat(workerFreeColonist).isAtLocation(ColonyTile::class.java)

        val farmerRoleCount = 4
        val expertFarmer = UnitFactory.create(UnitType.EXPERT_FARMER, dutch, nieuwAmsterdam.tile)
        expertFarmer.changeRole(unitRole(UnitRole.PIONEER), farmerRoleCount)

        val mission = ReplaceColonyWorkerMission(fortOranje, workerFreeColonist, expertFarmer)
        dutchMissionContainer.addMission(mission)


        // when
        newTurnAndExecuteMission(dutch, 1)

        // then
        assertThat(expertFarmer)
            .isAtLocation(ColonyTile::class.java)
            .isUnitRole(UnitRole.DEFAULT_ROLE_ID)
        assertThat(workerFreeColonist)
            .isAtLocation(fortOranje.tile)
            .isUnitRole(UnitRole.PIONEER)
            .hasRoleCount(farmerRoleCount)
        assertThat(fortOranje).hasGoods(GoodsType.TOOLS, 0)
        assertThat(mission).isDone
    }

    @Test
    fun `should replace worker in colony and transfer dragoon equipment`() {
        // given
        val workerFreeColonist = dutch.units.getById("unit:6436")
        assertThat(workerFreeColonist).isAtLocation(ColonyTile::class.java)

        val expertFarmer = UnitFactory.create(UnitType.EXPERT_FARMER, dutch, nieuwAmsterdam.tile)
        expertFarmer.changeRole(unitRole(UnitRole.DRAGOON))

        val mission = ReplaceColonyWorkerMission(fortOranje, workerFreeColonist, expertFarmer)
        dutchMissionContainer.addMission(mission)


        // when
        newTurnAndExecuteMission(dutch, 1)

        // then
        assertThat(expertFarmer)
            .isAtLocation(ColonyTile::class.java)
            .isUnitRole(UnitRole.DEFAULT_ROLE_ID)
        assertThat(workerFreeColonist)
            .isAtLocation(fortOranje.tile)
            .isUnitRole(UnitRole.DRAGOON)
        assertThat(fortOranje)
            .hasGoods(GoodsType.HORSES, 0)
            .hasGoods(GoodsType.MUSKETS, 0)
        assertThat(mission).isDone
    }

    @Test
    fun `should replace colony worker and notify parent missions`() {
        // given
        val colonyWorker = dutch.units.getById("unit:6436")
        assertThat(colonyWorker).isAtLocation(ColonyTile::class.java)
        fortOranje.goodsContainer.increaseGoodsQuantity(GoodsType.TOOLS, 100)

        val expertFarmer = UnitFactory.create(UnitType.EXPERT_FARMER, dutch, nieuwAmsterdam.tile)

        val pioneerMission = PioneerMission(expertFarmer, fortOranje)
        dutchMissionContainer.addMission(pioneerMission)

        val takeRoleMission = TakeRoleEquipmentMission(expertFarmer, fortOranje, unitRole(UnitRole.PIONEER), 4)
        dutchMissionContainer.addMission(pioneerMission, takeRoleMission)

        val replaceColonyWorkerMission = ReplaceColonyWorkerMission(fortOranje, colonyWorker, expertFarmer)
        dutchMissionContainer.addMission(takeRoleMission, replaceColonyWorkerMission)


        // when
        newTurnAndExecuteMission(dutch, 1)

        // then
        assertThat(expertFarmer).isAtLocation(ColonyTile::class.java)
        assertThat(colonyWorker).isAtLocation(fortOranje.tile)
        assertThat(replaceColonyWorkerMission).isDone

        assertThat(takeRoleMission).isDone
        assertThat(takeRoleMission.unit).isEqualsTo(colonyWorker)

        assertThat(pioneerMission.pioneer).isEqualsTo(colonyWorker)
        assertThat(pioneerMission)
            .isNotDone()
            .hasNotDependMission()
    }
}