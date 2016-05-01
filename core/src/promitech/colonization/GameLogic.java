package promitech.colonization;

import java.util.LinkedList;
import java.util.List;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Production;
import net.sf.freecol.common.model.ProductionInfo;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.TileImprovement;
import net.sf.freecol.common.model.TileImprovementType;
import net.sf.freecol.common.model.TileType;
import net.sf.freecol.common.model.TileTypeTransformation;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.Unit.UnitState;
import net.sf.freecol.common.model.UnitRole;
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
		player.fogOfWar.resetFogOfWar(player);
	}
	
	public void newTurnForUnit(Unit unit) {
		if (UnitState.IMPROVING == unit.getState()) {
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
				// TODO: po zmianie terenu i jesli teren nalezal do colony update produkcji w colony
				// TODO: przerwanie reszty ulepsaczy na tym terenie, jesli ulepszaja to samo
				// orginal ServerUnit.csImproveTile
				// TODO: ujawnianie resource exposeResourcePercent
				
				requireUpdateMapModel = true;
				unit.setState(UnitState.ACTIVE);
			}
		}
		
		unit.resetMovesLeftOnNewTurn();
		if (UnitState.SKIPPED == unit.getState()) {
			unit.setState(UnitState.ACTIVE);
		}
	}

	public boolean isRequireUpdateMapModel() {
		return requireUpdateMapModel;
	}
}
