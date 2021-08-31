package net.sf.freecol.common.model.ai.missions.workerrequest

import net.sf.freecol.common.model.ai.missions.workerrequest.ScorePolicy
import net.sf.freecol.common.model.map.path.PathFinder
import org.junit.jupiter.api.Test
import promitech.colonization.ai.Units
import promitech.colonization.savegame.Savegame1600BaseClass

class ColonyWorkerRequestPlanerTest : Savegame1600BaseClass() {

    @Test
    fun `can generate worker request score`() {
        // given
        val pathFinder = PathFinder()
        val sut = ColonyWorkerRequestPlaner(game.map, pathFinder)

        val transporter = Units.findCarrier(dutch)

        // when

        // when
        val scores = sut.score(dutch, transporter)
        ScorePolicy.scoreByWorkerValue(dutch, scores)
        //scores.prettyPrint();
        //scores.prettyPrint();
        sut.debugToConsole(dutch)

        // then
    }
}