package net.sf.freecol.common.model.ai

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.ColonyId
import net.sf.freecol.common.model.ObjectWithId
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.UnitRole
import net.sf.freecol.common.model.player.Player
import net.sf.freecol.common.model.specification.GoodsType
import net.sf.freecol.common.model.specification.GoodsTypeId
import promitech.colonization.savegame.XmlNodeAttributes
import promitech.colonization.savegame.XmlNodeAttributesWriter
import promitech.colonization.savegame.XmlNodeParser

class PlayerAiContainer(val player: Player) : ObjectWithId(player.id) {

    class SupplyGoods(
        val colonyId: ColonyId,
        val goodsType: GoodsTypeId,
        val amount: Int
    )

    val scoutBlockTiles = mutableListOf<Tile>()
    var supplyGoods = mutableListOf<SupplyGoods>()

    fun removeSupplyGoodsWhenNoColony() {
        if (supplyGoods.isEmpty()) {
            return
        }
        var tmpList = ArrayList<SupplyGoods>(supplyGoods)
        supplyGoods.clear()

        tmpList.filterTo(supplyGoods) { supplyGoodsElement ->
            player.settlements.containsId(supplyGoodsElement.colonyId)
                && player.settlements.getById(supplyGoodsElement.colonyId)
                    .goodsContainer.hasGoodsQuantity(supplyGoodsElement.goodsType, supplyGoodsElement.amount)
        }
    }

    fun addSupplyGoods(colony: Colony, unitRole: UnitRole, roleCount: Int) {
        val requiredGoods = unitRole.requiredGoodsForRoleCount(roleCount)
        for (goodsEntry in requiredGoods.entries()) {
            supplyGoods.add(SupplyGoods(
                colony.id,
                goodsEntry.key,
                goodsEntry.value
            ))
        }
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

    class Xml : XmlNodeParser<PlayerAiContainer> {

        val ATTR_PLAYER = "player"
        val ELEMENT_SCOUT_BLOCK_TILES = "scoutBlockTile"
        val ELEMENT_SUPPLY_GOODS = "supplyGoods"
        val ATTR_TILE = "tile"

        val ATTR_COLONY = "colony"
        val ATTR_GOODS_TYPE = "goodsType"
        val ATTR_AMOUNT = "amount"

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
            node.supplyGoods.forEach { supplyElement ->
                attr.xml.element(ELEMENT_SUPPLY_GOODS)
                attr.set(ATTR_COLONY, supplyElement.colonyId)
                attr.set(ATTR_GOODS_TYPE, supplyElement.goodsType)
                attr.set(ATTR_AMOUNT, supplyElement.amount)
                attr.xml.pop()
            }
        }

        override fun startReadChildren(attr: XmlNodeAttributes) {
            if (attr.isQNameEquals(ELEMENT_SCOUT_BLOCK_TILES)) {
                val blockTile = game.map.getSafeTile(attr.getPoint(ATTR_TILE))
                nodeObject.scoutBlockTiles.add(blockTile)
            }
            if (attr.isQNameEquals(ELEMENT_SUPPLY_GOODS)) {
                nodeObject.supplyGoods.add(
                    SupplyGoods(
                        attr.getStrAttribute(ATTR_COLONY),
                        attr.getStrAttribute(ATTR_GOODS_TYPE),
                        attr.getIntAttribute(ATTR_AMOUNT)
                    )
                )
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