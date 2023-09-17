package net.sf.freecol.common.model.map

import com.badlogic.gdx.utils.Array
import net.sf.freecol.common.model.Map
import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.forEachNeighbourTile
import promitech.map.IntIntArray

/**
 * Influence range map with values from initialRange to maxRange
 */
class InfluenceRangeMapBuilder {

    private var rangeUnknownMapValue = Int.MAX_VALUE
    private var spreadPowerUnknownMapValue = Int.MIN_VALUE

    private var tileFilter: (Tile) -> Boolean = ALLOW_ALL_TILES_FILTER
    private lateinit var map: Map
    private lateinit var tilePool: Array<Tile>
    private var rangeMapHolder: IntIntArray? = null
    private val tilePowerStartValues = com.badlogic.gdx.utils.IntArray()

    fun init(map: Map, tileFilter: (Tile) -> Boolean = ALLOW_ALL_TILES_FILTER) {
        this.map = map
        this.tileFilter = tileFilter

        if (!this::tilePool.isInitialized || tilePool.size != map.width * map.height) {
            tilePool = Array<Tile>(false, map.width * map.height)
        }
        tilePool.clear()
        tilePowerStartValues.clear()
    }

    fun addRangeSource(tile: Tile) {
        tilePool.add(tile)
    }

    fun addRangeSource(sourceTiles: List<Tile>) {
        for (tile in sourceTiles) {
            tilePool.add(tile)
        }
    }

    fun generateRange(maxRange: Int, initialRangeValue: Int = 0): IntIntArray {
        updateRangeMapHolder(rangeUnknownMapValue)

        for (tile in tilePool) {
            rangeMapHolder!!.set(tile.x, tile.y, initialRangeValue)
        }
        internalGenerateRange(maxRange, rangeMapHolder!!)
        return rangeMapHolder!!
    }

    fun generateRange(rangeMap: IntIntArray, maxRange: Int, initialRangeValue: Int = 0) {
        rangeMap.set(rangeUnknownMapValue)
        for (tile in tilePool) {
            rangeMap.set(tile.x, tile.y, initialRangeValue)
        }
        internalGenerateRange(maxRange, rangeMap)
    }

    private fun internalGenerateRange(maxRange: Int, rangeMap: IntIntArray) {
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

    fun addPowerSource(tile: Tile, startPowerProjection: Int) {
        tilePool.add(tile)
        tilePowerStartValues.add(startPowerProjection)
    }

    fun generateSpreadPower(): IntIntArray {
        updateRangeMapHolder(spreadPowerUnknownMapValue)

        if (tilePool.size != tilePowerStartValues.size) {
            throw java.lang.IllegalArgumentException("tilePool size (${tilePool.size}) different to tilePowerStartValues (${tilePowerStartValues.size})")
        }
        tilePool.forEachIndexed { index, tile ->
            rangeMapHolder!!.set(tile.x, tile.y, tilePowerStartValues.get(index))
        }
        internalSpreadPower(rangeMapHolder!!)
        return rangeMapHolder!!
    }

    private fun updateRangeMapHolder(unknownValue: Int) {
        if (rangeMapHolder == null || rangeMapHolder!!.width != map.width || rangeMapHolder!!.height != map.height) {
            rangeMapHolder = IntIntArray(map.width, map.height, unknownValue)
        } else {
            rangeMapHolder!!.set(spreadPowerUnknownMapValue)
        }
    }

    private fun internalSpreadPower(rangeMap: IntIntArray) {
        var range: Int
        var tmpTile: Tile
        while (tilePool.size != 0) {
            tmpTile = tilePool.removeIndex(0)
            range = rangeMap.get(tmpTile.x, tmpTile.y)
            range--
            if (range <= 0) {
                continue
            }
            map.forEachNeighbourTile(tmpTile) { nTile ->
                if (tileFilter(nTile) && rangeMap.get(nTile.x, nTile.y) < range) {
                    rangeMap.set(nTile.x, nTile.y, range)
                    tilePool.add(nTile)
                }
            }
        }
    }

    fun range(tile: Tile): Int {
        return rangeMapHolder!!.get(tile.x, tile.y)
    }

    fun getRangeMap(): IntIntArray {
        if (rangeMapHolder == null) {
            throw java.lang.IllegalStateException("rangeMapHolder not initialized")
        }
        return rangeMapHolder!!
    }

    companion object {
        val ALLOW_ALL_TILES_FILTER: (Tile) -> Boolean = { _ -> true }
    }
}