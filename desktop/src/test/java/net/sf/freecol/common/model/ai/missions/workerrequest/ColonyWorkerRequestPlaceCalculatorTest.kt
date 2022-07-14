package net.sf.freecol.common.model.ai.missions.workerrequest

import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.ai.missions.workerrequest.ScorePolicy
import net.sf.freecol.common.model.map.path.PathFinder
import org.junit.jupiter.api.Test
import promitech.colonization.ai.score.ScoreableObjectsListAssert
import promitech.colonization.savegame.Savegame1600BaseClass

import net.sf.freecol.common.model.ai.missions.workerrequest.WorkerRequestScoreValueComparator.eq
import promitech.colonization.ai.findCarrier

class ColonyWorkerRequestPlaceCalculatorTest : Savegame1600BaseClass() {

    @Test
    fun `can generate worker request score`() {
        // given
        val pathFinder = PathFinder()

        val transporter = dutch.findCarrier()
        val entryPointTurnRange = EntryPointTurnRange(game.map, pathFinder, dutch, transporter)
        val sut = ColonyWorkerRequestPlaceCalculator(dutch, game.map, entryPointTurnRange)

        // when
        val scores = sut.score(game.aiContainer.missionContainer(dutch))
        val scorePolicy = ScorePolicy.WorkerPriceToValue(entryPointTurnRange, dutch)
        scorePolicy.calculateScore(scores)
        //scores.prettyPrint();
        //sut.debugToConsole()

        // then
        ScoreableObjectsListAssert.assertThat(scores)
            .hasScore(0, 4, eq(game.map.getSafeTile(27, 74), UnitType.EXPERT_FUR_TRAPPER))
            .hasScore(1, 20, eq(fortOranje.tile, UnitType.FREE_COLONIST))
            .hasScore(2, 20, eq(nieuwAmsterdam.tile, UnitType.FREE_COLONIST))
            .hasScore(3, 23, eq(fortMaurits.tile, UnitType.MASTER_FUR_TRADER))
            .hasScore(4, 25, eq(fortMaurits.tile, UnitType.FREE_COLONIST))
    }
}