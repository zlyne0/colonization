package net.sf.freecol.common.model.colonyproduction

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.Production
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.colonyproduction.ColonyPlan.AssignWorkerToProductionPriorityResult.Assigned
import net.sf.freecol.common.model.colonyproduction.ColonyPlan.AssignWorkerToProductionPriorityResult.LackOfIngredients
import net.sf.freecol.common.model.colonyproduction.ColonyPlan.AssignWorkerToProductionPriorityResult.LackOfLocations
import net.sf.freecol.common.model.player.Market
import net.sf.freecol.common.model.specification.BuildingType
import net.sf.freecol.common.model.specification.GoodsType
import java.util.*

class GoodsMaxProductionLocationWithUnit {
    var score = -1
    lateinit var goodsType: GoodsType
    lateinit var worker: Unit
    var production: Production? = null
    var colonyTile: Tile? = null
    var buildingType: BuildingType? = null

    val ingredientsWorkersAllocation = HashMap<Unit, MaxGoodsProductionLocation>()
    val excludeLocationsIds = HashSet<String>()

    fun resetScore() {
        this.score = -1
    }

    fun reset(goodsType: GoodsType, worker : Unit) {
        this.goodsType = goodsType
        this.worker = worker
        this.score = -1
        this.excludeLocationsIds.clear()
        this.ingredientsWorkersAllocation.clear()
        this.production = null
        this.colonyTile = null
        this.buildingType = null
    }

    override fun toString(): String {
        var st : String
        if (worker != null) {
            st = worker.unitType.id
        } else {
            st = "[no worker]"
        }
        st += ", scrore=$score, "
        if (colonyTile != null) {
            st += colonyTile!!.getId()
        }
        if (buildingType != null) {
            st += buildingType!!.getId()
        }
        return st
    }

    fun hasBetterScore(a : GoodsMaxProductionLocationWithUnit) : Boolean {
        return this.score > a.score
    }

    fun isNotEmpty(): Boolean {
        return score > 0;
    }

    fun scoreForTile(maxProduction: MaxGoodsProductionLocation, market: Market) {
        excludeLocationsIds.add(maxProduction.productionLocationId())
        colonyTile = maxProduction.colonyTile
        production = maxProduction.tileTypeInitProduction

        score = market.getSalePrice(maxProduction.goodsType, maxProduction.production)
    }

    fun scoreForBuilding(buildingType: BuildingType, productionAmount: Int, market: Market) {
        excludeLocationsIds.add(buildingType.id)
        this.buildingType = buildingType

        score = market.getSalePrice(goodsType, productionAmount)
    }
}


class AvailableWorkers(initWorkers: Collection<Unit>) {
    private val availableWorkers = mutableListOf<Unit>()

    init {
        availableWorkers.addAll(initWorkers)
    }

    fun theBestCandidateForProduction(goodsType: GoodsType) : Unit {
        if (availableWorkers.isEmpty()) {
            throw IllegalArgumentException("no available workers")
        }
        val workerProdCal: (Unit) -> Float = { unit ->
            unit.unitType.applyModifier(goodsType.id, 10f)
        }
        return theBest(availableWorkers, workerProdCal)
    }

    fun theBestCandidateForProduction(goodsTypes: List<GoodsType>, withoutUnit: Unit): Unit {
        if (availableWorkers.isEmpty()) {
            throw IllegalArgumentException("no available workers")
        }
        val workerProdCal: (Unit) -> Float = { unit ->
            var workerProd = 0f
            for (goodsType in goodsTypes) {
                workerProd += unit.unitType.applyModifier(goodsType.id, 10f)
            }
            workerProd
        }
        return theBest(availableWorkers.minusElement(withoutUnit), workerProdCal)
    }

    fun theBestCandidateForProduction(goodsTypes: List<GoodsType>) : Unit {
        if (availableWorkers.isEmpty()) {
            throw IllegalArgumentException("no available workers")
        }

        val workerProdCal: (Unit) -> Float = { unit ->
            var workerProd = 0f
            for (goodsType in goodsTypes) {
                workerProd += unit.unitType.applyModifier(goodsType.id, 10f)
            }
            workerProd
        }
        return theBest(availableWorkers, workerProdCal)
    }

    private fun theBest(units: List<Unit>, workerProdCal: (unit: Unit) -> Float): Unit {
        lateinit var theBestUnit: Unit
        var theBestUnitProd: Float = -100f
        for (availableWorker in units) {
            val workerProd = workerProdCal.invoke(availableWorker)
            if (workerProd > theBestUnitProd) {
                theBestUnit = availableWorker
                theBestUnitProd = workerProd
            }
        }
        return theBestUnit
    }

    fun size() = availableWorkers.size
    fun isNotEmpty() = availableWorkers.isNotEmpty()
    fun isEmpty() = availableWorkers.isEmpty()

    fun remove(unit: Unit) {
        availableWorkers.remove(unit)
    }

    fun without(excludeUnit: Unit): AvailableWorkers {
        return AvailableWorkers(availableWorkers.minusElement(excludeUnit))
    }
}

class ColonyPlan(val colony: Colony) {

    class PlanSequence(private val planList: List<Plan>) {
        private var nextPlanIndex: Int = 0

        init {
            if (planList.isEmpty()) {
                throw java.lang.IllegalArgumentException("no plan")
            }
        }

        fun first(): Plan = planList[0]

        fun cyclicNextPlan(): Plan {
            val plan = planList[nextPlanIndex]
            nextPlanIndex++
            if (nextPlanIndex >= planList.size) {
                nextPlanIndex = 0
            }
            return plan
        }

        fun nextPlan() : Plan {
            if (hasNextPlan()) {
                return planList[nextPlanIndex++]
            } else {
                error("no more plans")
            }
        }

        fun hasNextPlan(): Boolean = nextPlanIndex < planList.size
    }

    sealed class Plan(goodsTypesIds: List<String>) {
        class Food : Plan(listOf(GoodsType.GRAIN, GoodsType.FISH))
        class Bell : Plan(listOf(GoodsType.BELLS))
        class Building : Plan(listOf(GoodsType.HAMMERS))
        class MostValuable : Plan(listOf())
        class Tools : Plan(listOf(GoodsType.TOOLS))
        class Muskets : Plan(listOf(GoodsType.MUSKETS))

        companion object {
            fun valueOf(planStr: String) : Plan {
                return when (planStr.lowercase()) {
                    "food" -> Food()
                    "bell" -> Bell()
                    "building" -> Building()
                    "mostvaluable" -> MostValuable()
                    "tools" -> Tools()
                    "muskets" -> Muskets()
                    else -> throw java.lang.IllegalArgumentException("can not recognize plan name $planStr");
                }
            }
        }

        val goodsTypes : List<GoodsType>

        init {
            goodsTypes = ArrayList<GoodsType>(goodsTypesIds.size)
            for (goodsTypesId in goodsTypesIds) {
                goodsTypes.add(Specification.instance.goodsTypes.getById(goodsTypesId))
            }
        }
    }

    enum class AssignWorkerToProductionPriorityResult {
        LackOfIngredients,
        LackOfLocations,
        Assigned
    }

    private val prod = GoodsCollection()
    private val ingredients = GoodsCollection()

    private val market : Market
    private val foodPlan = Plan.Food()
    private val colonySimulationSettingProvider : ColonySimulationSettingProvider
    private val colonyProduction : ColonyProduction
    private val productionSimulation : ProductionSimulation

    private var ignoreIndianOwner = false
    private var minimumProductionLimit = 0

    init {
        market = colony.owner.market()
        colonySimulationSettingProvider = ColonySimulationSettingProvider(colony)
        colonyProduction = ColonyProduction(colonySimulationSettingProvider)
        productionSimulation = colonyProduction.simulation()

        colonySimulationSettingProvider.clearAllProductionLocations()
        colonyProduction.updateRequest()
    }

    fun execute(vararg plan: Plan) {
        createBalancedProductionAllocationPlan(PlanSequence(plan.asList()))
        colonySimulationSettingProvider.putWorkersToColonyViaAllocation();
    }

    fun executeMaximizationProduction(vararg plan: Plan) {
        createMaximizationProductionAllocationPlan(PlanSequence(plan.asList()))
        colonySimulationSettingProvider.putWorkersToColonyViaAllocation();
    }

    /**
     * plan list means that assign one colonist per plan
     */
    private fun createBalancedProductionAllocationPlan(planSequence: PlanSequence) {
        val availableWorkers = AvailableWorkers(colony.settlementWorkers())
        val productionPriority = LinkedList<List<GoodsType>>()
        var actualPlan = planSequence.first()

        var infiniteLoopProtection = 0
        while (availableWorkers.isNotEmpty() && infiniteLoopProtection <= 10) {
            if (productionPriority.isEmpty()) {
                actualPlan = planSequence.cyclicNextPlan()
            }

            val assignResult = assignWorkerToProductionPriority(productionPriority, availableWorkers, actualPlan)
            if (assignResult == Assigned) {
                infiniteLoopProtection = 0
            } else {
                infiniteLoopProtection++
            }
        }
    }

    /**
     * take plan and assign to them colonist to end of place or resources
     */
    private fun createMaximizationProductionAllocationPlan(planSequence: PlanSequence) {
        val availableWorkers = AvailableWorkers(colony.settlementWorkers())
        val productionPriority = LinkedList<List<GoodsType>>()
        var actualPlan = planSequence.first()

        var infiniteLoopProtection = 0
        while (availableWorkers.isNotEmpty() && infiniteLoopProtection <= 10) {

            val assignResult = assignWorkerToProductionPriority(productionPriority, availableWorkers, actualPlan)
            when (assignResult) {
                Assigned -> {
                    infiniteLoopProtection = 0
                }
                LackOfIngredients -> {
                    if (productionPriority.isEmpty() && actualPlan.goodsTypes.isNotEmpty()) {
                        productionPriority.add(actualPlan.goodsTypes)
                    }
                    infiniteLoopProtection++
                }
                LackOfLocations -> {
                    if (planSequence.hasNextPlan()) {
                        actualPlan = planSequence.nextPlan()
                        infiniteLoopProtection++
                    } else {
                        return
                    }
                }
            }
        }
    }

    fun assignWorkerToProductionPriority(
        productionPriority: LinkedList<List<GoodsType>>,
        availableWorkers: AvailableWorkers,
        actualPlan: Plan
    ): AssignWorkerToProductionPriorityResult {

        if (productionPriority.isEmpty()) {
            if (actualPlan is Plan.MostValuable) {
                return assignToMostValuable(availableWorkers, productionPriority)
            }
            productionPriority.add(actualPlan.goodsTypes)
        }

        val productionGoodsTypes = productionPriority.pop()
        val candidate = availableWorkers.theBestCandidateForProduction(productionGoodsTypes)

        if (lackOfIngredients(productionGoodsTypes, candidate.unitType, productionPriority)) {
            // when lack of ingredients modify productionPriority list and try found new worker and location
            return LackOfIngredients
        }

        val maxGoodsProductions = productionSimulation.determinePotentialMaxGoodsProduction(productionGoodsTypes, candidate.unitType, ignoreIndianOwner)
        if (maxGoodsProductions.isEmpty()) {
            return LackOfLocations
        }

        maxGoodsProductions.sortWith(MaxGoodsProductionLocation.quantityComparator)
        val productionLocation = maxGoodsProductions.first()

        if (colonyProduction.canSustainNewWorker(candidate.unitType, productionLocation.goodsType, productionLocation.production)) {
            addWorkerToProductionLocation(candidate, productionLocation, availableWorkers)
            return Assigned
        } else {
            productionPriority.add(foodPlan.goodsTypes)
            return LackOfIngredients
        }
    }

    var max = GoodsMaxProductionLocationWithUnit()
    var maxCandidate = GoodsMaxProductionLocationWithUnit()

    private fun assignToMostValuable(
        availableWorkers: AvailableWorkers,
        productionPriority: LinkedList<List<GoodsType>>
    ): AssignWorkerToProductionPriorityResult {
        if (!colonyProduction.canSustainNewWorker()) {
            productionPriority.add(foodPlan.goodsTypes)
            return LackOfIngredients
        }

        max.resetScore()

        // iterate over goods to find most valuable
        for (goodsType in Specification.instance.goodsTypes) {
            if (!goodsType.isStorable()) {
                continue
            }
            prod.clear()
            ingredients.clear()

            maxCandidate.reset(goodsType, availableWorkers.theBestCandidateForProduction(goodsType))

            if (goodsType.isFarmed()) {
                val maxProduction = productionSimulation.determineMaxProduction(goodsType, maxCandidate.worker.unitType, ignoreIndianOwner)
                if (maxProduction != null && maxProduction.production >= minimumProductionLimit) {
                    if (colony.goodsContainer.hasGoodsQuantity(maxProduction.goodsType, colony.warehouseCapacity())) {
                        continue;
                    }
                    maxCandidate.scoreForTile(maxProduction, market)
                    if (maxCandidate.hasBetterScore(max)) {
                        if (canSustainProduction(maxCandidate, availableWorkers.without(maxCandidate.worker), ingredients)) {
                            val tmp = max
                            max = maxCandidate
                            maxCandidate = tmp
                            // reset maxCandidate on goodsTypes loop
                        }
                    }
                }
            } else {
                val buildingType = colonySimulationSettingProvider.findBuildingType(goodsType, maxCandidate.worker.unitType)
                if (buildingType == null) {
                    continue
                }
                productionSimulation.determineMaxPotentialProduction(buildingType, goodsType, maxCandidate.worker.unitType, prod, ingredients)

                var productionAmount = prod.amount(goodsType)
                productionAmount = colony.maxGoodsAmountToFillWarehouseCapacity(goodsType, productionAmount)
                maxCandidate.scoreForBuilding(buildingType, productionAmount, market)

                if (maxCandidate.hasBetterScore(max)) {
                    if (canSustainProduction(maxCandidate, availableWorkers.without(maxCandidate.worker), ingredients)) {
                        val tmp = max
                        max = maxCandidate
                        maxCandidate = tmp
                        // reset maxCandidate on goodsTypes loop
                    }
                }
            }
        }

        if (max.isNotEmpty()) {
            addWorkerToProductionLocation(max.worker, max, availableWorkers)
            for ((unit, goodMaxProductionLocation) in max.ingredientsWorkersAllocation) {
                addWorkerToProductionLocation(unit, goodMaxProductionLocation, availableWorkers)
            }
            return Assigned
        }
        return LackOfLocations
    }

    private fun canSustainProduction(
        maxCandidate : GoodsMaxProductionLocationWithUnit,
        availableWorkers: AvailableWorkers,
        ingredients : GoodsCollection
    ) : Boolean {

        if (hasGoodsToConsume(ingredients)) {
            if (!colonyProduction.canSustainNewWorker(maxCandidate.worker.unitType)) {
                if (availableWorkers.isEmpty()) {
                    return false
                }
                val foodProductionCandidate = availableWorkers.theBestCandidateForProduction(foodPlan.goodsTypes)
                val foodMaxGoodsProduction = productionSimulation.determineMaxProduction(
                    foodPlan.goodsTypes, foodProductionCandidate.unitType, ignoreIndianOwner, maxCandidate.excludeLocationsIds
                )
                if (foodMaxGoodsProduction == null) {
                    return false;
                }
                maxCandidate.ingredientsWorkersAllocation.put(foodProductionCandidate, foodMaxGoodsProduction)
                return colonyProduction.canSustainNewWorkers(2, foodMaxGoodsProduction.getProduction())
            }
            return true
        }
        if (availableWorkers.isEmpty()) {
            return false
        }

        val ingredient = ingredients.first()
        val worker2 = availableWorkers.theBestCandidateForProduction(ingredient.type())
        val maxGoodsProduction2 = productionSimulation.determineMaxProduction(
            ingredient.type(), worker2.unitType, ignoreIndianOwner, maxCandidate.excludeLocationsIds
        )
        if (maxGoodsProduction2 == null || maxGoodsProduction2.getProduction() < ingredient.amount()) {
            return false
        }
        maxCandidate.ingredientsWorkersAllocation.put(worker2, maxGoodsProduction2)
        maxCandidate.excludeLocationsIds.add(maxGoodsProduction2.productionLocationId())

        // has worker to produce food
        if (colonyProduction.canSustainNewWorkers(2, 0)) {
            return true
        }
        if (availableWorkers.size() - 1 <= 0) {
            return false
        }
        val availableWorkers2 = availableWorkers.without(worker2)
        val foodProductionCandidate = availableWorkers2.theBestCandidateForProduction(foodPlan.goodsTypes)
        val foodMaxGoodsProduction = productionSimulation.determineMaxProduction(
            foodPlan.goodsTypes, foodProductionCandidate.unitType, ignoreIndianOwner, maxCandidate.excludeLocationsIds
        )
        if (foodMaxGoodsProduction == null) {
            return false
        }
        maxCandidate.ingredientsWorkersAllocation.put(foodProductionCandidate, foodMaxGoodsProduction)
        maxCandidate.excludeLocationsIds.add(foodMaxGoodsProduction.productionLocationId())
        return colonyProduction.canSustainNewWorkers(3, foodMaxGoodsProduction.getProduction());
    }

    private fun addWorkerToProductionLocation(worker: Unit, productionLocation: GoodsMaxProductionLocationWithUnit, availableWorkers: AvailableWorkers) {
        availableWorkers.remove(worker)
        if (productionLocation.colonyTile != null) {
            colonySimulationSettingProvider.addWorker(productionLocation.colonyTile, worker, productionLocation.production)
        }
        if (productionLocation.buildingType != null) {
            colonySimulationSettingProvider.addWorker(productionLocation.buildingType, worker)
        }
        colonyProduction.updateRequest()
    }

    private fun addWorkerToProductionLocation(worker: Unit, productionLocation: MaxGoodsProductionLocation, availableWorkers: AvailableWorkers) {
        availableWorkers.remove(worker)
        if (productionLocation.colonyTile != null) {
            colonySimulationSettingProvider.addWorker(productionLocation.colonyTile, worker, productionLocation.tileTypeInitProduction)
        }
        if (productionLocation.buildingType != null) {
            colonySimulationSettingProvider.addWorker(productionLocation.buildingType, worker)
        }
        colonyProduction.updateRequest()
    }

    private fun lackOfIngredients(goodsToProduce: List<GoodsType>, workerType: UnitType, productionPriority: LinkedList<List<GoodsType>>) : Boolean {
        prod.clear()
        ingredients.clear()

        for (goodsType in goodsToProduce) {
            productionSimulation.determineMaxPotentialProduction(goodsType, workerType, prod, ingredients);
        }
        var lack = false
        for (ingredient in ingredients) {
            if (!hasGoodsToConsume(ingredient)) {
                lack = true
                productionPriority.add(listOf(ingredient.type()))
            }
        }
        return lack
    }

    private fun hasGoodsToConsume(goods : GoodsEntry): Boolean {
        var available = colonyProduction.globalProductionConsumption().getQuantity(goods.type())
        available += colonySimulationSettingProvider.warehouse().amount(goods.type())
        return goods.amount() * 0.5 <= available
    }

    private fun hasGoodsToConsume(goods: GoodsCollection): Boolean {
        for (goodsEntry in goods) {
            var available = colonyProduction.globalProductionConsumption().getQuantity(goodsEntry.type())
            available += colonySimulationSettingProvider.warehouse().amount(goodsEntry.type())
            if (goodsEntry.amount() * 0.5 > available) {
                return false
            }
        }
        return true
    }

    fun withConsumeWarehouseResources(consumeWarehouseResources: Boolean): ColonyPlan {
        if (consumeWarehouseResources) {
            colonySimulationSettingProvider.withConsumeWarehouseResources()
        }
        // else default behavior to not consume warehouse resources
        return this
    }

    fun withIgnoreIndianOwner(): ColonyPlan {
        ignoreIndianOwner = true
        return this
    }

    fun withMinimumProductionLimit(limit: Int): ColonyPlan {
        minimumProductionLimit = limit
        return this
    }
}