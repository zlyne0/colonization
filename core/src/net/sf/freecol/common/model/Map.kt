package net.sf.freecol.common.model

import promitech.colonization.Direction
import promitech.colonization.SpiralIterator

inline fun Map.forEachTile(action: (x: Int, y: Int) -> kotlin.Unit) {
    for (y: Int in 0 until this.height) {
        for (x: Int in 0 until this.width) {
            action.invoke(x, y)
        }
    }
}

inline fun Map.forEachTile(action: (tile: Tile) -> kotlin.Unit) {
    for (y: Int in 0 until this.height) {
        for (x: Int in 0 until this.width) {
            action.invoke(this.getSafeTile(x, y))
        }
    }
}

inline fun Map.forEachTileCoordinatesInRange(sourceTile: Tile, radius: Int, action: (Int, Int) -> kotlin.Unit) {
    val spiral = SpiralIterator(this.width, this.height)
    spiral.reset(sourceTile.x, sourceTile.y, true, radius)
    while (spiral.hasNext()) {
        action(spiral.x, spiral.y)
        spiral.next()
    }
}

inline fun Map.forEachTileInRange(sourceTile: Tile, radius: Int, action: (Tile) -> kotlin.Unit) {
    if (radius == 1) {
        for (direction in Direction.allDirections) {
            val neighbourTile: Tile? = getTile(sourceTile, direction!!)
            if (neighbourTile != null) {
                action(neighbourTile)
            }
        }
    } else {
        val spiral = SpiralIterator(this.width, this.height)
        spiral.reset(sourceTile.x, sourceTile.y, true, radius)
        while (spiral.hasNext()) {
            action(this.getSafeTile(spiral.x, spiral.y))
            spiral.next()
        }
    }
}
