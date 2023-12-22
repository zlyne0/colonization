package net.sf.freecol.common.model.ai.missions.workerrequest

import promitech.colonization.ai.score.ScoreableObjectsList

/*
Two policies.
One policy worker price to production value(gold). Used when looking for worker to buy to produce something.
Second policy, worker production value(gold). Used when has worker and looking for place to work.
 */
interface ScorePolicy {

    class WorkerPriceToValue: ScorePolicy {

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
            if (obj.productionValue != 0) {
                obj.score = (obj.unitPrice / obj.productionValue)
            } else {
                obj.score = 10000
            }
        }

        override fun scoreMultiple(obj: MultipleWorkerRequestScoreValue) {
            var unitPriceSum = 0
            var productionValueSum = 0

            for (workerScore in obj.workersScores) {
                unitPriceSum += workerScore.unitPrice
                productionValueSum += workerScore.productionValue
                if (workerScore.productionValue != 0) {
                    break
                }
            }
            if (productionValueSum != 0) {
                obj.score = (unitPriceSum / productionValueSum)
            } else {
                obj.score = 10000
            }
        }
    }

    class WorkerProductionValue: ScorePolicy {

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
            obj.score = obj.productionValue
        }

        override fun scoreMultiple(obj: MultipleWorkerRequestScoreValue) {
            obj.score = 0

            var levelWeight : Float = 1.0f
            for (workersScore in obj.workersScores) {
                if (workersScore.productionValue != 0) {
                    obj.score = (workersScore.productionValue * levelWeight).toInt()
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