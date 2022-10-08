package net.sf.freecol.common.model.ai.missions.pioneer

import net.sf.freecol.common.model.ColonyAssert.assertThat
import net.sf.freecol.common.model.MapIdEntitiesAssert.assertThat
import net.sf.freecol.common.model.UnitAssert.assertThat
import net.sf.freecol.common.model.UnitFactory
import net.sf.freecol.common.model.UnitRole
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.ai.missions.MissionHandlerBaseTestClass
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.colonyproduction.GoodsCollection
import net.sf.freecol.common.model.colonyproduction.GoodsCollectionAssert
import net.sf.freecol.common.model.colonyproduction.GoodsCollectionAssert.Companion.assertThat
import net.sf.freecol.common.model.specification.GoodsType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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
        val playerAiContainer = game.aiContainer.playerAiContainer(dutch)
        val freeColonist = UnitFactory.create(UnitType.FREE_COLONIST, dutch, nieuwAmsterdam.tile)

        fortOranje.goodsContainer.increaseGoodsQuantity(GoodsType.TOOLS, 100)
        val fortOranjeSupplyGoods = playerAiContainer.findOrCreateColonySupplyGoods(fortOranje)
        // colony should has supply
        fortOranjeSupplyGoods.supplyReservations.clear()

        val mission = TakeRoleEquipmentMission(freeColonist, fortOranje, unitRole(UnitRole.PIONEER), 4)
        dutchMissionContainer.addMission(mission)
        mission.makeReservation(game, dutch)

        // when
        newTurnAndExecuteMission(dutch, 1)

        // then
        assertThat(freeColonist)
            .isAtLocation(fortOranje.tile)
            .hasRoleCount(4)
            .isUnitRole(UnitRole.PIONEER)
        assertThat(fortOranje).hasGoods(GoodsType.TOOLS, 20)
        assertThat(mission).isDone

        assertThat(fortOranjeSupplyGoods.supplyReservations).notContainsId(mission.id)
    }
}