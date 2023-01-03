package promitech.colonization.ai

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.ProductionSummary
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.colonyproduction.ColonyPlan
import net.sf.freecol.common.model.player.Player
import net.sf.freecol.common.model.specification.BuildingType
import net.sf.freecol.common.model.specification.GoodsType
import promitech.colonization.ai.score.ScoreableObjectsList

@JvmInline
value class TurnRateOfReturn(private val value: Int) {
    fun isLower(turnRateOfReturn: TurnRateOfReturn): Boolean {
        return value < turnRateOfReturn.value
    }
}

data class BuildingTypeScore(
    val buildingType: BuildingType,
    val profit: Int,
    val colony: Colony,
    val price: Int,
    val turnRateOfReturn: TurnRateOfReturn
) : ScoreableObjectsList.Scoreable {

    override fun score(): Int {
        return profit
    }

    fun maxProfit(score: BuildingTypeScore?): BuildingTypeScore {
        if (score == null) {
            return this
        }
        if (this.profit > score.profit) {
            return this
        }
        if (this.profit == score.profit) {
            if (this.turnRateOfReturn.isLower(score.turnRateOfReturn)) {
                return this
            } else {
                return score
            }
        } else {
            return score
        }
    }
}

class ColonyBuildingPlaner {

    private val mostValuableGoods: Set<GoodsType>
    private val hammersGoodsType: GoodsType
    private val productionBuildingModifications: List<BuildingType>

    init {
        mostValuableGoods = HashSet()
        mostValuableGoods.addAll(ColonyPlan.Plan.RawMaterials.goodsTypes)
        mostValuableGoods.addAll(ColonyPlan.Plan.ProcessedMaterials.goodsTypes)

        hammersGoodsType = Specification.instance.goodsTypes.getById(GoodsType.HAMMERS)
        productionBuildingModifications = mutableListOf<BuildingType>(
            Specification.instance.buildingTypes.getById(BuildingType.DOCKS),
            Specification.instance.buildingTypes.getById(BuildingType.WAREHOUSE),
            Specification.instance.buildingTypes.getById(BuildingType.WAREHOUSE_EXPANSION),
        )
    }

    fun generateBuildingQueue(player: Player) {
        for (settlement in player.settlements) {
            val colony = settlement.asColony()
            colony.clearBuildingQueue()
            productionValueBuildingPlan(player, colony).whenNotNull { bt -> colony.addToBuildingQueue(bt.buildingType) }
        }
    }

    fun productionValueBuildingPlan(player: Player, colony: Colony): BuildingTypeScore? {
        val productionSummary = colony.productionSummary()
        val colonyGoldProductionValue = player.market().getSalePrice(productionSummary)

        var theBestProfit: BuildingTypeScore? = null

        for (buildingType in Specification.instance.buildingTypes) {
            if (canAddBuildingToColony(colony, buildingType)
                && buildingProduceMostValuableGoods(buildingType)
                && isBuildingTypeProduceTypeOnColonyProduction(buildingType, productionSummary)
            ) {
                calculateGainWithBuildingType(player, colony, buildingType, colonyGoldProductionValue)
                    .whenNotNull { buildingTypeScore ->
                        theBestProfit = buildingTypeScore.maxProfit(theBestProfit)
                    }
            }
        }
        for (buildingType in productionBuildingModifications) {
            if (canAddBuildingToColony(colony, buildingType)) {
                calculateGainWithBuildingType(player, colony, buildingType, colonyGoldProductionValue)
                    .whenNotNull { buildingTypeScore ->
                        theBestProfit = buildingTypeScore.maxProfit(theBestProfit)
                    }
            }
        }
        return theBestProfit
    }

    private fun calculateGainWithBuildingType(
        player: Player,
        colony: Colony,
        buildingType: BuildingType,
        colonyGoldProductionValue: Int
    ): BuildingTypeScore? {
        val virtualColony = ColonyPlan(colony)
            .withConsumeWarehouseResources(true)
            .withIgnoreIndianOwner()
            .withMinimumProductionLimit(2)
            .addBuilding(buildingType)
        virtualColony.execute2(ColonyPlan.Plan.MostValuable, ColonyPlan.Plan.Bell, ColonyPlan.Plan.Food)
        val virtualProductionConsumption = virtualColony.productionConsumption()
        val virtualGold = player.market().getSalePrice(virtualProductionConsumption)

        val profit = virtualGold - colonyGoldProductionValue

        val priceForBuilding = colony.getAIPriceForBuilding(buildingType)
        //println("$buildingType actual: $colonyGoldProductionValue, upgrade: $virtualGold, profit: $profit, priceForBuilding: $priceForBuilding")

        if (profit > 0) {
            return BuildingTypeScore(buildingType, profit, colony, priceForBuilding, TurnRateOfReturn(priceForBuilding / profit))
        } else {
            return null
        }
    }

    fun buildingValue(player: Player, colony: Colony) {
        val virtualColony = ColonyPlan(colony).withConsumeWarehouseResources(true)

        virtualColony.execute2(ColonyPlan.Plan.Building, ColonyPlan.Plan.Food)
        val hammersProductionConsumption = virtualColony.productionConsumption()
        val hammersGoldValue = hammersProductionGoldValue(hammersProductionConsumption, player)

        virtualColony.execute2(ColonyPlan.Plan.MostValuable, ColonyPlan.Plan.Food)
        val mostValuableProductionConsumption = virtualColony.productionConsumption()
        val mostValuableGoodsGoldValue = player.market().getSalePrice(mostValuableProductionConsumption)


        println("buildable value, hammersGoldValue: $hammersGoldValue, mostValuableGoodsGoldValue: $mostValuableGoodsGoldValue")
    }

    private fun buildingProduceMostValuableGoods(buildingType: BuildingType): Boolean {
        for (production in buildingType.productionInfo.productions) {
            for (outputEntry in production.outputEntries()) {
                if (mostValuableGoods.contains(outputEntry.key)) {
                    return true
                }
            }
        }
        return false
    }

    private fun isBuildingTypeProduceTypeOnColonyProduction(
        buildingType: BuildingType,
        colonyProductionGoods: ProductionSummary
    ): Boolean {
        for (production in buildingType.productionInfo.productions) {
            for (outputEntry in production.outputEntries()) {
                return colonyProductionGoods.getQuantity(outputEntry.key) != 0
            }
        }
        return false
    }

    private fun hammersProductionGoldValue(productionSummary: ProductionSummary, player: Player): Int {
        return player.market().buildingGoodsPrice(
            hammersGoodsType,
            productionSummary.getQuantity(hammersGoodsType)
        )
    }

    private fun canAddBuildingToColony(colony: Colony, buildingType: BuildingType): Boolean {
        return colony.getNoBuildReason(buildingType) == Colony.NoBuildReason.NONE
            && !colony.isBuildingAlreadyBuilt(buildingType)
    }

}

inline fun <T : Any> T?.whenNotNull(action: (T) -> Unit) {
    if (this != null) {
        action.invoke(this)
    }
}
