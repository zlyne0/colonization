package net.sf.freecol.common.model.ai.missions.workerrequest

import net.sf.freecol.common.model.Map
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.map.path.PathFinder
import net.sf.freecol.common.model.player.Player

class EntryPointTurnRange(val map: Map, val pathFinder: PathFinder, player: Player, transportUnit: Unit? = null) {
    init {
        if (transportUnit == null) {
            pathFinder.generateRangeMap(
                map,
                map.getSafeTile(player.entryLocation),
                pathFinder.createPathUnit(player, Specification.instance.unitTypes.getById(UnitType.CARAVEL)),
                PathFinder.includeUnexploredTiles
            )
        } else {
            pathFinder.generateRangeMap(map, map.getSafeTile(player.entryLocation), transportUnit, PathFinder.includeUnexploredTiles)
        }
    }

    fun turnsCost(cellIndex : Int): Int {
        return pathFinder.turnsCost(cellIndex)
    }

    fun trunsCost(tile: Tile): Int {
        return pathFinder.turnsCost(tile)
    }

}