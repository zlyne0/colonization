package net.sf.freecol.common.model.ai.missions.workerrequest

import com.badlogic.gdx.utils.IntMap
import net.sf.freecol.common.model.Tile
import java.util.Collections
import java.util.Comparator


class TheBestColonyTiles(
    private val maxTilesPerTurn: Int = 3
) {

    class Pair(var tile: Tile, var weight: Int)

    private val pairComparator = Comparator<Pair> { p1, p2 -> p2.weight - p1.weight }
    private val placesPerTurnDistance = IntMap<ArrayList<Pair>>()

    var minTurns = Int.MAX_VALUE
        private set
    var maxTurns = 0
        private set

    fun add(tile: Tile, turns: Int, weight: Int) {
        if (turns < minTurns) {
            minTurns = turns
        }
        if (turns > maxTurns) {
            maxTurns = turns
        }

        var placesTurn = placesPerTurnDistance.get(turns)
        if (placesTurn == null) {
            placesTurn = ArrayList()
            placesPerTurnDistance.put(turns, placesTurn)
            placesTurn.add(Pair(tile, weight))
        } else {
            if (placesTurn.size >= maxTilesPerTurn) {
                val last = placesTurn.last()
                if (weight > last.weight) {
                    last.tile = tile
                    last.weight = weight
                }
            } else {
                placesTurn.add(Pair(tile, weight))
            }
            placesTurn.sortWith(pairComparator)
        }
    }

    fun firstTheBest(): Tile? {
        val list = placesPerTurnDistance.get(minTurns)
        if (list != null && list.isNotEmpty()) {
            return list[0].tile
        }
        return null
    }

    fun tilesInTurns(turns: Int): List<Pair> {
        val placesTurn = placesPerTurnDistance.get(turns)
        if (placesTurn == null) {
            return Collections.emptyList()
        }
        return placesTurn
    }

    fun isEmpty(): Boolean = placesPerTurnDistance.isEmpty
}