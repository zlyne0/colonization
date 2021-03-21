package net.sf.freecol.common.model.colonyproduction

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.specification.GoodsType
import java.util.*
import kotlin.collections.ArrayList


class ColonyPlan2(colony: Colony) {

    sealed class Plan(goodsTypesIds: List<String>) {
        class Food() : Plan(listOf(GoodsType.GRAIN, GoodsType.FISH))
        class Bell() : Plan(listOf(GoodsType.BELLS))
        class Building() : Plan(listOf(GoodsType.HAMMERS))
        class MostValuable() : Plan(listOf())
        class Tools() : Plan(listOf(GoodsType.TOOLS))
        class Muskets() : Plan(listOf(GoodsType.MUSKETS))

        val goodsTypes : List<GoodsType>

        init {
            goodsTypes = ArrayList<GoodsType>(goodsTypesIds.size)
            for (goodsTypesId in goodsTypesIds) {
                goodsTypes.add(Specification.instance.goodsTypes.getById(goodsTypesId))
            }
        }
    }

    private val foodPlan = Plan.Food()
    private val colonySimulationSettingProvider : ColonySimulationSettingProvider
    private val colonyProduction : ColonyProduction
    private val productionSimulation : ProductionSimulation
    private val availableWorkers = mutableListOf<Unit>()

    private var ignoreIndianOwner = false
    private var consumeWarehouseResources = false

    init {
        colonySimulationSettingProvider = ColonySimulationSettingProvider(colony)
        colonyProduction = ColonyProduction(colonySimulationSettingProvider)
        productionSimulation = colonyProduction.simulation()

        colonySimulationSettingProvider.clearAllProductionLocations()
        colonyProduction.updateRequest()

        availableWorkers.addAll(colony.settlementWorkers())
    }

    fun execute(plan: Plan) {
        val productionPriority = LinkedList<List<GoodsType>>()

        var infiniteLoopProtection = 0
        while (availableWorkers.isNotEmpty()) {
            if (infiniteLoopProtection > 10) {
                return
            }

            if (productionPriority.isEmpty()) {
                productionPriority.add(plan.goodsTypes)
            }

            val productionGoodsTypes = productionPriority.pop()
            val candidate = theBestCandidateForProduction(productionGoodsTypes)
            val maxGoodsProductions = productionSimulation.determinePotentialMaxGoodsProduction(productionGoodsTypes, candidate.unitType, ignoreIndianOwner)
            if (maxGoodsProductions.isEmpty()) {
                infiniteLoopProtection++;
                continue
            }

            maxGoodsProductions.sortWith(MaxGoodsProductionLocation.quantityComparator)
            val productionLocation = maxGoodsProductions.first()

            if (colonyProduction.canSustainNewWorker(candidate.unitType, productionLocation.goodsType, productionLocation.production)) {
                addWorkerToProductionLocation(candidate, productionLocation)
                infiniteLoopProtection = 0
            } else {
                productionPriority.add(foodPlan.goodsTypes)
            }
        }
        colonySimulationSettingProvider.putWorkersToColonyViaAllocation();
    }

    private fun addWorkerToProductionLocation(worker: Unit, productionLocation: MaxGoodsProductionLocation) {
        availableWorkers.remove(worker)
        colonySimulationSettingProvider.addWorker(worker, productionLocation)
        colonyProduction.updateRequest()
    }

    private fun theBestCandidateForProduction(goodsTypes: List<GoodsType>) : Unit {
        if (availableWorkers.isEmpty()) {
            throw IllegalArgumentException("no available workers")
        }

        lateinit var theBestUnit : Unit
        var theBestUnitProd : Float = -100f
        for (availableWorker in availableWorkers) {
            var workerProd = 0f
            for (goodsType in goodsTypes) {
                workerProd += availableWorker.unitType.applyModifier(goodsType.id, 10f)
            }
            if (workerProd > theBestUnitProd) {
                theBestUnit = availableWorker
                theBestUnitProd = workerProd
            }
        }
        return theBestUnit
    }

    fun withConsumeWarehouseResources(consumeWarehouseResources: Boolean): ColonyPlan2 {
        this.consumeWarehouseResources = consumeWarehouseResources
        return this
    }

    fun withIgnoreIndianOwner(): ColonyPlan2 {
        ignoreIndianOwner = true
        return this;
    }
}