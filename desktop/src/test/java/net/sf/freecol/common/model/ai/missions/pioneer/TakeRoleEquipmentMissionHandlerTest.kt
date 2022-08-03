package net.sf.freecol.common.model.ai.missions.pioneer

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.ColonyAssert.assertThat
import net.sf.freecol.common.model.ColonyTile
import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.MapIdEntitiesAssert
import net.sf.freecol.common.model.MapIdEntitiesAssert.assertThat
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

class TakeRoleEquipmentMissionHandlerTest : MissionHandlerBaseTestClass() {

    lateinit var dutchMissionContainer: PlayerMissionsContainer

    @BeforeEach
    override fun setup() {
        super.setup()

        clearAllMissions(dutch)

        fortOranje.goodsContainer.decreaseAllToZero()
        dutchMissionContainer = game.aiContainer.missionContainer(dutch)
    }

    @Test
    fun `should move and equip tools`() {
        // given
        val freeColonist = UnitFactory.create(UnitType.FREE_COLONIST, dutch, nieuwAmsterdam.tile)

        fortOranje.goodsContainer.increaseGoodsQuantity(GoodsType.TOOLS, 100)
        val mission = TakeRoleEquipmentMission(freeColonist, fortOranje, unitRole(UnitRole.PIONEER), 4)
        dutchMissionContainer.addMission(mission)

        val playerAiContainer = game.aiContainer.playerAiContainer(dutch)
        playerAiContainer.findOrCreateColonySupplyGoods(fortOranje)
            .addSupplyGoodsReservation(mission.id, goodsType(GoodsType.TOOLS), 100)

        // when
        newTurnAndExecuteMission(dutch, 1)

        // then
        assertThat(freeColonist)
            .isAtLocation(fortOranje.tile)
            .hasRoleCount(4)
            .isUnitRole(UnitRole.PIONEER)
        assertThat(fortOranje).hasGoods(GoodsType.TOOLS, 20)
        assertThat(mission).isDone
        assertThat(playerAiContainer.colonySupplyGoods.getById(fortOranje).supplyReservations)
            .notContainsId(mission.id)
    }
}