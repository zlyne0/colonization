package net.sf.freecol.common.model.ai.missions.workerrequest

import net.sf.freecol.common.model.MapIdEntities
import net.sf.freecol.common.model.MapIdEntitiesAssert.*
import net.sf.freecol.common.model.UnitFactory
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainerAssert.*
import net.sf.freecol.common.model.map.path.PathFinder
import org.junit.jupiter.api.Test
import promitech.colonization.ai.score.ScoreableObjectsListAssert
import promitech.colonization.savegame.Savegame1600BaseClass

import net.sf.freecol.common.model.ai.missions.workerrequest.WorkerRequestScoreValueComparator.eq
import promitech.colonization.ai.Units
import promitech.colonization.ai.purchase.ColonistsPurchaseRecommendations

class ColonyWorkerRequestPlanerTest : Savegame1600BaseClass() {

    @Test
    fun `should generate list of colonists to buy`() {
        // given
        val player = dutch
        val playerMissionContainer = game.aiContainer.missionContainer(player)
        val pathFinder = PathFinder()

        val transporter = UnitFactory.create(UnitType.GALLEON, player, nieuwAmsterdam.tile)
        val transporterCapacity = Units.transporterCapacity(transporter, playerMissionContainer)
        val placeCalculator = ColonyWorkerRequestPlaceCalculator(player, game.map, pathFinder)

        val purchaseRecommendations = ColonistsPurchaseRecommendations(game, player, playerMissionContainer, placeCalculator)

        // when
        val recommendations = purchaseRecommendations.generateRecommendations(transporterCapacity)
        purchaseRecommendations.printToLog(recommendations)

        // then
        ScoreableObjectsListAssert.assertThat(recommendations)
            .hasSize(6)
            .hasScore(0, 13, eq(fortMaurits.tile, UnitType.MASTER_FUR_TRADER))
            .hasScore(1, 15, eq(fortMaurits.tile, UnitType.FREE_COLONIST))
            .hasScore(2, 16, eq(game.map.getSafeTile(19, 76), UnitType.EXPERT_ORE_MINER))
            .hasScore(3, 20, eq(fortOranje.tile, UnitType.FREE_COLONIST))
            .hasScore(4, 27, eq(game.map.getSafeTile(19, 76), UnitType.FREE_COLONIST))
            .hasScore(5, 30, eq(fortOranje.tile, UnitType.MASTER_TOBACCONIST))
    }

    @Test
    fun `should buy recomended workers and create missions`() {
        // given
        val player = dutch
        val playerMissionContainer = game.aiContainer.missionContainer(player)

        val transporter = UnitFactory.create(UnitType.FRIGATE, player, player.europe)
        val workersNumber = Units.transporterCapacity(transporter, playerMissionContainer)
        val placeCalculator = ColonyWorkerRequestPlaceCalculator(player, game.map, PathFinder())

        val purchaseRecommendations = ColonistsPurchaseRecommendations(game, player, playerMissionContainer, placeCalculator)

        val previewUnitsInEurope = MapIdEntities(player.europe.units)

        // when
        purchaseRecommendations.buyRecommendations(workersNumber)

        // then
        val newUnits = MapIdEntities(player.europe.units).reduceBy(previewUnitsInEurope)
        assertThat(newUnits).hasSize(4)

        for (newUnit in newUnits) {
            assertThat(playerMissionContainer).hasMission(ColonyWorkerMission::class.java, newUnit)
        }
    }
}

