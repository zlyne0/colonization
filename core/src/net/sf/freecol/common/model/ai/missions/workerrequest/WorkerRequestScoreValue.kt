package net.sf.freecol.common.model.ai.missions.workerrequest

import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.specification.GoodsType
import promitech.colonization.ai.score.ScoreableObjectsList

class MultipleWorkerRequestScoreValue(
    val workersScores: ScoreableObjectsList<SingleWorkerRequestScoreValue>
) : WorkerRequestScoreValue {

    override var score: Int = 0
    override val location: Tile
    override val goodsType: GoodsType

    private var productionValue: Int = 0
    private var productionAmount: Int = 0
    private var workerType: UnitType
    private var unitPrice: Int

    init {
        if (workersScores.isEmpty()) {
            throw IllegalArgumentException("empty workerscore list")
        }
        val workerScore = workersScores.firstObj()
        goodsType = workerScore.goodsType
        workerType = workerScore.workerType
        unitPrice = workerScore.unitPrice
        productionAmount = workerScore.productionAmount
        productionValue += workerScore.productionValue
        location = workerScore.location
        score = productionValue
    }

    override fun workerType(): UnitType = workerType

    override fun toString(): String {
        var settlementName = ""
        if (location.hasSettlement()) {
            settlementName = location.settlement.name
        }
        var workersStr = ""
        for (workersScore in workersScores) {
            if (workersStr.isNotEmpty()) {
                workersStr += "\n"
            }
            workersStr += "    ${workersScore.workerType} price: ${workersScore.unitPrice} (${workersScore.goodsType} amount: ${workersScore.productionAmount}, value: ${workersScore.productionValue})"
        }
        return "${score}, first: ${workerType}, (${goodsType}:${productionAmount} $unitPrice / ${productionValue}), [${location.x},${location.y}] $settlementName\n" + workersStr
    }
}

class SingleWorkerRequestScoreValue(
    override val goodsType: GoodsType,
    val productionAmount: Int,
    val productionValue: Int,
    val workerType: UnitType,
    val unitPrice: Int,
    override val location: Tile
) : WorkerRequestScoreValue {

    override var score: Int = productionValue

    override fun workerType(): UnitType = workerType

    override fun toString(): String {
        var settlementName = ""
        if (location.hasSettlement()) {
            settlementName = location.settlement.name
        }
        return "${score}, ${workerType} (${goodsType}:${productionAmount} $unitPrice / ${productionValue}), [${location.x},${location.y}] $settlementName"
    }
}

interface WorkerRequestScoreValue : ScoreableObjectsList.Scoreable {
    var score : Int
    val location : Tile
    val goodsType : GoodsType

    fun goodsType(): GoodsType = goodsType

    fun location() : Tile = location
    fun workerType() : UnitType
    override fun score(): Int = score

}