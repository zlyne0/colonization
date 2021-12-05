package net.sf.freecol.common.model.ai

import net.sf.freecol.common.model.ObjectWithId
import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.player.Player
import promitech.colonization.savegame.XmlNodeAttributes
import promitech.colonization.savegame.XmlNodeAttributesWriter
import promitech.colonization.savegame.XmlNodeParser

class PlayerAiContainer(val player: Player) : ObjectWithId(player.id) {

    val scoutBlockTiles = mutableListOf<Tile>()

    fun addScoutBlockTile(tile: Tile) {
        this.scoutBlockTiles.add(tile)
    }

    fun isScoutTileBlocked(tile: Tile): Boolean {
        for (scoutBlockTile in scoutBlockTiles) {
            if (scoutBlockTile.equalsCoordinates(tile)) {
                return true
            }
        }
        return false
    }

    class Xml : XmlNodeParser<PlayerAiContainer> {

        val ATTR_PLAYER = "player"
        val ELEMENT_SCOUT_BLOCK_TILES = "scoutBlockTile"
        val ATTR_TILE = "tile"

        constructor() {
        }

        override fun startElement(attr: XmlNodeAttributes) {
            val player = game.players.getById(attr.getStrAttributeNotNull(ATTR_PLAYER))
            nodeObject = PlayerAiContainer(player)
        }

        override fun startWriteAttr(node: PlayerAiContainer, attr: XmlNodeAttributesWriter) {
            attr.set(ATTR_PLAYER, node.player)
            node.scoutBlockTiles.forEach { tile ->
                attr.xml.element(ELEMENT_SCOUT_BLOCK_TILES)
                attr.setPoint(ATTR_TILE, tile.x, tile.y)
                attr.xml.pop()
            }
        }

        override fun startReadChildren(attr: XmlNodeAttributes) {
            if (attr.isQNameEquals(ELEMENT_SCOUT_BLOCK_TILES)) {
                val blockTile = game.map.getSafeTile(attr.getPoint(ATTR_TILE))
                nodeObject.scoutBlockTiles.add(blockTile)
            }
        }

        override fun getTagName(): String {
            return tagName()
        }

        companion object {
            @JvmStatic
            fun tagName(): String {
                return "playerAiContainer"
            }
        }
    }

}