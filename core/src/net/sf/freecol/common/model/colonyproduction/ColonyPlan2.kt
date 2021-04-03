package net.sf.freecol.common.model.colonyproduction

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.ProductionSummary
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.specification.GoodsType
import java.util.*
import kotlin.collections.ArrayList


class ColonyPlan2(colony: Colony) {

    class PlanSequence(private val planList: List<Plan>) {
        private var nextPlanIndex : Int = 0

        init {
            if (planList.isEmpty()) {
                throw java.lang.IllegalArgumentException("no plan")
            }
        }

        fun nextPlan() : Plan {
            var plan = planList[nextPlanIndex]
            nextPlanIndex++
            if (nextPlanIndex >= planList.size) {
                nextPlanIndex = 0
            }
            return plan
        }
    }

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

    fun execute(vararg plan: Plan) {
        createAllocationPlan(PlanSequence(plan.asList()))

        colonySimulationSettingProvider.putWorkersToColonyViaAllocation();
    }

    private fun createAllocationPlan(planSequence: PlanSequence) {
        val productionPriority = LinkedList<List<GoodsType>>()

        var infiniteLoopProtection = 0
        while (availableWorkers.isNotEmpty()) {
            if (infiniteLoopProtection > 10) {
                break
            }

            if (productionPriority.isEmpty()) {
                val plan = planSequence.nextPlan()
                productionPriority.add(plan.goodsTypes)
            }

            val productionGoodsTypes = productionPriority.pop()
            val candidate = theBestCandidateForProduction(productionGoodsTypes)

            if (lackOfIngredients(productionGoodsTypes, candidate.unitType, productionPriority)) {
                // when lack of ingredients modify productionPriority list and try found new worker and location
                infiniteLoopProtection++
                continue
            }

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

    private val prod = ProductionSummary()
    private val ingredients = ProductionSummary()

    private fun lackOfIngredients(goodsToProduce: List<GoodsType>, workerType: UnitType, productionPriority: LinkedList<List<GoodsType>>) : Boolean {
        prod.clear()
        ingredients.clear()

        for (goodsType in goodsToProduce) {
            productionSimulation.determineMaxPotentialProduction(goodsType.id, workerType, prod, ingredients);
        }
        var lack = false
        for (ingredient in ingredients.entries()) {
            if (!hasGoodsToConsume(ingredient.key, ingredient.value)) {
                lack = true
                productionPriority.add(listOf(Specification.instance.goodsTypes.getById(ingredient.key)))
            }
        }
        return lack
    }

    private fun hasGoodsToConsume(goodsTypeId: String, amount: Int): Boolean {
        var available = colonyProduction.globalProductionConsumption().getQuantity(goodsTypeId)
        if (consumeWarehouseResources) {
            available += colonySimulationSettingProvider.warehouse().amount(goodsTypeId)
        }
        return amount * 0.5 <= available
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