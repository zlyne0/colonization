package promitech.colonization;

import java.util.LinkedList;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.ResourceType;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.TileImprovement;
import net.sf.freecol.common.model.TileImprovementType;
import net.sf.freecol.common.model.TileResource;
import net.sf.freecol.common.model.TileTypeTransformation;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.Unit.UnitState;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.Goods;
import net.sf.freecol.common.model.specification.Modifier;

public class GameLogic {

	private final Game game;
	
	private boolean requireUpdateMapModel = false;
	
	public GameLogic(Game game) {
		this.game = game;
	}

	public void newTurn(Player player) {
		requireUpdateMapModel = false;
		
		for (Unit unit : player.units.entities()) {
			newTurnForUnit(unit);
		}
		
		for (Settlement settlement : player.settlements.entities()) {
			if (!settlement.isColony()) {
				continue;
			}
			Colony colony = (Colony)settlement;
			System.out.println("calculate new turn for colony " + colony);
			
			colony.increaseWorkersExperience();
			colony.increaseWarehouseByProduction();
			colony.removeExcessedStorableGoods();
			colony.notificationsAboutLackOfResources();
            // TODO: updejt building queue
            // usuniecie nadmiaru z magazynu z komunikatem ze magazyny pelen
		}
		
		player.fogOfWar.resetFogOfWar(player);
	}

	public void newTurnForUnit(Unit unit) {
		unit.resetMovesLeftOnNewTurn();
		switch (unit.getState()) {
			case IMPROVING:
				workOnImprovement(unit);
				break;
			case FORTIFYING:
				unit.setState(UnitState.FORTIFIED);
				break;
			case SKIPPED:
				unit.setState(UnitState.ACTIVE);
				break;
			default:
				break;
		}
	}

	private void workOnImprovement(Unit unit) {
		if (unit.workOnImprovement()) {
			Tile improvingTile = unit.getTile();
			TileImprovementType improvementType = unit.getTileImprovementType();
			
			LinkedList<Settlement> neighbouringSettlements = game.map.findSettlements(improvingTile, unit.getOwner(), 2);
			
			TileTypeTransformation changedTileType = improvementType.changedTileType(improvingTile.getType());
			if (changedTileType != null) {
				improvingTile.changeTileType(changedTileType.getToType());
				
				Goods production = changedTileType.getProduction();
				int prodAmount = production.getAmount();
				if (unit.unitType.hasAbility(Ability.EXPERT_PIONEER)) {
					prodAmount *= 2;
				}
				if (!neighbouringSettlements.isEmpty()) {
					Settlement settlement = neighbouringSettlements.getFirst();
					prodAmount = settlement.applyModifiers(Modifier.TILE_TYPE_CHANGE_PRODUCTION, prodAmount);
					settlement.addGoods(production.getId(), prodAmount);
				}
			} else {
				TileImprovement tileImprovement = new TileImprovement(game.idGenerator, improvementType);
				improvingTile.addImprovement(tileImprovement);
				if (improvementType.isRoad()) {
					improvingTile.updateRoadConnections(game.map);
				}
			}
			
			for (Settlement settlement : neighbouringSettlements) {
				if (settlement.isContainsTile(improvingTile)) {
					settlement.initMaxPossibleProductionOnTile(improvingTile);
					break;
				}
			}
			
			for (Unit u : improvingTile.units.entities()) {
				if (u.getState() == UnitState.IMPROVING && u.getTileImprovementType().equalsId(improvementType)) {
					u.setState(UnitState.ACTIVE);
				}
			}
			
			// does improvement expose resource
			if (isExposedResourceAfterImprovement(improvingTile, improvementType)) {
				ResourceType resourceType = improvingTile.getType().exposeResource();
				int initQuantity = resourceType.initQuantity();
				improvingTile.addResource(new TileResource(game.idGenerator, resourceType, initQuantity));
			}
			
			requireUpdateMapModel = true;
			unit.setState(UnitState.ACTIVE);
		}
	}

	private boolean isExposedResourceAfterImprovement(Tile tile, TileImprovementType improvementType) {
		return !tile.hasTileResource() 
				&& Randomizer.getInstance().isHappen(improvementType) 
				&& tile.getType().allowedResourceTypes.isNotEmpty();
		
	}
	
	public boolean isRequireUpdateMapModel() {
		return requireUpdateMapModel;
	}
}
