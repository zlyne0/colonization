package net.sf.freecol.common.model.ai.missions.workerrequest

import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.ai.missions.workerrequest.WorkerRequestScoreValueComparator.eq
import net.sf.freecol.common.model.map.path.PathFinder
import org.junit.jupiter.api.Test
import promitech.colonization.ai.score.ScoreableObjectsListAssert
import promitech.colonization.savegame.Savegame1600BaseClass

class ColonyWorkerRequestPlaceCalculatorTest : Savegame1600BaseClass() {

    @Test
    fun `can generate worker request score`() {
        // given
        val pathFinder = PathFinder()
        val sut = ColonyWorkerRequestPlaceCalculator(dutch, game.map, pathFinder)

        // when
        val scores = sut.score(game.aiContainer.missionContainer(dutch))
        val scorePolicy = ScorePolicy.WorkerPriceToValue()
        scorePolicy.calculateScore(scores)
        scores.prettyPrint();

        // then
        ScoreableObjectsListAssert.assertThat(scores)
            .hasSize(8)
            .hasSumScore(216)
            .hasScore(0, 13, eq(fortMaurits.tile, UnitType.MASTER_FUR_TRADER))
            .hasScore(1, 15, eq(game.map.getSafeTile(21, 72), UnitType.FREE_COLONIST))
            .hasScore(2, 16, eq(game.map.getSafeTile(19, 76), UnitType.EXPERT_ORE_MINER))
            .hasScore(3, 20, eq(fortOranje.tile, UnitType.FREE_COLONIST))
            .hasScore(4, 27, eq(game.map.getSafeTile(19, 76), UnitType.FREE_COLONIST))
            .hasScore(5, 30, eq(fortOranje.tile, UnitType.MASTER_TOBACCONIST))
    }
}