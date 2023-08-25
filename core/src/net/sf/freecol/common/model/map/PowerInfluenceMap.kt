package net.sf.freecol.common.model.map

import com.badlogic.gdx.utils.Array
import net.sf.freecol.common.model.Map
import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.ai.MapTileDebugInfo
import net.sf.freecol.common.model.forEachNeighbourTile
import promitech.map.IntIntArray
import promitech.map.forEach

class PowerInfluenceMap(
    private val map: Map,
    private val decreaseValue: Int = 1,
    private val tileFilter: (Tile) -> Boolean = InfluenceRangeMap.ALLOW_ALL_TILES_FILTER
) {
    val resetValue = Integer.MIN_VALUE

    private val powerMap = IntIntArray(map.width, map.height)
    private val rangeMap = IntIntArray(map.width, map.height)
    private val tilePool = Array<Tile>(false, rangeMap.size())

    init {
        powerMap.set(resetValue)
    }

    fun addSource(tile: Tile, startValue: Int) {
        rangeMap.set(resetValue)
        tilePool.clear()

        rangeMap.set(tile.x, tile.y, startValue)
        tilePool.add(tile)

        generate()
    }

    private fun generate() {
        expand()
        increasePowerMap()
    }

    private fun expand() {
        var range: Int
        var tmpTile: Tile
        while (tilePool.size != 0) {
            tmpTile = tilePool.removeIndex(0)
            range = rangeMap.get(tmpTile.x, tmpTile.y)
            if (range <= 0) {
                continue
            }
            range -= decreaseValue
            map.forEachNeighbourTile(tmpTile) { nTile ->
                if (tileFilter(nTile) && rangeMap.get(nTile.x, nTile.y) < range) {
                    rangeMap.set(nTile.x, nTile.y, range)
                    tilePool.add(nTile)
                }
            }
        }
    }

    private fun increasePowerMap() {
        var powerValue: Int
        rangeMap.forEach { x, y, value ->
            if (value != resetValue) {
                powerValue = powerMap.get(x, y)
                if (powerValue == resetValue) {
                    powerValue = 0
                }
                powerMap.set(x, y, value + powerValue)
            }
        }
    }

    fun power(x: Int, y: Int): Int {
        return powerMap.get(x, y)
    }

    fun power(tile: Tile): Int {
        return powerMap.get(tile.x, tile.y)
    }

    fun printTo(mapDebugInfo: MapTileDebugInfo) {
        powerMap.forEach { x, y, value ->
            if (value != resetValue) {
                mapDebugInfo.str(x, y, value.toString())
            }
        }
    }
}