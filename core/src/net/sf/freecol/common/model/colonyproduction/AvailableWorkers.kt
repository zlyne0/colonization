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
            return aw
        }
    }

    fun add(unit: Unit) {
        availableWorkers.add(unit)
    }

    fun theBestCandidateForProduction(goodsType: GoodsType, colonySettingProvider: ColonySettingProvider): Unit {
        if (availableWorkers.isEmpty()) {
            throw IllegalArgumentException("no available workers")
        }
        val workerProdCal: (Unit) -> Float = { unit ->
            unit.unitType.applyModifier(goodsType.id, 10f)
        }
        return theBest(availableWorkers, colonySettingProvider, workerProdCal)
    }

    fun theBestCandidateForProduction(goodsTypes: List<GoodsType>, withoutUnit: Unit, colonySettingProvider: ColonySettingProvider): Unit {
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
        return theBest(availableWorkers.minusElement(withoutUnit), colonySettingProvider, workerProdCal)
    }

    fun theBestCandidateForProduction(goodsTypes: List<GoodsType>, colonySettingProvider: ColonySettingProvider) : Unit {
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
        return theBest(availableWorkers, colonySettingProvider, workerProdCal)
    }

    private fun theBest(units: List<Unit>, colonySettingProvider: ColonySettingProvider, workerProdCal: (unit: Unit) -> Float): Unit {
        lateinit var theBestUnit: Unit
        var theBestUnitProd: Float = -100f
        for (availableWorker in units) {
            val workerProd = workerProdCal(availableWorker)
            if (workerProd > theBestUnitProd) {
                theBestUnit = availableWorker
                theBestUnitProd = workerProd
            }
        }

        var workerPlaceInColony: Worker? = null
        for (worker in colonySettingProvider.workers()) {
            if (worker.unit != null) {
                val workerProd = workerProdCal(worker.unit)
                if (workerProd > theBestUnitProd) {
                    theBestUnit = worker.unit
                    theBestUnitProd = workerProd
                    workerPlaceInColony = worker
                }
            }
        }
        if (workerPlaceInColony != null) {
            val firstToReplace = availableWorkers.removeFirst()
            workerPlaceInColony.unit = firstToReplace
            workerPlaceInColony.unitType = firstToReplace.unitType
            availableWorkers.add(theBestUnit)
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
        val aw = AvailableWorkers()
        aw.availableWorkers.addAll(this.availableWorkers.minusElement(excludeUnit))
        return aw
    }
}