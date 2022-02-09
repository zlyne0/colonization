package net.sf.freecol.common.model.ai.missions.pionier

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.ColonyTile
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.TileImprovementType
import net.sf.freecol.common.model.ai.MapTileDebugInfo
import net.sf.freecol.common.model.specification.GoodsType

class TileImprovementPlan(val tile: Tile, val improvementType: TileImprovementType)
class ColonyTilesImprovementPlan(val colony: Colony, val improvements: List<TileImprovementPlan>) {
    fun hasImprovements(): Boolean {
        return improvements.size > 0
    }
}

sealed class AddImprovementPolicy {

    // balanced, leave forest resources
    class Balanced : AddImprovementPolicy() {
        override fun addImprovement(colonyTile: ColonyTile, imprList: MutableList<TileImprovementPlan>) {
            val tile = colonyTile.tile
            if (tile.type.isForested) {
                if (tile.hasTileResource() || tile.hasRiver()) {
                    addIfAbsent(imprList, tile, roadType)
                } else {
                    addIfAbsent(imprList, tile, clearForestType)
                }
            } else {
                if (tile.type.isTileImprovementAllowed(plowedType)) {
                    addIfAbsent(imprList, tile, plowedType)
                } else {
                    // maybe mountain
                    addIfAbsent(imprList, tile, roadType)
                }
            }
        }
    }

    // improvements for max food production, clear forest resources
    class MaxFood : AddImprovementPolicy() {
        override fun addImprovement(colonyTile: ColonyTile, imprList: MutableList<TileImprovementPlan>) {
            val tile = colonyTile.tile
            if (tile.type.isForested) {
                addIfAbsent(imprList, tile, clearForestType)
            } else {
                if (tile.type.isTileImprovementAllowed(plowedType)) {
                    addIfAbsent(imprList, tile, plowedType)
                } else {
                    // maybe mountain
                    addIfAbsent(imprList, tile, roadType)
                }
            }
        }
    }

    protected val plowedType : TileImprovementType
    protected val roadType : TileImprovementType
    protected val clearForestType : TileImprovementType

    init {
        plowedType = Specification.instance.tileImprovementTypes.getById(TileImprovementType.PLOWED_IMPROVEMENT_TYPE_ID)
        roadType = Specification.instance.tileImprovementTypes.getById(TileImprovementType.ROAD_MODEL_IMPROVEMENT_TYPE_ID)
        clearForestType = Specification.instance.tileImprovementTypes.getById(TileImprovementType.CLEAR_FOREST_IMPROVEMENT_TYPE_ID)
    }

    protected abstract fun addImprovement(colonyTile: ColonyTile, imprList: MutableList<TileImprovementPlan>)

    fun generateImprovements(colony: Colony): ColonyTilesImprovementPlan {
        val imprList = mutableListOf<TileImprovementPlan>()

        findCenterTile(colony) { colonyTile ->
            addImprovement(colonyTile, imprList)
        }
        findResourcesTiles(colony) { colonyTile ->
            addImprovement(colonyTile, imprList)
        }
        findCommonTiles(colony) { colonyTile ->
            addImprovement(colonyTile, imprList)
        }
        findVacantForFood(colony, imprList)

        return ColonyTilesImprovementPlan(colony, imprList)
    }

    private inline fun findCenterTile(colony: Colony, consumer: (colonyTile: ColonyTile) -> Unit) {
        consumer(colony.colonyTiles.getById(colony.tile))
    }

    private inline fun findResourcesTiles(colony: Colony, consumer: (colonyTile: ColonyTile) -> Unit) {
        for (colonyTile in colony.colonyTiles) {
            if (colonyTile.tile.type.isWater || colonyTile.tile.equalsCoordinates(colony.tile)) {
                continue
            }
            if (colonyTile.tile.hasTileResource() && colonyTile.hasWorker()) {
                consumer(colonyTile)
            }
        }
    }

    private inline fun findCommonTiles(colony: Colony, consumer: (colonyTile: ColonyTile) -> Unit) {
        for (colonyTile in colony.colonyTiles) {
            if (colonyTile.tile.type.isWater
                || colonyTile.tile.equalsCoordinates(colony.tile)
                || colonyTile.tile.hasTileResource()) {
                continue
            }
            if (colonyTile.hasWorker()) {
                consumer(colonyTile)
            }
        }
    }

    private fun findVacantForFood(colony: Colony, imprList: MutableList<TileImprovementPlan>) {
        var firstTile: Tile? = null
        var firstTileImpr: TileImprovementType = roadType
        var maxGrainProduction: Int = 0

        for (colonyTile in colony.colonyTiles) {
            if (colonyTile.tile.type.isWater
                || colonyTile.tile.equalsCoordinates(colony.tile)
                || colonyTile.hasWorker()) {
                continue
            }
            if (colonyTile.tile.type.isForested) {
                if (!colonyTile.tile.hasTileResource()) {
                    val grainProduction = colonyTile.tile.goodsProduction(GoodsType.GRAIN)
                    if (firstTile == null || grainProduction > maxGrainProduction) {
                        firstTile = colonyTile.tile
                        firstTileImpr = clearForestType
                        maxGrainProduction = grainProduction
                    }
                }
            } else {
                if (!colonyTile.tile.hasImprovementType(plowedType.id)) {
                    val grainProduction = colonyTile.tile.goodsProduction(GoodsType.GRAIN)
                    if (firstTile == null || grainProduction > maxGrainProduction) {
                        firstTile = colonyTile.tile
                        firstTileImpr = plowedType
                        maxGrainProduction = grainProduction
                    }
                } else {
                    return
                }
            }
        }
        if (firstTile != null) {
            imprList.add(TileImprovementPlan(firstTile, firstTileImpr))
        }
    }

    protected fun addIfAbsent(imprList: MutableList<TileImprovementPlan>, tile: Tile, imprType: TileImprovementType) {
        if (!tile.hasImprovementType(imprType.id)) {
            imprList.add(TileImprovementPlan(tile, imprType))
        }
    }

    fun printToMap(mapTileDebugInfo: MapTileDebugInfo, improvementPlan: ColonyTilesImprovementPlan) {
        for (plan in improvementPlan.improvements) {
            mapTileDebugInfo.str(plan.tile.x, plan.tile.y, plan.improvementType.toSmallIdStr())
        }
    }
}