package net.sf.freecol.common.model.map

import com.badlogic.gdx.utils.Array
import net.sf.freecol.common.model.Map
import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.ai.MapTileDebugInfo
import promitech.colonization.Direction
import promitech.map.Int2dArray
import promitech.map.IntIntArray

class LandSeaAreaMap(private val map: Map) {

    private val areas = IntIntArray(map.width, map.height)

    private val notTouched = 0
    private var areaCounter = notTouched

    fun generate() {
        val poolIndex = Array<Tile>(false, map.width * map.height)
        for (y in 0 .. map.height - 1) {
            for (x in 0 .. map.width - 1) {
                val tile = map.getSafeTile(x, y)
                if (isAreaNotCreated(tile)) {
                    createArea(poolIndex, tile)
                }
            }
        }
    }

    private fun createArea(poolIndex: Array<Tile>, tile: Tile) {
        areaCounter++
        areas.set(tile.x, tile.y, areaCounter)
        poolIndex.add(tile)

        var tmpTile: Tile
        var nTile: Tile?
        while (poolIndex.size != 0) {
            tmpTile = poolIndex.removeIndex(0)

            for (i in 0 .. Direction.values().size-1) {
                val nDirection = Direction.values()[i]
                nTile = map.getTile(tmpTile, nDirection)
                if (nTile == null) {
                    continue
                }
                if (nTile.type.isLand == tile.type.isLand && isAreaNotCreated(nTile)) {
                    areas.set(nTile.x, nTile.y, areaCounter)
                    poolIndex.add(nTile)
                }
            }
        }
    }

    private fun isAreaNotCreated(tile: Tile) : Boolean {
        return areas.get(tile.x, tile.y) == notTouched
    }

    fun printTo(mapDebugInfo: MapTileDebugInfo) {
        for (y in 0 .. areas.height - 1) {
            for (x in 0 .. areas.width - 1) {
                mapDebugInfo.str(x, y, areas.get(x, y).toString())
            }
        }
    }

    fun isTheSameArea(tile1: Tile, tile2: Tile): Boolean {
        return areas.get(tile1.x, tile1.y) == areas.get(tile2.x, tile2.y)
    }
}