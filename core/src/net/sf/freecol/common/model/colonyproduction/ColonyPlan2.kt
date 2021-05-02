package net.sf.freecol.common.model.colonyproduction

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.Production
import net.sf.freecol.common.model.ProductionSummary
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.player.Market
import net.sf.freecol.common.model.specification.BuildingType
import net.sf.freecol.common.model.specification.GoodsType
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.Collection
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.asList
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.first
import kotlin.collections.isNotEmpty
import kotlin.collections.iterator
import kotlin.collections.listOf
import kotlin.collections.minusElement
import kotlin.collections.mutableListOf
import kotlin.collections.sortWith

class GoodsMaxProductionLocationWithUnit {
    var score = -1
    lateinit var worker: Unit
    lateinit var goodsType: GoodsType
    var production: Production? = null
    var colonyTile: Tile? = null
    var buildingType: BuildingType? = null

    val ingredientsWorkersAllocation: MutableMap<Unit, MaxGoodsProductionLocation> = HashMap()

    fun update(newScore: Int, worker: Unit,
        goodsType: GoodsType,
        colonyTile : Tile,
        production : Production,
        ingredientsWorkersAllocation: HashMap<Unit, MaxGoodsProductionLocation>
    ) {
        if (newScore > this.score) {
            this.score = newScore
            this.worker = worker
            this.goodsType = goodsType
            this.colonyTile = colonyTile
            this.buildingType = null
            this.production = production
            this.ingredientsWorkersAllocation.clear()
            this.ingredientsWorkersAllocation.putAll(ingredientsWorkersAllocation)
        }
    }

    fun update(newScore: Int, worker: Unit,
        goodsType: GoodsType,
        buildingType: BuildingType,
        ingredientsWorkersAllocation: HashMap<Unit, MaxGoodsProductionLocation>
    ) {
        if (newScore > this.score) {
            this.score = newScore
            this.worker = worker
            this.goodsType = goodsType
            this.colonyTile = null
            this.production = null
            this.buildingType = buildingType
            this.ingredientsWorkersAllocation.clear()
            this.ingredientsWorkersAllocation.putAll(ingredientsWorkersAllocation)
        }
    }


    override fun toString(): String {
        var st = ""
        if (worker != null) {
            st = worker!!.unitType.id
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

    inline fun isEmpty(): Boolean {
        return availableWorkers.isEmpty()
    }

    inline fun remove(unit: Unit) {
        availableWorkers.remove(unit)
    }

    fun without(excludeUnit: Unit): AvailableWorkers {
        return AvailableWorkers(availableWorkers.minusElement(excludeUnit))
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

        private var chain : MutableList<GoodsType>? = null
        val goodsTypes : List<GoodsType>

        init {
            goodsTypes = ArrayList<GoodsType>(goodsTypesIds.size)
            for (goodsTypesId in goodsTypesIds) {
                goodsTypes.add(Specification.instance.goodsTypes.getById(goodsTypesId))
            }
        }

        fun productionChain() : List<GoodsType> {
            if (this.chain != null) {
                return this.chain!!
            }
            var goodsList = mutableListOf<GoodsType>()
            for (goodsType in goodsTypes) {
                goodsList.add(goodsType)

                var gt = goodsType
                while (gt.madeFrom != null) {
                    gt = gt.madeFrom
                    goodsList.add(0, gt)
                }
            }
            this.chain = goodsList
            return goodsList
        }
    }

    private val productionChainForGoodsType = HashMap<GoodsType, List<GoodsType>>()

    private val market : Market
    private val foodPlan = Plan.Food()
    private val colonySimulationSettingProvider : ColonySimulationSettingProvider
    private val colonyProduction : ColonyProduction
    private val productionSimulation : ProductionSimulation

    private var ignoreIndianOwner = false

    init {
        market = colony.owner.market()
        colonySimulationSettingProvider = ColonySimulationSettingProvider(colony)
        colonyProduction = ColonyProduction(colonySimulationSettingProvider)
        productionSimulation = colonyProduction.simulation()

        colonySimulationSettingProvider.clearAllProductionLocations()
        colonyProduction.updateRequest()
    }

    fun execute(vararg plan: Plan) {
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
        if (!colonyProduction.canSustainNewWorker()) {
            productionPriority.add(foodPlan.goodsTypes)
            return false
        }

        var max = GoodsMaxProductionLocationWithUnit()
        val ingredientsWorkersAllocation = HashMap<Unit, MaxGoodsProductionLocation>()
        val excludeLocationsIds = HashSet<String>()

        // iterate over goods to find most valuable
        for (goodsType in Specification.instance.goodsTypes) {
            if (!goodsType.isStorable()) {
                continue
            }
            excludeLocationsIds.clear()
            prod.clear()
            ingredients.clear()

            var candidate = availableWorkers.theBestCandidateForProduction(goodsType)
            if (goodsType.isFarmed()) {
                val maxProduction = productionSimulation.determineMaxProduction(goodsType, candidate.unitType, ignoreIndianOwner)
                if (maxProduction != null) {
                    excludeLocationsIds.add(maxProduction.productionLocationId())

                    val score : Int = market.getSalePrice(maxProduction.goodsType, maxProduction.production)
                    if (max.hasBetterNewScore(score)) {
                        if (canSustainProduction(candidate, availableWorkers.without(candidate), ingredients, ingredientsWorkersAllocation, excludeLocationsIds)) {
                            max.update(score, candidate,
                                maxProduction.goodsType,
                                maxProduction.colonyTile,
                                maxProduction.tileTypeInitProduction,
                                ingredientsWorkersAllocation
                            )
                        }
                    }
                }
            } else {
                val buildingType = colonySimulationSettingProvider.findBuildingType(goodsType, candidate.unitType)
                if (buildingType == null) {
                    continue
                }

                productionSimulation.determineMaxPotentialProduction(buildingType, goodsType, candidate.unitType, prod, ingredients)

                var productionQuantity = prod.amount(goodsType)
                productionQuantity = colony.maxGoodsAmountToFillWarehouseCapacity(goodsType, productionQuantity)
                val score : Int = market.getSalePrice(goodsType, productionQuantity)

                if (score > 0 && max.hasBetterNewScore(score)) {
                    if (canSustainProduction(candidate, availableWorkers.without(candidate), ingredients, ingredientsWorkersAllocation, excludeLocationsIds)) {
                        max.update(score, candidate, goodsType, buildingType, ingredientsWorkersAllocation)
                    }
                }
            }
        }

        if (max.isNotEmpty()) {
            addWorkerToProductionLocation(max.worker, max, availableWorkers)
            for ((unit, goodMaxProductionLocation) in max.ingredientsWorkersAllocation) {
                addWorkerToProductionLocation(unit, goodMaxProductionLocation, availableWorkers)
            }
        }
        return max.isNotEmpty()
    }

    private fun canSustainProduction(
        worker: Unit,
        availableWorkers: AvailableWorkers,
        ingredients : GoodsCollection,
        ingredientsWorkersAllocation: HashMap<Unit, MaxGoodsProductionLocation>,
        excludeLocationsIds : HashSet<String>
    ) : Boolean {
        ingredientsWorkersAllocation.clear()

        if (hasGoodsToConsume(ingredients)) {
            if (!colonyProduction.canSustainNewWorker(worker.unitType)) {
                if (availableWorkers.isEmpty()) {
                    return false
                }
                val foodProductionCandidate = availableWorkers.theBestCandidateForProduction(foodPlan.goodsTypes)
                val foodMaxGoodsProduction = productionSimulation.determineMaxProduction(
                    foodPlan.goodsTypes, foodProductionCandidate.unitType, ignoreIndianOwner, excludeLocationsIds
                )
                if (foodMaxGoodsProduction == null) {
                    return false;
                }
                ingredientsWorkersAllocation.put(foodProductionCandidate, foodMaxGoodsProduction)
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
            listOf(ingredient.type()), worker2.unitType, ignoreIndianOwner, excludeLocationsIds
        )
        if (maxGoodsProduction2 == null || maxGoodsProduction2.getProduction() < ingredient.amount()) {
            return false
        }
        ingredientsWorkersAllocation.put(worker2, maxGoodsProduction2)
        excludeLocationsIds.add(maxGoodsProduction2.productionLocationId())

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
            foodPlan.goodsTypes, foodProductionCandidate.unitType, ignoreIndianOwner, excludeLocationsIds
        )
        if (foodMaxGoodsProduction == null) {
            return false
        }
        ingredientsWorkersAllocation.put(foodProductionCandidate, foodMaxGoodsProduction)
        excludeLocationsIds.add(foodMaxGoodsProduction.productionLocationId())
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


    private val prod = GoodsCollection()
    private val ingredients = GoodsCollection()

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

    fun productionChain(goodsType: GoodsType) : List<GoodsType> {
        var chain = productionChainForGoodsType.get(goodsType)
        if (chain != null) {
            return chain
        }
        chain = mutableListOf<GoodsType>()
        chain.add(goodsType)

        var gt = goodsType
        while (gt.madeFrom != null) {
            gt = gt.madeFrom
            chain.add(0, gt)
        }
        productionChainForGoodsType.put(goodsType, chain)
        return chain
    }

    fun withConsumeWarehouseResources(consumeWarehouseResources: Boolean): ColonyPlan2 {
        if (consumeWarehouseResources) {
            colonySimulationSettingProvider.withConsumeWarehouseResources()
        } else {
            // default behavior to not consume warehouse resources
        }
        return this
    }

    fun withIgnoreIndianOwner(): ColonyPlan2 {
        ignoreIndianOwner = true
        return this;
    }
}