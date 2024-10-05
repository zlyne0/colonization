package promitech.colonization.screen.map.diplomacy

import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.player.Player
import promitech.colonization.Direction
import promitech.colonization.orders.diplomacy.ScoreService
import promitech.colonization.ui.resources.Messages
import promitech.colonization.ui.resources.StringTemplate


internal class ColonyTradeItem(val colony : Colony, fromPlayer : Player, toPlayer : Player)
	: TradeItem(fromPlayer, toPlayer)
{
	override fun acceptTrade(game : Game) {
		if (colony.isCoastland()) {
			moveNavyToFreeNeighbourTile(game, colony)
		}
		
		colony.changeOwner(toPlayer)
		colony.updateColonyFeatures()
		colony.updateColonyPopulation()
		toPlayer.playerExploredTiles.resetForOfWar(colony.tile, colony.settlementType.visibleRadius, game.turn)
	}

	private fun moveNavyToFreeNeighbourTile(game : Game, colony : Colony) {
		var seaTile = firstSeaNeighbourTile(game, colony.tile, colony.getOwner())
		if (seaTile != null) {
			for (unit in ArrayList(colony.tile.units.entities())) {
				if (unit.isNaval()) {
					unit.changeUnitLocation(seaTile)
					unit.setState(Unit.UnitState.ACTIVE)
				}
			}
		}
	}
	
	private fun firstSeaNeighbourTile(game : Game, tile : Tile, player : Player) : Tile? {
		for (direction : Direction in Direction.allDirections) {
			var neighbourTile = game.map.getTile(tile, direction)
			if (neighbourTile != null && neighbourTile.type.isWater()) {
				if (neighbourTile.units.isEmpty()) {
					return neighbourTile  
				}
				var unit = neighbourTile.units.first()
				if (player.equalsId(unit.getOwner())) {
					return neighbourTile
				}
			}
		}
		return null
	}
	
	override fun calculateAgreementValue(isDemand: Boolean, game: Game, ss: ScoreService) {
		if (isDemand && fromPlayer.settlements.size() < 5) {
			agreementValue = UnacceptableTradeValue
		} else {
			agreementValue = ss.scoreColony(game, colony, fromPlayer)
		}
	}

	override fun createLabel(skin: Skin): Label {
		return Label(
			Messages.message(StringTemplate.template("tradeItem.colony.long").add("%colony%", colony.getName())),
			skin
		) 
	}
	
	override fun toString() : String {
		return "ColonyTradeItem colony: ${colony.getName()}, agreementValue: $agreementValue"
	}
	
}