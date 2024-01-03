package net.sf.freecol.common.model.ai.missions.pioneer

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.ColonyTile
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.TileImprovementType
import net.sf.freecol.common.model.TileType
import net.sf.freecol.common.model.ai.MapTileDebugInfo
import net.sf.freecol.common.model.player.Player
import net.sf.freecol.common.model.specification.GoodsType

class TileImprovementPlan(val tile: Tile, val improvementType: TileImprovementType)

class ColonyTilesImprovementPlan(val colony: Colony, val improvements: MutableList<TileImprovementPlan>) {
    fun hasImprovements(): Boolean {
        return improvements.size > 0
    }

    fun isEmpty(): Boolean {
        return improvements.isEmpty()
    }

    fun firstImprovement(): TileImprovementPlan {
        return improvements.first()
    }

    fun printToMap(mapTileDebugInfo: MapTileDebugInfo) {
        for (plan in improvements) {
            mapTileDebugInfo.str(plan.tile.x, plan.tile.y, plan.improvementType.toSmallIdStr())
        }
    }
}

sealed class AddImprovementPolicy {

    // balanced, leave forest resources
    class Balanced(player: Player) : AddImprovementPolicy() {

        private val centerTilesImprovementRecommendations = CenterTilesImprovementRecommendations(player)

        override fun addImprovement(colony: Colony, colonyTile: ColonyTile, imprList: MutableList<TileImprovementPlan>) {
            val tile = colonyTile.tile
            if (tile.type.isForested) {
                if (tile.hasTileResource() || tile.hasRiver()) {
                    addIfAbsent(imprList, tile, roadType)
                } else {
//                    if (colony.colonyUnitsCount <= 4 && hasClearNotPlowedTile || forestNumber == 1) {
//                        addIfAbsent(imprList, tile, roadType)
//                    } else {
//                        addIfAbsent(imprList, tile, clearForestType)
//                    }

                    if (colonyTile.isCenterColonyTile()) {
                        if (canClearForestOnCenterTile(colony, tile.type)) {
                            addIfAbsent(imprList, tile, clearForestType)
                        }
                    } else {
                        if (hasNoForestNotWorkingTile) {
                            addIfAbsent(imprList, tile, roadType)
                        } else {
                            if (colonyTile.hasFoodProduction()) {
                                addIfAbsent(imprList, tile, clearForestType)
                            }
                        }
                    }
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

        private fun canClearForestOnCenterTile(colony: Colony, colonyCenterTileType: TileType): Boolean {
            val newTileType = tileTypeAfterImprovement(colonyCenterTileType, clearForestType)
            if (newTileType != null) {
                val prod = newTileType.productionInfo.findFirstNotFoodUnattendedProduction()
                val actualProd = colonyCenterTileType.productionInfo.findFirstNotFoodUnattendedProduction()
                if (prod != null && actualProd != null) {
                    val transformationRecommendation = centerTilesImprovementRecommendations.recommend(colony, actualProd.key.id, prod.key.id)
                    return transformationRecommendation != CenterTilesImprovementRecommendations.BenefitResult.NO
                }
            }
            return false
        }

        private fun tileTypeAfterImprovement(tileType: TileType, improvementType: TileImprovementType): TileType? {
            if (tileType.isTileImprovementAllowed(improvementType)) {
                val tileTypeTransformation = improvementType.changedTileType(tileType)
                if (tileTypeTransformation != null) {
                    return tileTypeTransformation.toType
                }
            }
            return null
        }

    }

    // improvements for max food production, clear forest resources
    class MaxFood : AddImprovementPolicy() {
        override fun addImprovement(colony: Colony, colonyTile: ColonyTile, imprList: MutableList<TileImprovementPlan>) {
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

    protected var hasNoForestNotWorkingTile = false

    init {
        plowedType = Specification.instance.tileImprovementTypes.getById(TileImprovementType.PLOWED_IMPROVEMENT_TYPE_ID)
        roadType = Specification.instance.tileImprovementTypes.getById(TileImprovementType.ROAD_MODEL_IMPROVEMENT_TYPE_ID)
        clearForestType = Specification.instance.tileImprovementTypes.getById(TileImprovementType.CLEAR_FOREST_IMPROVEMENT_TYPE_ID)
    }

    protected abstract fun addImprovement(colony: Colony, colonyTile: ColonyTile, imprList: MutableList<TileImprovementPlan>)

    fun generateImprovements(colony: Colony): ColonyTilesImprovementPlan {
        val imprList = mutableListOf<TileImprovementPlan>()

        hasNoForestNotWorkingTile = calculateHasNoForestNotWorkingTile(colony)

        findCenterTile(colony) { colonyTile ->
            addImprovement(colony, colonyTile, imprList)
        }
        findResourcesTiles(colony) { colonyTile ->
            addImprovement(colony, colonyTile, imprList)
        }
        findCommonTiles(colony) { colonyTile ->
            addImprovement(colony, colonyTile, imprList)
        }
        return ColonyTilesImprovementPlan(colony, imprList)
    }

    fun generateVacantForFood(plan: ColonyTilesImprovementPlan) {
        findVacantForFood(plan.colony, plan.improvements)
    }

    private inline fun findCenterTile(colony: Colony, consumer: (colonyTile: ColonyTile) -> Unit) {
        consumer(colony.colonyTiles.getById(colony.tile))
    }

    private inline fun findResourcesTiles(colony: Colony, consumer: (colonyTile: ColonyTile) -> Unit) {
        for (colonyTile in colony.colonyTiles) {
            if (colonyTile.tile.type.isWater
                || colonyTile.tile.hasSettlement()
                || colonyTile.tile.equalsCoordinates(colony.tile)) {
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
                || colonyTile.tile.hasSettlement()
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
                || colonyTile.tile.hasSettlement()
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

    protected fun calculateClearNotPlowedTile(colony: Colony): Boolean {
        for (colonyTile in colony.colonyTiles) {
            if (colonyTile.tile.type.isWater
                || colonyTile.tile.hasSettlement()
                || colonyTile.tile.equalsCoordinates(colony.tile)) {
                continue
            }
            if (!colonyTile.tile.type.isForested && !colonyTile.tile.hasImprovementType(plowedType.id)) {
                return true
            }
        }
        return false
    }

    protected fun calculateHasNoForestNotWorkingTile(colony: Colony): Boolean {
        for (colonyTile in colony.colonyTiles) {
            if (colonyTile.tile.type.isWater || colonyTile.tile.hasSettlement() || colonyTile.isCenterColonyTile) {
                continue
            }
            if (!colonyTile.tile.type.isForested && colonyTile.hasNotWorker()) {
                return true
            }
        }
        return false
    }

    protected fun calculateForestNumber(colony: Colony): Int {
        var forestNumber = 0
        for (colonyTile in colony.colonyTiles) {
            if (colonyTile.tile.type.isWater
                || colonyTile.tile.hasSettlement()
                || colonyTile.tile.equalsCoordinates(colony.tile)
            ) {
                continue
            }
            if (colonyTile.tile.type.isForested) {
                forestNumber++
            }
        }
        return forestNumber
    }

}