package net.sf.freecol.common.model.map

import com.badlogic.gdx.utils.Array
import net.sf.freecol.common.model.Map
import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.ai.MapTileDebugInfo
import net.sf.freecol.common.model.forEachNeighbourTile
import promitech.map.IntIntArray
import promitech.map.forEach

class InfluenceRangeMap(
    private val map: Map,
    private val maxRange: Int,
    private val tileFilter: (Tile) -> Boolean = ALLOW_ALL_TILES_FILTER
) {

    companion object {
        val ALLOW_ALL_TILES_FILTER: (Tile) -> Boolean = { _ -> true }
    }

    private val initialRange = 0
    private val maxValue = Int.MAX_VALUE

    private val rangeMap = IntIntArray(map.width, map.height)
    private val tilePool = Array<Tile>(false, rangeMap.size())


    init {
        rangeMap.set(maxValue)
        tilePool.clear()
    }

    fun generate(sourceTiles: List<Tile>): IntIntArray {
        for (sourceTile in sourceTiles) {
            rangeMap.set(sourceTile.x, sourceTile.y, initialRange)
            tilePool.add(sourceTile)
        }
        expand()
        return rangeMap
    }

    fun addSource(tile: Tile) {
        rangeMap.set(tile.x, tile.y, initialRange)
        tilePool.add(tile)
    }

    fun generate() {
        expand()
    }

    private fun expand() {
        var range: Int
        var tmpTile: Tile
        while (tilePool.size != 0) {
            tmpTile = tilePool.removeIndex(0)
            range = rangeMap.get(tmpTile.x, tmpTile.y)
            if (range >= maxRange) {
                continue
            }
            range++
            map.forEachNeighbourTile(tmpTile) { nTile ->
                if (tileFilter(nTile) && rangeMap.get(nTile.x, nTile.y) > range) {
                    rangeMap.set(nTile.x, nTile.y, range)
                    tilePool.add(nTile)
                }
            }
        }
    }

    fun range(x: Int, y: Int): Int {
        return rangeMap.get(x, y)
    }

    fun range(tile: Tile): Int {
        return rangeMap.get(tile.x, tile.y)
    }

    fun printTo(mapDebugInfo: MapTileDebugInfo) {
        rangeMap.forEach { x, y, value ->
            if (value != maxValue) {
                mapDebugInfo.str(x, y, value.toString())
            }
        }
    }
}