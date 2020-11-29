package net.sf.freecol.common.model.ai.missions.goodsToSell

import net.sf.freecol.common.model.*
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.ai.missions.MissionHandlerBaseTestClass
import net.sf.freecol.common.model.specification.GoodsType
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import promitech.colonization.savegame.AbstractMissionAssert

class TransportGoodsToSellMissionHandlerTest : MissionHandlerBaseTestClass() {

	private val transactionLogger = TestMarketTransactionLogger()

	private lateinit var transporter: Unit
	private lateinit var fortOranje: Colony
	private lateinit var nieuwAmsterdam: Colony
	lateinit var mission : TransportGoodsToSellMission

	@Test
	fun canGoToColoniesTakeGoodsAndSellInEurope() {
		// given
		createMission()

		// when
		// move from europe to newWorld
		newTurnAndExecuteMission(dutch, 4)
		// move to colonies and load goods
		newTurnAndExecuteMission(dutch, 3)
		// back to europe
		newTurnAndExecuteMission(dutch, 4)

		// then
		SettlementAssert.assertThat(fortOranje)
			.hasGoods(GoodsType.RUM, 0)
			.hasGoods(GoodsType.SILVER, 0)
		SettlementAssert.assertThat(nieuwAmsterdam)
			.hasGoods(GoodsType.RUM, 0)
			.hasGoods(GoodsType.SILVER, 0)
		UnitAssert.assertThat(transporter)
			.hasGoods(GoodsType.RUM, 0)
			.hasGoods(GoodsType.SILVER, 0)

		Assertions.assertThat(transactionLogger.containsSale(GoodsType.RUM, 100)).isTrue()
		Assertions.assertThat(transactionLogger.containsSale(GoodsType.SILVER, 100)).isTrue()

		AbstractMissionAssert.assertThat(mission).isDone()
	}

	@Test
	fun canOmitNotOwnerColony() {
		// given
		createMission()
		fortOranje.owner = spain

		// when
		// move from europe to newWorld
		newTurnAndExecuteMission(dutch, 4)
		// move to colonies and load goods
		newTurnAndExecuteMission(dutch, 3)
		// back to europe
		newTurnAndExecuteMission(dutch, 4)

		// then
		SettlementAssert.assertThat(fortOranje)
			.hasGoods(GoodsType.RUM, 100)
			.hasGoods(GoodsType.SILVER, 0)
		SettlementAssert.assertThat(nieuwAmsterdam)
			.hasGoods(GoodsType.RUM, 0)
			.hasGoods(GoodsType.SILVER, 0)
		UnitAssert.assertThat(transporter)
			.hasGoods(GoodsType.RUM, 0)
			.hasGoods(GoodsType.SILVER, 0)

		Assertions.assertThat(transactionLogger.notContainsSale(GoodsType.RUM, 100)).isTrue()
		Assertions.assertThat(transactionLogger.containsSale(GoodsType.SILVER, 100)).isTrue()

		AbstractMissionAssert.assertThat(mission).isDone()
	}

	private fun createMission() {
		dutch.market().setMarketTransactionLogger(transactionLogger)

		transporter = UnitFactory.create(UnitType.GALLEON, dutch, dutch.europe)
		fortOranje = game.map.getTile(25, 75).settlement.asColony()
		nieuwAmsterdam = game.map.getTile(24, 78).settlement.asColony()

		fortOranje.getGoodsContainer().decreaseAllToZero()
		fortOranje.getGoodsContainer()
			.increaseGoodsQuantity(GoodsType.RUM, 100)
		nieuwAmsterdam.getGoodsContainer()
			.increaseGoodsQuantity("model.goods.cigars", 100)
			.increaseGoodsQuantity(GoodsType.SILVER, 100)
			.increaseGoodsQuantity("model.goods.cloth", 100)

		mission = TransportGoodsToSellMission(
			transporter,
			fortOranje.asColony(),
			setOf(fortOranje.getId(), nieuwAmsterdam.getId())
		)
		game.aiContainer.missionContainer(dutch).clearAllMissions()
		game.aiContainer.missionContainer(dutch).addMission(mission)
	}
}