package promitech.colonization.ai

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.ProductionSummary
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.colonyproduction.ColonyPlan
import net.sf.freecol.common.model.player.Player
import net.sf.freecol.common.model.specification.BuildingType
import net.sf.freecol.common.model.specification.GoodsType
import promitech.colonization.ai.score.ObjectScoreList

class ColonyBuildingPlaner {

    private val mostValuableGoods: Set<GoodsType>
    private val hammersGoodsType: GoodsType

    init {
        mostValuableGoods = HashSet()
        mostValuableGoods.addAll(ColonyPlan.Plan.RawMaterials.goodsTypes)
        mostValuableGoods.addAll(ColonyPlan.Plan.ProcessedMaterials.goodsTypes)

        hammersGoodsType = Specification.instance.goodsTypes.getById(GoodsType.HAMMERS)
    }

    fun productionValueBuildingPlan(player: Player, colony: Colony): ObjectScoreList<BuildingType> {

        val productionSummary = colony.productionSummary()
        val colonyGoldProductionValue = goldValue(productionSummary, player)

        val buildingTypeScore = ObjectScoreList<BuildingType>(10)

        generateColonyBuildableBuildings(colony)
            .filter { buildingType -> buildingProduceMostValuableGoods(buildingType) }
            .filter { buildingType -> isBuildingTypeProduceTypeOnColonyProduction(buildingType, productionSummary) }
            .forEach { buildingType ->

                val virtualColony = ColonyPlan(colony).withConsumeWarehouseResources(true)
                virtualColony.addBuilding(buildingType)
                virtualColony.execute2(ColonyPlan.Plan.MostValuable, ColonyPlan.Plan.Bell, ColonyPlan.Plan.Food)
                val virtualProductionConsumption = virtualColony.productionConsumption()
                val virtualGold = goldValue(virtualProductionConsumption, player)

                val gain = virtualGold - colonyGoldProductionValue

                val priceForBuilding = colony.getAIPriceForBuilding(buildingType)
                println("$buildingType actual: $colonyGoldProductionValue, upgrade: $virtualGold, gain: $gain, priceForBuilding: $priceForBuilding")

                if (gain > 0) {
                    buildingTypeScore.add(buildingType, gain)
                }
            }
        return buildingTypeScore
    }

    fun buildingValue(player: Player, colony: Colony) {
        val virtualColony = ColonyPlan(colony).withConsumeWarehouseResources(true)

        virtualColony.execute2(ColonyPlan.Plan.Building, ColonyPlan.Plan.Food)
        val hammersProductionConsumption = virtualColony.productionConsumption()
        val hammersGoldValue = hammersProductionGoldValue(hammersProductionConsumption, player)

        virtualColony.execute2(ColonyPlan.Plan.MostValuable, ColonyPlan.Plan.Food)
        val mostValuableProductionConsumption = virtualColony.productionConsumption()
        val mostValuableGoodsGoldValue = goldValue(mostValuableProductionConsumption, player)


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

    private fun goldValue(productionSummary: ProductionSummary, player: Player): Int {
        var goldSumValue = 0
        for (entry in productionSummary.entries()) {
            goldSumValue += player.market().getSalePrice(entry.key, entry.value)
        }
        return goldSumValue
    }

    private fun hammersProductionGoldValue(productionSummary: ProductionSummary, player: Player): Int {
        return player.market().buildingGoodsPrice(
            hammersGoodsType,
            productionSummary.getQuantity(hammersGoodsType)
        )
    }

    private fun generateColonyBuildableBuildings(colony: Colony): Sequence<BuildingType> {
        return Specification.instance.buildingTypes.asSequence()
            .filter { buildingType -> colony.getNoBuildReason(buildingType) == Colony.NoBuildReason.NONE }
            .filter { buildingType -> !colony.isBuildingAlreadyBuilt(buildingType) }
    }

}