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

	@Test
	fun `should sell goods, buy colonist and transport them to new world`() {
		// given
		game.aiContainer.missionContainer(dutch).clearAllMissions()

		val missionContainer = game.aiContainer.missionContainer(dutch)
		dutch.subtractGold(dutch.gold)

		removeAllNavy(dutch)
		removeAllUnitsFromLocation(dutch.europe)

		// create colonists in europe
		val dockFreeColonists = UnitFactory.create(UnitType.FREE_COLONIST, dutch, dutch.europe)
		val colonyWorkerMission = ColonyWorkerMission(nieuwAmsterdam.tile, dockFreeColonists, goodsType(GoodsType.FURS))
		missionContainer.addMission(colonyWorkerMission)

		// prepare caravel with goods to sell
		val dutchEntryLocation = game.map.getSafeTile(dutch.entryLocation)

		val caravel = UnitFactory.create(UnitType.CARAVEL, dutch, dutchEntryLocation)
		caravel.goodsContainer.increaseGoodsQuantity(GoodsType.SILVER, 100)
		caravel.sailUnitToEurope(dutchEntryLocation)

		val transportGoodsToSellMission = TransportGoodsToSellMission(caravel, nieuwAmsterdam, emptySet())
		transportGoodsToSellMission.changePhase(TransportGoodsToSellMission.Phase.MOVE_TO_EUROPE)
		missionContainer.addMission(transportGoodsToSellMission)

		// when
		newTurnAndExecuteMission(dutch, caravel.sailTurns)
		planMissions(dutch)
		// should buy units
		newTurnAndExecuteMission(dutch, 1)

		// then
		Assertions.assertThat(transactionLogger.containsSale(GoodsType.SILVER, 100)).isTrue()
		UnitAssert.assertThat(caravel)
			.isAtLocation(dutch.highSeas)
			.hasUnitsSize(2);

		for (unit in caravel.unitContainer.units) {
			PlayerMissionsContainerAssert.assertThat(missionContainer)
				.hasMission(ColonyWorkerMission::class.java, unit)
		}
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