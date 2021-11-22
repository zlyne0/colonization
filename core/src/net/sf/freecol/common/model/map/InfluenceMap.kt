package net.sf.freecol.common.model.map

import com.badlogic.gdx.utils.Array
import net.sf.freecol.common.model.Map
import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.ai.MapTileDebugInfo
import promitech.colonization.Direction
import promitech.map.IntIntArray

class InfluenceMap(
    private val map: Map,
    private val maxRange: Int = Int.MAX_VALUE
) {
    private val initialRange = 0
    private val maxValue = Int.MAX_VALUE

    private val rangeMap = IntIntArray(map.width, map.height)

    fun generate(sourceTiles: List<Tile>): IntIntArray {
        rangeMap.set(maxValue)
        val tilePool = Array<Tile>(false, rangeMap.size())
        for (tile in sourceTiles) {
            expand(tile, tilePool)
        }
        return rangeMap
    }

    private fun expand(sourceTile: Tile, tilePool: Array<Tile>) {
        rangeMap.set(sourceTile.x, sourceTile.y, initialRange)
        tilePool.add(sourceTile)

        var range: Int
        var tmpTile: Tile
        var nTile: Tile?
        while (tilePool.size != 0) {
            tmpTile = tilePool.removeIndex(0)
            range = rangeMap.get(tmpTile.x, tmpTile.y)
            if (range >= maxRange) {
                continue
            }
            range++
            for (i in 0..Direction.values().size - 1) {
                val nDirection = Direction.values()[i]
                nTile = map.getTile(tmpTile, nDirection)
                if (nTile == null) {
                    continue
                }
                if (rangeMap.get(nTile.x, nTile.y) > range) {
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
        var value: Int
        for (y in 0 .. rangeMap.height - 1) {
            for (x in 0 .. rangeMap.width - 1) {
                value = rangeMap.get(x, y)
                if (value != maxValue) {
                    mapDebugInfo.str(x, y, value.toString())
                }
            }
        }
    }
}