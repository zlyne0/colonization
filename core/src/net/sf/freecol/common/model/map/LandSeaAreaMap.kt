package net.sf.freecol.common.model.map

import com.badlogic.gdx.utils.Array
import net.sf.freecol.common.model.Map
import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.ai.MapTileDebugInfo
import net.sf.freecol.common.model.forEachTile
import promitech.colonization.Direction
import promitech.map.IntIntArray
import promitech.map.forEachCords

class LandSeaAreaMap(private val width: Int, private val height: Int) {

    private val areas = IntIntArray(width, height)

    private val notTouched = 0
    private var areaCounter = notTouched

    fun generate(map: Map) {
        val poolIndex = Array<Tile>(false, map.width * map.height)
        map.forEachTile { tile ->
            if (isAreaNotCreated(tile)) {
                createArea(poolIndex, tile, map)
            }
        }
    }

    private fun createArea(poolIndex: Array<Tile>, tile: Tile, map: Map) {
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
        areas.forEachCords { x, y ->
            mapDebugInfo.str(x, y, areas.get(x, y).toString())
        }
    }

    fun isTheSameArea(tile1: Tile, tile2: Tile): Boolean {
        return areas.get(tile1.x, tile1.y) == areas.get(tile2.x, tile2.y)
    }
}