package net.sf.freecol.common.model.ai.missions.pioneer

import com.badlogic.gdx.utils.ObjectIntMap
import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.Settlement
import net.sf.freecol.common.model.player.Player
import net.sf.freecol.common.model.specification.GoodsTypeId
import net.sf.freecol.common.util.whenNotNull
import java.util.Collections

class CenterTilesImprovementRecommendations {
    private val centerTilesProductions: ObjectIntMap<String>
    private val orderedGoodsTypeIdBySettlementId: LinkedHashMap<String, String>
    private var sum = 0

    constructor(player: Player) {
        orderedGoodsTypeIdBySettlementId = LinkedHashMap(player.settlements.size())
        centerTilesProductions = ObjectIntMap<String>(player.settlements.size())

        val sorted = ArrayList<Settlement>(player.settlements.entities())
        sorted.sortWith(Collections.reverseOrder(Settlement.ID_LOW_FIRST_COMPARATOR))

        for (settlement in sorted) {
            settlement.tile.type.productionInfo.findFirstNotFoodUnattendedProduction()
                .whenNotNull { entry ->
                    centerTilesProductions.getAndIncrement(entry.key.id, 0, 1)
                    sum++
                    orderedGoodsTypeIdBySettlementId[settlement.id] = entry.key.id
                }
        }
    }

    constructor(vararg pairs: Pair<GoodsTypeId, Int>) {
        orderedGoodsTypeIdBySettlementId = LinkedHashMap(pairs.size)
        centerTilesProductions = ObjectIntMap<String>(pairs.size)

        for (pair in pairs) {
            this.centerTilesProductions.put(pair.first, pair.second)
        }
        for (centerTilesProduction in this.centerTilesProductions) {
            sum += centerTilesProduction.value
        }
    }

    enum class BenefitResult {
        OK, NO, CONDITIONAL
    }

    fun recommend(colony: Colony, fromGoodsId: GoodsTypeId, toGoodsId: GoodsTypeId): BenefitResult {
        val result = recommend(fromGoodsId, toGoodsId)
        if (result == BenefitResult.NO) {
            return result
        }

        var foundGoodsType = false
        for (mutableEntry in orderedGoodsTypeIdBySettlementId) {
            if (mutableEntry.value == fromGoodsId) {
                if (colony.id == mutableEntry.key && foundGoodsType) {
                    return BenefitResult.NO
                }
                foundGoodsType = true
            }
        }
        return result
    }

    fun recommend(fromGoodsId: GoodsTypeId, toGoodsId: GoodsTypeId): BenefitResult {
        val beforeFromCount = centerTilesProductions.get(fromGoodsId, 0)
        val beforeToCount = centerTilesProductions.get(toGoodsId, 0)

        val afterFromCount = beforeFromCount - 1
        val afterToCount = beforeToCount + 1

        // new production type
        if (beforeToCount == 0) {
            return BenefitResult.OK
        }
        // remove production type
        if (afterFromCount == 0) {
            return BenefitResult.NO
        }
        if (sum == 0 || centerTilesProductions.isEmpty) {
            return BenefitResult.CONDITIONAL
        }

        val oneToManyRatio = 1 / centerTilesProductions.size.toFloat()

        val beforeFromRatio = beforeFromCount / sum.toFloat()
        val beforeToRatio = beforeToCount / sum.toFloat()

        val afterFromRatio = afterFromCount / sum.toFloat()
        val afterToRatio = afterToCount / sum.toFloat()

        // shorter distance better
        val betterChange1 = Math.abs(beforeFromRatio - oneToManyRatio) > Math.abs(afterFromRatio - oneToManyRatio)
        val betterChange2 = Math.abs(beforeToRatio - oneToManyRatio) > Math.abs(afterToRatio - oneToManyRatio)

        if (betterChange1 && betterChange2) {
            return BenefitResult.OK
        }
        if (betterChange2) {
            return BenefitResult.OK
        }
        if (betterChange1) {
            return BenefitResult.CONDITIONAL
        }

        return BenefitResult.NO;
    }

    fun print() {
        println("centerTilesProductions.size = " + centerTilesProductions.size + " sum = " + sum)
        for (entry in centerTilesProductions.entries()) {
            println("${entry.key}(${entry.value})")
        }
        println("ordered")
        for (mutableEntry in orderedGoodsTypeIdBySettlementId) {
            println("   " + mutableEntry.key + " " + mutableEntry.value)
        }

    }
}
