package net.sf.freecol.common.model.ai

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.MapIdEntities
import net.sf.freecol.common.model.ObjectWithId
import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.ai.missions.hasMission
import net.sf.freecol.common.model.colonyproduction.GoodsCollection
import net.sf.freecol.common.model.getByIdOrCreate
import net.sf.freecol.common.model.player.Player
import net.sf.freecol.common.model.removeByPredicate
import promitech.colonization.savegame.XmlNodeAttributes
import promitech.colonization.savegame.XmlNodeAttributesWriter
import promitech.colonization.savegame.XmlNodeParser

class PlayerAiContainer(val player: Player) : ObjectWithId(player.id) {

    val colonySupplyGoods: MapIdEntities<ColonySupplyGoods> = MapIdEntities<ColonySupplyGoods>()
    val scoutBlockTiles = mutableListOf<Tile>()

    fun removeSupplyGoodsWhenNoColony() {
        if (colonySupplyGoods.isEmpty()) {
            return
        }

        for (colonySupplyGood in colonySupplyGoods) {
            val settlement = player.settlements.getByIdOrNull(colonySupplyGood.colonyId)
            if (!settlement.goodsContainer.hasMoreOrEquals(colonySupplyGood.supplyGoods)) {
                colonySupplyGood.supplyGoods.clear()
            }
        }
        colonySupplyGoods.removeByPredicate { supplyEntry ->
            val settlement = player.settlements.getByIdOrNull(supplyEntry.colonyId)
            settlement == null || supplyEntry.isEmpty()
        }
    }

    fun removeOutdatedReservations(playerMissionsContainer: PlayerMissionsContainer) {
        // when mission has unit and unit die, mission end, and goods reservation should back to supply pool

        for (colonySupplyGood in colonySupplyGoods) {
            var tmpToRemove: ArrayList<ColonySupplyGoodsReservation>? = null
            for (supplyReservation in colonySupplyGood.supplyReservations) {
                if (!playerMissionsContainer.hasMission(supplyReservation.missionId)) {
                    if (tmpToRemove == null) {
                        tmpToRemove = ArrayList<ColonySupplyGoodsReservation>()
                    }
                    tmpToRemove.add(supplyReservation)
                }
            }
            if (tmpToRemove != null) {
                for (colonySupplyGoodsReservation in tmpToRemove) {
                    colonySupplyGood.addSupply(colonySupplyGoodsReservation.goods)
                    colonySupplyGood.supplyReservations.removeId(colonySupplyGoodsReservation)
                }
            }
        }
    }

    fun addSupplyGoods(colony: Colony, goodsCollection: GoodsCollection) {
        if (!goodsCollection.isEmpty()) {
            var colonySupply = colonySupplyGoods.getByIdOrCreate(colony.id) { ColonySupplyGoods(colony.id) }
            colonySupply.addSupply(goodsCollection)
        }
    }

    fun findOrCreateColonySupplyGoods(colony: Colony): ColonySupplyGoods {
        return colonySupplyGoods.getByIdOrCreate(colony.id) { ColonySupplyGoods(colony.id) }
    }

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

    class Xml : XmlNodeParser<PlayerAiContainer>() {
        private val ATTR_PLAYER = "player"
        private val ELEMENT_SCOUT_BLOCK_TILES = "scoutBlockTile"
        private val ATTR_TILE = "tile"

        init {
            addNodeForMapIdEntities("colonySupplyGoods", ColonySupplyGoods::class.java)
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