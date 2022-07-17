package net.sf.freecol.common.model.ai.missions.pioneer

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.ColonyAssert.assertThat
import net.sf.freecol.common.model.ColonyTile
import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.SettlementAssert
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.TileAssert.assertThat
import net.sf.freecol.common.model.TileImprovement
import net.sf.freecol.common.model.TileImprovementType
import net.sf.freecol.common.model.UnitAssert.assertThat
import net.sf.freecol.common.model.UnitFactory
import net.sf.freecol.common.model.UnitRole
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.ai.missions.MissionHandlerBaseTestClass
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainerAssert.assertThat
import net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission
import net.sf.freecol.common.model.map.path.PathFinder
import net.sf.freecol.common.model.player.Player
import net.sf.freecol.common.model.specification.GoodsType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import promitech.colonization.Direction
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

        val mission = ReplaceColonyWorkerMission(expertFarmer, fortOranje, workerFreeColonist)
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

        val mission = ReplaceColonyWorkerMission(expertFarmer, fortOranje, workerFreeColonist)
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

        val mission = ReplaceColonyWorkerMission(expertFarmer, fortOranje, workerFreeColonist)
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
}