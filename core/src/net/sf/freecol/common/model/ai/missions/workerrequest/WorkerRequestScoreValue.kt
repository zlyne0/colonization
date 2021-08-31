package net.sf.freecol.common.model.ai.missions.workerrequest

import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.player.Player
import net.sf.freecol.common.model.specification.GoodsType
import promitech.colonization.ai.score.ScoreableObjectsList

class MultipleWorkerRequestScoreValue(
    val workersScores: ScoreableObjectsList<SingleWorkerRequestScoreValue>
) : WorkerRequestScoreValue {

    override var score: Int = 0
    override lateinit var location: Tile

    var productionValue: Int = 0
    private lateinit var goodsType: GoodsType
    private var productionAmount: Int = 0
    private lateinit var workerType: UnitType

    init {
        var foundWorker = false
        for (workerScore in workersScores) {
            if (workerScore.score != 0) {
                foundWorker = true
                goodsType = workerScore.goodsType
                workerType = workerScore.workerType
                productionAmount = workerScore.productionAmount
                productionValue += workerScore.productionValue
                location = workerScore.location
                break
            }
        }
        if (!foundWorker) {
            throw IllegalArgumentException("empty workerscore list")
        }
        score = productionValue
    }

    override fun workerType(): UnitType = workerType

    override fun toPrettyString(player: Player) : String {
        val unitPrice = player.europe.getUnitPrice(workerType)
        var settlementName = ""
        if (location.hasSettlement()) {
            settlementName = location.settlement.name
        }
        return "${score}, ${workerType} (${goodsType} $unitPrice / ${productionValue}), [${location.x},${location.y}] $settlementName"
    }
}

class SingleWorkerRequestScoreValue(
    val goodsType: GoodsType,
    val productionAmount: Int,
    val productionValue: Int,
    val workerType: UnitType,
    override var location: Tile
) : WorkerRequestScoreValue {

    override var score: Int = productionValue

    override fun workerType(): UnitType = workerType

    override fun toPrettyString(player: Player) : String {
        val unitPrice = player.europe.getUnitPrice(workerType)
        var settlementName = ""
        if (location.hasSettlement()) {
            settlementName = location.settlement.name
        }
        return "${score}, ${workerType} (${goodsType} $unitPrice / ${productionValue}), [${location.x},${location.y}] $settlementName"
    }
}

interface WorkerRequestScoreValue : ScoreableObjectsList.Scoreable {
    var score : Int
    var location : Tile

    fun location() : Tile = location
    fun toPrettyString(player: Player) : String
    fun workerType() : UnitType
    override fun score(): Int = score
}