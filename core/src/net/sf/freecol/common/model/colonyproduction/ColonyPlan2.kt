package net.sf.freecol.common.model.colonyproduction

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.GoodMaxProductionLocation
import net.sf.freecol.common.model.ProductionSummary
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.player.Market
import net.sf.freecol.common.model.specification.GoodsType
import java.util.HashMap
import java.util.LinkedList
import kotlin.collections.ArrayList

class GoodsMaxProductionLocationWithUnit {
    var score = -1
    lateinit var worker: Unit
    lateinit var productionLocation: MaxGoodsProductionLocation
//    private var goodsType: GoodsType? = null
//    private var location: ProductionLocation? = null
    val ingredientsWorkersAllocation: MutableMap<Unit, MaxGoodsProductionLocation> = HashMap()

    fun update(newScore: Int, worker: Unit,
       productionLocation: MaxGoodsProductionLocation,
       ingredientsWorkersAllocation: HashMap<Unit, MaxGoodsProductionLocation>
    ) {
        if (newScore > this.score) {
            this.score = newScore
            this.worker = worker
            this.productionLocation = productionLocation
            ingredientsWorkersAllocation.clear()
            ingredientsWorkersAllocation.putAll(ingredientsWorkersAllocation)
        }
    }

    override fun toString(): String {
        var st = ""
        if (worker != null) {
            st = worker!!.unitType.id
        } else {
            st = "[no worker]"
        }
        st += ", scrore=$score, " + productionLocation
        return st
    }

    fun hasBetterNewScore(newScore: Int): Boolean {
        return newScore > score
    }

    fun isNotEmpty(): Boolean {
        return score > 0;
    }

}


class AvailableWorkers(initWorkers: Collection<Unit>) {
    val availableWorkers = mutableListOf<Unit>()

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

    inline fun size() : Int {
        return availableWorkers.size
    }

    inline fun isNotEmpty(): Boolean {
        return availableWorkers.isNotEmpty()
    }

    inline fun remove(unit: Unit) {
        availableWorkers.remove(unit)
    }

    inline fun isEmptyWithout(worker: Unit): Boolean {
        return availableWorkers.contains(worker) && availableWorkers.size == 1
    }
}

class ColonyPlan2(val colony: Colony) {

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

        companion object {
            fun valueOf(planStr: String) : Plan {
                when (planStr.toLowerCase()) {
                    "food" -> return Food()
                    "bell" -> return Bell()
                    "building" -> return Building()
                    "mostvaluable" -> return MostValuable()
                    "tools" -> return Tools()
                    "Muskets" -> return Muskets()
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

    private val market : Market
    private val foodPlan = Plan.Food()
    private val colonySimulationSettingProvider : ColonySimulationSettingProvider
    private val colonyProduction : ColonyProduction
    private val productionSimulation : ProductionSimulation

    private var ignoreIndianOwner = false
    private var consumeWarehouseResources = false

    init {
        market = colony.owner.market()
        colonySimulationSettingProvider = ColonySimulationSettingProvider(colony)
        colonyProduction = ColonyProduction(colonySimulationSettingProvider)
        productionSimulation = colonyProduction.simulation()

        colonySimulationSettingProvider.clearAllProductionLocations()
        colonyProduction.updateRequest()
    }

    fun execute(vararg plan: Plan) {
        if (consumeWarehouseResources) {
            colonySimulationSettingProvider.withConsumeWarehouseResources()
        }
        createAllocationPlan(PlanSequence(plan.asList()))
        colonySimulationSettingProvider.putWorkersToColonyViaAllocation();
    }

    private fun createAllocationPlan(planSequence: PlanSequence) {
        val availableWorkers = AvailableWorkers(colony.settlementWorkers())

        // -first- implemented scenario (BalancedProduction)
        // plan list means that assign one colonist per plan
        // -second- not implemented scenario (MaximizationProduction)
        // take plan and assign to them colonist to end of place or resources

        val productionPriority = LinkedList<List<GoodsType>>()

        var infiniteLoopProtection = 0
        while (availableWorkers.isNotEmpty()) {
            if (infiniteLoopProtection > 10) {
                break
            }

            if (productionPriority.isEmpty()) {
                val plan = planSequence.nextPlan()
                if (plan is Plan.MostValuable) {
                    var assignedWorker = assignToMostValuable(availableWorkers, productionPriority)
                    if (assignedWorker) {
                        infiniteLoopProtection = 0
                    } else {
                        infiniteLoopProtection++
                    }
                    continue
                }
                productionPriority.add(plan.goodsTypes)
            }

            val productionGoodsTypes = productionPriority.pop()
            val candidate = availableWorkers.theBestCandidateForProduction(productionGoodsTypes)

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
                addWorkerToProductionLocation(candidate, productionLocation, availableWorkers)
                infiniteLoopProtection = 0
            } else {
                productionPriority.add(foodPlan.goodsTypes)
            }
        }
    }

    private fun assignToMostValuable(availableWorkers: AvailableWorkers, productionPriority: LinkedList<List<GoodsType>>): Boolean {
        var max = GoodsMaxProductionLocationWithUnit()
        val ingredientsWorkersAllocation = HashMap<Unit, MaxGoodsProductionLocation>()

        if (!colonyProduction.canSustainNewWorker()) {
            productionPriority.add(foodPlan.goodsTypes)
            return false
        }

        // iterate over goods to find most valuable
        for (goodsType in Specification.instance.goodsTypes) {
            if (!goodsType.isStorable()) {
                continue
            }
            var candidate = availableWorkers.theBestCandidateForProduction(goodsType)
            if (goodsType.isFarmed()) {
                val maxProduction = productionSimulation.determineMaxProduction(goodsType, candidate.unitType, ignoreIndianOwner)
                if (maxProduction != null) {
                    val score : Int = market.getSalePrice(maxProduction.goodsType, maxProduction.production)
                    if (max.hasBetterNewScore(score)) {
                        if (canSustainProduction(maxProduction, candidate, availableWorkers, ingredientsWorkersAllocation)) {
                            max.update(score, candidate, maxProduction, ingredientsWorkersAllocation)
                        }
                    }
                }
            } else {
                println("XX try determine production for " + goodsType + " " + colonySimulationSettingProvider.findBuildingType(goodsType, candidate.unitType))
            }
        }

        if (max.isNotEmpty()) {
            addWorkerToProductionLocation(max.worker, max.productionLocation, availableWorkers)
            for ((unit, goodMaxProductionLocation) in max.ingredientsWorkersAllocation) {
                addWorkerToProductionLocation(unit, goodMaxProductionLocation, availableWorkers)
            }
        }
        return max.isNotEmpty()
    }

    private fun canSustainProduction(
        maxProduction: MaxGoodsProductionLocation,
        worker: Unit,
        availableWorkers: AvailableWorkers,
        ingredientsWorkersAllocation: HashMap<Unit, MaxGoodsProductionLocation>
    ) : Boolean {
        if (!colonyProduction.canSustainNewWorker(worker.unitType)) {
            if (availableWorkers.isEmptyWithout(worker)) {
                return false
            }
            val foodProductionCandidate = availableWorkers.theBestCandidateForProduction(foodPlan.goodsTypes)
            val excludeLocationsIds = HashSet<String>()
            excludeLocationsIds.add(maxProduction.productionLocationId())
            val foodMaxGoodsProduction = productionSimulation.determineMaxProduction(
                foodPlan.goodsTypes, foodProductionCandidate.unitType, ignoreIndianOwner, excludeLocationsIds
            )
            if (foodMaxGoodsProduction == null) {
                return false;
            }
            ingredientsWorkersAllocation.put(foodProductionCandidate, foodMaxGoodsProduction)
            return colonyProduction.canSustainNewWorkers(2, foodMaxGoodsProduction.getProduction())
        }
        return true;
    }

    private fun addWorkerToProductionLocation(worker: Unit, productionLocation: MaxGoodsProductionLocation, availableWorkers: AvailableWorkers) {
        availableWorkers.remove(worker)
        colonySimulationSettingProvider.addWorker(worker, productionLocation)
        colonyProduction.updateRequest()
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

    fun createPlanProductionChain(plan: Plan) : List<GoodsType> {
        val chain = mutableListOf<GoodsType>()
        for (goodsType in plan.goodsTypes) {
            chain.add(goodsType)

            var gt = goodsType
            while (gt.madeFrom != null) {
                gt = gt.madeFrom
                chain.add(0, gt)
            }
        }
        return chain
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