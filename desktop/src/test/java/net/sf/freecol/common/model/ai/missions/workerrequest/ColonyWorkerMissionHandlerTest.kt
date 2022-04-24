package net.sf.freecol.common.model.ai.missions.workerrequest

import net.sf.freecol.common.model.ai.missions.MissionHandlerBaseTestClass
import org.junit.jupiter.api.Test
import net.sf.freecol.common.model.UnitFactory
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.TileAssert
import net.sf.freecol.common.model.ColonyAssert
import net.sf.freecol.common.model.UnitAssert
import net.sf.freecol.common.model.UnitLocation
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainerAssert
import net.sf.freecol.common.model.ai.missions.goodsToSell.TestMarketTransactionLogger
import net.sf.freecol.common.model.ai.missions.goodsToSell.TransportGoodsToSellMission
import net.sf.freecol.common.model.player.Player
import net.sf.freecol.common.model.specification.GoodsType
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import promitech.colonization.savegame.AbstractMissionAssert

class ColonyWorkerMissionHandlerTest : MissionHandlerBaseTestClass() {

	val transactionLogger = TestMarketTransactionLogger()

	@BeforeEach
	override fun setup() {
		super.setup()

		dutch.market().setMarketTransactionLogger(transactionLogger)
	}

	@Test
	fun canBuildColony() {
		// given
		game.aiContainer.missionContainer(dutch).clearAllMissions()
		
		val tile = game.map.getTile(27, 72)
		val colonist = UnitFactory.create(UnitType.FREE_COLONIST, dutch, tile)
		
		val mission = ColonyWorkerMission(tile, colonist, goodsType(GoodsType.FURS))
		game.aiContainer.missionContainer(dutch).addMission(mission)
		
		// when
		newTurnAndExecuteMission(dutch)
		
		// then
		AbstractMissionAssert.assertThat(mission).isDone()
		TileAssert.assertThat(tile).hasSettlementOwnBy(dutch)
		ColonyAssert.assertThat(tile.getSettlement().asColony()).hasSize(1)
	}

	@Test
	fun canMoveUnitAndIncreaseWorkerCount() {
		// given
		game.aiContainer.missionContainer(dutch).clearAllMissions()
		
		val tile = game.map.getTile(27, 72)
		val nextToTile = game.map.getTile(27, 74)
		val colonist = UnitFactory.create(UnitType.FREE_COLONIST, dutch, tile)

		val mission = ColonyWorkerMission(nextToTile, colonist, goodsType(GoodsType.FURS))
		game.aiContainer.missionContainer(dutch).addMission(mission)
		
		// additional colonist
		game.aiContainer.missionContainer(dutch).addMission(
			ColonyWorkerMission(tile, UnitFactory.create(UnitType.FREE_COLONIST, dutch, tile), goodsType(GoodsType.FURS))
		)
				
		// when
		newTurnAndExecuteMission(dutch, 2)
		
		// then
		AbstractMissionAssert.assertThat(mission).isDone()
		TileAssert.assertThat(tile).hasSettlementOwnBy(dutch)
		ColonyAssert.assertThat(tile.getSettlement().asColony()).hasSize(1)
	}

	fun removeAllUnitsFromLocation(unitLocation: UnitLocation) {
		for (unit in unitLocation.units.copy()) {
			unit.owner.removeUnit(unit)
		}
	}

	fun removeAllNavy(player: Player) {
		for (unit in player.units.copy()) {
			if (unit.isNaval) {
				player.removeUnit(unit)
			}
		}
	}
}