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

    private val singleGoodsTypeHolder = mutableListOf<GoodsType>()

    fun add(unit: Unit) {
        availableWorkers.add(unit)
    }

    fun theBestCandidateForProduction(goodsType: GoodsType, colonySettingProvider: ColonySettingProvider): Unit {
        if (availableWorkers.isEmpty()) {
            throw IllegalArgumentException("no available workers")
        }
        singleGoodsTypeHolder.clear()
        singleGoodsTypeHolder.add(goodsType)
        return theBest(availableWorkers, colonySettingProvider, singleGoodsTypeHolder)
    }

    fun theBestCandidateForProduction(goodsTypes: List<GoodsType>, colonySettingProvider: ColonySettingProvider) : Unit {
        if (availableWorkers.isEmpty()) {
            throw IllegalArgumentException("no available workers")
        }
        return theBest(availableWorkers, colonySettingProvider, goodsTypes)
    }

    private fun theBest(units: List<Unit>, colonySettingProvider: ColonySettingProvider, goodsTypes: List<GoodsType>): Unit {
        lateinit var theBestUnit: Unit
        var theBestUnitProd: Float = -100f
        for (availableWorker in units) {
            val workerProd = workerProdCal(availableWorker, goodsTypes)
            if (workerProd > theBestUnitProd) {
                theBestUnit = availableWorker
                theBestUnitProd = workerProd
            }
        }

        for (tile: ColonyTileProduction in colonySettingProvider.tiles()) {
            if (tile.worker != null && tile.worker.unit != null) {
                if (tile.isExpertAndWorkingInItsProfession) {
                    continue
                }
                val worker = tile.worker
                val workerProd = workerProdCal(worker.unit, goodsTypes)
                if (workerProd > theBestUnitProd) {
                    theBestUnit = worker.unit
                    theBestUnitProd = workerProd
                }
            }
        }

        for (building: BuildingProduction in colonySettingProvider.buildings()) {
            for (worker in building.workers) {
                if (worker.unit != null) {
                    if (building.isExpertAndWorkingInItsProfession(worker.unit)) {
                        continue
                    }
                    val workerProd = workerProdCal(worker.unit, goodsTypes)
                    if (workerProd > theBestUnitProd) {
                        theBestUnit = worker.unit
                        theBestUnitProd = workerProd
                    }
                }
            }
        }
        return theBestUnit
    }

    private fun workerProdCal(unit: Unit, goodsTypes: List<GoodsType>): Float {
        var workerProd = 0f
        for (goodsType in goodsTypes) {
            workerProd += unit.unitType.applyModifier(goodsType.id, 10f)
        }
        return workerProd
    }

    fun size() = availableWorkers.size
    fun isNotEmpty() = availableWorkers.isNotEmpty()
    fun isEmpty() = availableWorkers.isEmpty()

    fun remove(unit: Unit, colonySettingProvider: ColonySettingProvider) {
        if (availableWorkers.contains(unit)) {
            availableWorkers.remove(unit)
        } else {
            for (worker in colonySettingProvider.workers()) {
                if (worker.unit != null && worker.unit.equals(unit)) {
                    val firstVacant = availableWorkers.removeFirst()
                    worker.unit = firstVacant
                    worker.unitType = firstVacant.unitType
                    break
                }
            }
        }
    }

    fun without(excludeUnit: Unit): AvailableWorkers {
        val aw = AvailableWorkers()
        for (availableWorker in this.availableWorkers) {
            if (!availableWorker.equalsId(excludeUnit)) {
                aw.availableWorkers.add(availableWorker)
            }
        }
        return aw
    }
}