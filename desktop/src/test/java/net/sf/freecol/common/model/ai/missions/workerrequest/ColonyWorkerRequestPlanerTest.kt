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

class ColonyWorkerRequestPlanerTest : Savegame1600BaseClass() {

    @Test
    fun `should generate list of colonists to buy`() {
        // given
        val player = dutch
        val playerMissionContainer = game.aiContainer.missionContainer(player)

        val transporter = UnitFactory.create(UnitType.GALLEON, player, nieuwAmsterdam.tile)
        val pathFinder = PathFinder()
        val entryPointTurnRange = EntryPointTurnRange(game.map, pathFinder, player, transporter)
        val placeCalculator = ColonyWorkerRequestPlaceCalculator(player, game.map, entryPointTurnRange)

        val purchaseRecommendations = ColonistsPurchaseRecommendations(player, playerMissionContainer)

        // when
        val recomendations = purchaseRecommendations.generateRecommendations(placeCalculator, entryPointTurnRange, transporter)
        purchaseRecommendations.printToLog(recomendations, entryPointTurnRange)

        // then
        ScoreableObjectsListAssert.assertThat(recomendations)
            .hasSize(6)
            .hasScore(0, 20, eq(fortOranje.tile, UnitType.FREE_COLONIST))
            .hasScore(1, 20, eq(nieuwAmsterdam.tile, UnitType.FREE_COLONIST))
            .hasScore(2, 25, eq(game.map.getSafeTile(27, 74), UnitType.FREE_COLONIST))
            .hasScore(3, 27, eq(fortMaurits.tile, UnitType.MASTER_FUR_TRADER))
            .hasScore(4, 29, eq(fortMaurits.tile, UnitType.FREE_COLONIST))
            .hasScore(5, 30, eq(fortOranje.tile, UnitType.MASTER_TOBACCONIST))
    }

    @Test
    fun `should buy recomended workers and create missions`() {
        // given
        val player = dutch
        val playerMissionContainer = game.aiContainer.missionContainer(player)

        val transporter = UnitFactory.create(UnitType.FRIGATE, player, player.europe)
        val pathFinder = PathFinder()
        val entryPointTurnRange = EntryPointTurnRange(game.map, pathFinder, player, transporter)
        val placeCalculator = ColonyWorkerRequestPlaceCalculator(player, game.map, entryPointTurnRange)

        val purchaseRecommendations = ColonistsPurchaseRecommendations(player, playerMissionContainer)

        val previewUnitsInEurope = MapIdEntities(player.europe.units)

        // when
        purchaseRecommendations.buyRecommendations(placeCalculator, entryPointTurnRange, transporter)

        // then
        val newUnits = MapIdEntities(player.europe.units).reduceBy(previewUnitsInEurope)
        assertThat(newUnits).hasSize(4)

        for (newUnit in newUnits) {
            assertThat(playerMissionContainer).hasMission(ColonyWorkerMission::class.java, newUnit)
        }
    }
}

