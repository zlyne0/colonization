package net.sf.freecol.common.model.ai.missions.workerrequest

import net.sf.freecol.common.model.player.Player
import promitech.colonization.ai.score.ScoreableObjectsList

object ScorePolicy {

    fun scoreByWorkerValue(player: Player, scores: ScoreableObjectsList<WorkerRequestScoreValue>) {
        for (obj in scores) {
            when (obj) {
                is SingleWorkerRequestScoreValue -> scoreByWorkerValue(player, obj)
                is MultipleWorkerRequestScoreValue -> scoreByWorkerValue(player, obj)
            }
        }
        scores.sortAscending()
    }

    private fun scoreByWorkerValue(player: Player, obj: SingleWorkerRequestScoreValue) {
        val unitPrice = player.europe.getUnitPrice(obj.workerType)
        if (obj.productionValue != 0) {
            obj.score = unitPrice / obj.productionValue
        } else {
            obj.score = 10000
        }
    }

    private fun scoreByWorkerValue(player: Player, obj: MultipleWorkerRequestScoreValue) {
        var unitPriceSum = 0
        var productionValueSum = 0

        for (workerScore in obj.workersScores) {
            unitPriceSum += player.europe.getUnitPrice(workerScore.workerType)
            productionValueSum += workerScore.productionValue
            if (workerScore.productionValue != 0) {
                break
            }
        }
        if (productionValueSum != 0) {
            obj.score = unitPriceSum / productionValueSum
        } else {
            obj.score = 10000
        }
    }

    fun scoreByProductionValue(scores: ScoreableObjectsList<WorkerRequestScoreValue>) {
        for (obj in scores) {
            when (obj) {
                is SingleWorkerRequestScoreValue -> scoreByProductionValue(obj)
                is MultipleWorkerRequestScoreValue -> scoreByProductionValue(obj)
            }
        }
    }

    fun scoreByProductionValue(obj: SingleWorkerRequestScoreValue) {
        obj.score = obj.productionValue
    }

    fun scoreByProductionValue(obj: MultipleWorkerRequestScoreValue) {
        obj.score = obj.productionValue
    }

}