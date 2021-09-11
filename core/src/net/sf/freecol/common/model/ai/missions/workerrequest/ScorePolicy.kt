package net.sf.freecol.common.model.ai.missions.workerrequest

import net.sf.freecol.common.model.player.Player
import promitech.colonization.ai.score.ScoreableObjectsList

/*
Two policies.
One policy worker price to production value(gold). Used when looking for worker to buy to produce something.
Secound policy, worker production value(gold). Used when has worker and looking for place to work.
 */
interface ScorePolicy {

    class WorkerPriceToValue(val entryPointTurnRange: EntryPointTurnRange, val player: Player) : ScorePolicy {

        override fun calculateScore(scores: ScoreableObjectsList<WorkerRequestScoreValue>) {
            for (obj in scores) {
                when (obj) {
                    is SingleWorkerRequestScoreValue -> scoreSingle(obj)
                    is MultipleWorkerRequestScoreValue -> scoreMultiple(obj)
                }
            }
            scores.sortAscending()
        }

        override fun scoreSingle(obj: SingleWorkerRequestScoreValue) {
            val range = entryPointTurnRange.trunsCost(obj.location)
            val unitPrice = player.europe.aiUnitPrice(obj.workerType)

            if (obj.productionValue != 0) {
                obj.score = (unitPrice / obj.productionValue) + range * 2
            } else {
                obj.score = 10000
            }
        }

        override fun scoreMultiple(obj: MultipleWorkerRequestScoreValue) {
            var unitPriceSum = 0
            var productionValueSum = 0
            val range = entryPointTurnRange.trunsCost(obj.location)

            for (workerScore in obj.workersScores) {
                unitPriceSum += player.europe.aiUnitPrice(workerScore.workerType)
                productionValueSum += workerScore.productionValue
                if (workerScore.productionValue != 0) {
                    break
                }
            }
            if (productionValueSum != 0) {
                obj.score = (unitPriceSum / productionValueSum) + range * 2
            } else {
                obj.score = 10000
            }
        }
    }

    class WorkerProductionValue(val entryPointTurnRange: EntryPointTurnRange) : ScorePolicy {

        override fun calculateScore(scores: ScoreableObjectsList<WorkerRequestScoreValue>) {
            for (obj in scores) {
                when (obj) {
                    is SingleWorkerRequestScoreValue -> scoreSingle(obj)
                    is MultipleWorkerRequestScoreValue -> scoreMultiple(obj)
                }
            }
            scores.sortDescending()
        }

        override fun scoreSingle(obj: SingleWorkerRequestScoreValue) {
            val range = entryPointTurnRange.trunsCost(obj.location)
            obj.score = obj.productionValue - range
        }
        override fun scoreMultiple(obj: MultipleWorkerRequestScoreValue) {
            val range = entryPointTurnRange.trunsCost(obj.location)
            obj.score = 0

            var levelWeight : Float = 1.0f
            for (workersScore in obj.workersScores) {
                if (workersScore.productionValue != 0) {
                    obj.score = (workersScore.productionValue * levelWeight).toInt() - range
                    break
                }
                levelWeight -= 0.25f
            }
        }
    }

    fun calculateScore(scores: ScoreableObjectsList<WorkerRequestScoreValue>)
    fun scoreSingle(obj: SingleWorkerRequestScoreValue)
    fun scoreMultiple(obj: MultipleWorkerRequestScoreValue)

}