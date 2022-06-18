package promitech.colonization.ai

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.Map
import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.player.Player

fun Player.generateCivilizationSources(map: Map): List<Tile> {
    val sourceTiles = mutableListOf<Tile>()
    sourceTiles.add(map.getSafeTile(entryLocation))
    for (settlement in settlements) {
        sourceTiles.add(settlement.tile)
    }
    return sourceTiles
}

fun Player.findShipsTileLocations(map: Map): List<Tile> {
    val sourceTiles = mutableListOf<Tile>()
    for (unit in units) {
        if (unit.isNaval && unit.isCarrier && !unit.isDamaged) {
            if (unit.isAtTileLocation) {
                sourceTiles.add(unit.tile)
            } else if (unit.isAtEuropeLocation || unit.isAtHighSeasLocation) {
                sourceTiles.add(map.getSafeTile(entryLocation))
            } else if (unit.isAtColonyLocation) {
                sourceTiles.add(unit.getLocationOrNull(Colony::class.java).tile)
            }
        }
    }
    return sourceTiles
}