package promitech.colonization.ai

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.ProductionSummary
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.colonyproduction.ColonyPlan
import net.sf.freecol.common.model.colonyproduction.ColonyPlan.ProductionProfile.Companion.Building
import net.sf.freecol.common.model.colonyproduction.ColonyPlan.ProductionProfile.Companion.MostValuable
import net.sf.freecol.common.model.colonyproduction.ColonyProductionLogger
import net.sf.freecol.common.model.player.Player
import net.sf.freecol.common.model.specification.BuildingType
import net.sf.freecol.common.model.specification.GoodsType
import net.sf.freecol.common.util.whenNotNull
import java.util.*

@JvmInline
value class TurnRateOfReturn(private val value: Int) {
    companion object {
        val firstLower = object : Comparator<TurnRateOfReturn> {
            override fun compare(p0: TurnRateOfReturn, p1: TurnRateOfReturn): Int {
                return p0.value - p1.value
            }
        }
    }
}

data class BuildingTypeScore(
    val buildingType: BuildingType,
    val profit: Int,
    val colony: Colony,
    val price: Int,
    val turnRateOfReturn: TurnRateOfReturn
) {

    fun maxProfit(score: BuildingTypeScore?): BuildingTypeScore {
        if (score == null) {
            return this
        }
        val c = firstHigherProfit.compare(this, score)
        if (c > 0) {
            return score
        } else {
            return this
        }
    }

    companion object {
        val firstHigherProfit = compareByDescending<BuildingTypeScore>( { bts -> bts.profit } )
            .then( compareBy( TurnRateOfReturn.firstLower, { bts -> bts.turnRateOfReturn }) )
    }
}

class ProductionPlanSummary(
    val colony: Colony,
    val mostValuableGoldValue: Int,
    val buildingTypeScore: BuildingTypeScore? = null,
    val hammersGoldValue: Int = 0,
    val besidesHammersGoldValue: Int = 0
) {
    companion object {
        val firstHigherBuildingTypeProfit: Comparator<ProductionPlanSummary> = compareBy(
            nullsLast(BuildingTypeScore.firstHigherProfit),
            { productionPlanSummary -> productionPlanSummary.buildingTypeScore }
        )
    }
}

data class ColonyPlanProductionRecommendation(
    val colony: Colony,
    val productionProfile: ColonyPlan.ProductionProfile,
    val buildingTypeScore: BuildingTypeScore? = null
)

class ColonyProductionPlaner {

    private val mostValuableGoods: Set<GoodsType>
    private val hammersGoodsType: GoodsType
    private val productionBuildingModifications: List<BuildingType>

    private val buildingToMostValuableProfileRatio = 0.25f
    private val minimumProfitFromBuilding = 30

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

    fun generateAndSetColonyProductionPlan(player: Player) {
        val colonyPlanProduction = generateColonyPlanProductionRecommendations(player)

        for (colonyPlanRecommendation: ColonyPlanProductionRecommendation in colonyPlanProduction) {
            val colony = colonyPlanRecommendation.colony
            if (ColonyProductionLogger.logger.isDebug) {
                logProductionRecommendation(colonyPlanRecommendation, player)
            }

            if (colonyPlanRecommendation.buildingTypeScore != null) {
                colony.clearBuildingQueue()
                colony.addToBuildingQueue(colonyPlanRecommendation.buildingTypeScore.buildingType)
            }
            ColonyPlan(colony)
                .withIgnoreIndianOwner()
                .withConsumeWarehouseResources(true)
                .withMinimumProductionLimit(2)
                .executeMaximizationProduction(colonyPlanRecommendation.productionProfile)
                .allocateWorkers()
        }

    }

    private fun logProductionRecommendation(colonyPlanRecommendation: ColonyPlanProductionRecommendation, player: Player) {
        if (colonyPlanRecommendation.productionProfile == Building) {
            ColonyProductionLogger.logger.debug(
                "player[%s].colony[%s].setProductionProfile[%s].building[%s]",
                player.id,
                colonyPlanRecommendation.colony.name,
                colonyPlanRecommendation.productionProfile.name,
                colonyPlanRecommendation.buildingTypeScore!!.buildingType
            )
        } else {
            ColonyProductionLogger.logger.debug(
                "player[%s].colony[%s].setProductionProfile[%s]",
                player.id,
                colonyPlanRecommendation.colony.name,
                colonyPlanRecommendation.productionProfile.name
            )
        }
    }

    fun generateColonyPlanProductionRecommendations(player: Player): List<ColonyPlanProductionRecommendation> {
        val summaryList: List<ProductionPlanSummary> = calculateSettlementsBuildingValue(player)
        val globalMostValuableProductionGoldValue: Int = summaryList.sumOf { pps -> pps.mostValuableGoldValue }

        val profitabilityLimit = buildingToMostValuableProfileRatio * globalMostValuableProductionGoldValue

        val recommendations = ArrayList<ColonyPlanProductionRecommendation>(summaryList.size)

        var noProduceGoldValue = 0
        for (productionPlanSummary in summaryList) {
            if (productionPlanSummary.buildingTypeScore != null) {
                val colonyNoProduceGoldValue = noProduceGoldValue + productionPlanSummary.mostValuableGoldValue
                if (colonyNoProduceGoldValue <= profitabilityLimit && minimumProfitFromBuilding <= productionPlanSummary.buildingTypeScore.profit) {
                    noProduceGoldValue += productionPlanSummary.mostValuableGoldValue
                    recommendations.add(ColonyPlanProductionRecommendation(productionPlanSummary.colony, Building, productionPlanSummary.buildingTypeScore))
                } else {
                    recommendations.add(ColonyPlanProductionRecommendation(productionPlanSummary.colony, MostValuable))
                }
            } else {
                recommendations.add(ColonyPlanProductionRecommendation(productionPlanSummary.colony, MostValuable))
            }
        }
        return recommendations
    }

    fun prettyPrintSettlementsBuildingValue(summaryList: List<ProductionPlanSummary>) {
        val globalMostValuableProductionGoldValue: Int = summaryList.sumOf { pps -> pps.mostValuableGoldValue }
        val profitabilityLimit = buildingToMostValuableProfileRatio * globalMostValuableProductionGoldValue
        var noProduceGoldValue = 0

        println("global: $globalMostValuableProductionGoldValue, limit: $profitabilityLimit")
        for (productionPlanSummary in summaryList) {
            println("colony: " + productionPlanSummary.colony.name)
            println("   noProduceGoldValue: " + noProduceGoldValue)
            println("   mostValuableGoldValue: " + productionPlanSummary.mostValuableGoldValue)
            var productionProfile = MostValuable
            if (productionPlanSummary.buildingTypeScore != null) {
                println("   buildingType: " + productionPlanSummary.buildingTypeScore.buildingType)
                println("   profit: " + productionPlanSummary.buildingTypeScore.profit)
                println("   turnRateOfReturn: " + productionPlanSummary.buildingTypeScore.turnRateOfReturn)
                val colonyNoProduceGoldValue = noProduceGoldValue + productionPlanSummary.mostValuableGoldValue
                if (colonyNoProduceGoldValue <= profitabilityLimit && minimumProfitFromBuilding <= productionPlanSummary.buildingTypeScore.profit) {
                    noProduceGoldValue += productionPlanSummary.mostValuableGoldValue
                    productionProfile = Building
                }
            }
            println("   profile: $productionProfile")
        }
    }

    fun calculateSettlementsBuildingValue(player: Player): List<ProductionPlanSummary> {
        val summaryList = ArrayList<ProductionPlanSummary>(player.settlements.size())
        for (settlement in player.settlements) {
            val colony = settlement.asColony()
            calculateBuildingValue(player, colony).let { productionPlanSummary ->
                summaryList.add(productionPlanSummary)
            }
        }
        Collections.sort(summaryList, ProductionPlanSummary.firstHigherBuildingTypeProfit)
        return summaryList
    }

    private fun calculateBuildingValue(player: Player, colony: Colony): ProductionPlanSummary {
        val virtualColony = ColonyPlan(colony)
            .withConsumeWarehouseResources(true)
            .withIgnoreIndianOwner()
            .withMinimumProductionLimit(2)

        virtualColony.execute2(MostValuable)
        val mostValuableProductionConsumption = virtualColony.productionConsumption()
        val mostValuableGoodsGoldValue = player.market().getSalePrice(mostValuableProductionConsumption)

        val buildingTypeToProduce = findBuildingTypeToProduce(player, colony, mostValuableProductionConsumption)
        if (buildingTypeToProduce == null) {
            return ProductionPlanSummary(
                colony = colony,
                mostValuableGoldValue = mostValuableGoodsGoldValue
            )
        }

        virtualColony.execute2(Building)
        val hammersProductionConsumption = virtualColony.productionConsumption()
        val hammersGoldValue = hammersProductionGoldValue(hammersProductionConsumption, player)
        val besidesHammersGoldValue = player.market().getSalePrice(hammersProductionConsumption)

        return ProductionPlanSummary(
            colony = colony,
            mostValuableGoldValue = mostValuableGoodsGoldValue,
            buildingTypeScore = buildingTypeToProduce,
            hammersGoldValue = hammersGoldValue,
            besidesHammersGoldValue = besidesHammersGoldValue
        )
    }

    private fun findBuildingTypeToProduce(
        player: Player,
        colony: Colony,
        mostValuablePlanProductionSummary: ProductionSummary
    ): BuildingTypeScore? {
        val colonyGoldProductionValue = player.market().getSalePrice(mostValuablePlanProductionSummary)

        var theBestProfit: BuildingTypeScore? = null

        for (buildingType in Specification.instance.buildingTypes) {
            if (canAddBuildingToColony(colony, buildingType)
                && buildingProduceMostValuableGoods(buildingType)
                && isBuildingTypeProduceTypeOnColonyProduction(buildingType, mostValuablePlanProductionSummary)
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

    companion object {

        @JvmStatic
        fun createPlanForNewColony(colony: Colony) {
            ColonyPlan(colony)
                .withIgnoreIndianOwner()
                .withConsumeWarehouseResources(true)
                .withMinimumProductionLimit(2)
                .executeMaximizationProduction(ColonyPlan.ProductionProfile.MostValuable)
                .allocateWorkers()
        }

        @JvmStatic
        fun initColonyBuilderUnit(colony: Colony, builder: net.sf.freecol.common.model.Unit) {
            colony.updateModelOnWorkerAllocationOrGoodsTransfer()
            val maxProd = colony.productionSimulation().determinePotentialMaxTilesProduction(builder.unitType, false)
            if (maxProd != null) {
                colony.addWorkerToTerrain(maxProd.colonyTile, builder)
            } else {
                addUnitToRandomBuilding(colony, builder)
            }
            colony.updateColonyPopulation()
        }

        private fun addUnitToRandomBuilding(colony: Colony, unit: net.sf.freecol.common.model.Unit) {
            val townHall = colony.findBuildingByType(BuildingType.TOWN_HALL)
            if (townHall.canAddWorker(unit.unitType)) {
                colony.addWorkerToBuilding(townHall, unit)
            } else {
                for (building in colony.buildings) {
                    if (building.canAddWorker(unit.unitType)) {
                        colony.addWorkerToBuilding(building, unit)
                        break
                    }
                }
            }
        }

    }
}