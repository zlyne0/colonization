package net.sf.freecol.common.model.ai.missions.workerrequest

import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.specification.GoodsType
import promitech.colonization.ai.score.ScoreableObjectsList

class WorkerRequestScoreValue  (
    val goodsType: GoodsType,
    val productionAmount: Int,
    val productionValue: Int,
    val workerType: UnitType,
    val location: Tile
) : ScoreableObjectsList.Scoreable {

    public var score = productionValue

    override fun toString(): String {
        return "workerType = $workerType, goodsType = $goodsType, amount = $productionAmount, productionValue = $productionValue"
    }

    override fun score(): Int {
        return score
    }
}

class WorkerRequestScore {

    fun createScore() : List<WorkerRequestScore> {
        return emptyList()
    }

}