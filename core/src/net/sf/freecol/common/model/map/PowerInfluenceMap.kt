package net.sf.freecol.common.model.map

import net.sf.freecol.common.model.Map
import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.ai.MapTileDebugInfo
import promitech.map.FloatFloatArray
import promitech.map.IntIntArray
import promitech.map.forEach

class PowerInfluenceMap(
    private val map: Map,
    private val tileFilter: (Tile) -> Boolean,
    private val influenceRangeMapBuilder: InfluenceRangeMapBuilder
) {

    // sum power for all sources
    private val sumSourcePower = FloatFloatArray(map.width, map.height, FLOAT_UNKNOWN_VALUE)
    // range from source
    private val influencePowerMap = IntIntArray(map.width, map.height, INT_UNKNOWN_VALUE)

    fun addSourceLayer(tile: Tile, startPowerProjection: Int, power: Float) {
        influenceRangeMapBuilder.init(map, tileFilter)
        influenceRangeMapBuilder.addPowerSource(tile, startPowerProjection)
        influenceRangeMapBuilder.generateSpreadPower()

        val rangeMap = influenceRangeMapBuilder.getRangeMap()
        rangeMap.forEach { x, y, rangeMapValue ->
            sumSourcePower.addValue(x, y, power)
            influencePowerMap.addValue(x, y, rangeMapValue)
        }
    }

    fun reset() {
        sumSourcePower.resetToUnknown()
        influencePowerMap.resetToUnknown()
    }

    fun powerSum(tile: Tile): Float = sumSourcePower.get(tile.x, tile.y)
    fun powerSum(x: Int, y: Int): Float = sumSourcePower.get(x, y)
    fun powerProjectionRange(tile: Tile): Int = influencePowerMap.get(tile.x, tile.y)
    fun powerProjectionRange(x: Int, y: Int): Int = influencePowerMap.get(x, y)

    fun printInfluencePower(mapDebugInfo: MapTileDebugInfo) {
        influencePowerMap.forEach { x, y, powerValue ->
            mapDebugInfo.str(x, y, "${sumSourcePower.get(x, y)}/$powerValue")
        }
    }

    fun isUnknownValue(v: Float): Boolean {
        return v == FLOAT_UNKNOWN_VALUE
    }

    companion object {
        val FLOAT_UNKNOWN_VALUE = Float.MIN_VALUE
        val INT_UNKNOWN_VALUE = Integer.MIN_VALUE
    }

}