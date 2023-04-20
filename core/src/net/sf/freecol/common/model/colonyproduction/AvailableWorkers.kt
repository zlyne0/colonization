package net.sf.freecol.common.model.colonyproduction

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.specification.GoodsType

class AvailableWorkers() {
    private val availableWorkers = mutableListOf<Unit>()

    companion object {
        fun createFrom(colony: Colony): AvailableWorkers {
            val aw = AvailableWorkers()
            aw.availableWorkers.addAll(colony.settlementWorkers())

            colony.tile.units.entities()
                .asSequence()
                .filter { unit -> unit.state == Unit.UnitState.IN_COLONY }
                .forEach { unit -> aw.availableWorkers.add(unit) }
            aw.sortExpertLast()
            return aw
        }
    }

    fun add(unit: Unit) {
        availableWorkers.add(unit)
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

    fun sortExpertLast() {
        // because theBest method take first unit with best production
        // and to avoid take experts to not their work
        availableWorkers.sortWith(Unit.EXPERTS_LAST_COMPARATOR)
    }

    fun size() = availableWorkers.size
    fun isNotEmpty() = availableWorkers.isNotEmpty()
    fun isEmpty() = availableWorkers.isEmpty()

    fun remove(unit: Unit) {
        availableWorkers.remove(unit)
    }

    fun without(excludeUnit: Unit): AvailableWorkers {
        val aw = AvailableWorkers()
        aw.availableWorkers.addAll(this.availableWorkers.minusElement(excludeUnit))
        return aw
    }
}