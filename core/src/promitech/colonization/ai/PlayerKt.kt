package promitech.colonization.ai

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
