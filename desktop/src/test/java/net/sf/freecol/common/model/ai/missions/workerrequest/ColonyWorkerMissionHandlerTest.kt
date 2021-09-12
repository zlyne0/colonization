package net.sf.freecol.common.model.ai.missions.workerrequest

import net.sf.freecol.common.model.ai.missions.MissionHandlerBaseTestClass
import org.junit.jupiter.api.Test
import net.sf.freecol.common.model.UnitFactory
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.TileAssert
import net.sf.freecol.common.model.ColonyAssert
import net.sf.freecol.common.model.specification.GoodsType
import promitech.colonization.savegame.AbstractMissionAssert

class ColonyWorkerMissionHandlerTest : MissionHandlerBaseTestClass() {
	
	lateinit var mission : ColonyWorkerMission
	
	@Test
	fun canBuildColony() {
		// given
		game.aiContainer.missionContainer(dutch).clearAllMissions()
		
		val tile = game.map.getTile(27, 72)
		val colonist = UnitFactory.create(UnitType.FREE_COLONIST, dutch, tile)
		
		mission = ColonyWorkerMission(tile, colonist, goodsType(GoodsType.FURS))
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

		mission = ColonyWorkerMission(nextToTile, colonist, goodsType(GoodsType.FURS))
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
		
}