package promitech.colonization;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.TileImprovement;
import net.sf.freecol.common.model.TileImprovementType;
import net.sf.freecol.common.model.TileTypeTransformation;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.Unit.UnitState;
import net.sf.freecol.common.model.UnitRole;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.Ability;

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
				TileImprovementType improvementType = unit.getTileImprovementType();
				
				TileTypeTransformation changedTileType = improvementType.changedTileType(unit.getTile().getType());
				if (changedTileType != null) {
					unit.getTile().setType(changedTileType.getToType());
					// TODO: znalezienie najbliszego miasta i dodanie mu produkcji ze zmiany terenu
					// TODO: delivery goods gdy w poblirzu (dwa pola) jest osada 
				} else {
					// TODO: add improvement
					// TODO: generate id
					TileImprovement tileImprovement = new TileImprovement(game.idGenerator, improvementType);
					unit.getTile().addImprovement(tileImprovement);
					
					if (improvementType.isRoad()) {
						unit.getTile().updateRoadConnections(game.map);
					}
				}
				// TODO: po zmianie terenu i jesli teren nalezal do colony update produkcji w colony
				// TODO: przerwanie reszty ulepsaczy na tym terenie, jesli ulepszaja to samo
				// orginal ServerUnit.csImproveTile
				// TODO: ujawnianie resource exposeResourcePercent
				
				// TODO: ustawienie ze wszystkie zmieniona mape i trzeba przerenderowac
				requireUpdateMapModel = true;
				
//				tile.
//				przy dodawaniu improvement na tile, inaczej trzeba traktowac wycinanie lasu inaczej droge i plow
//				bo wycinka lasu tak naprawde nie jest ulepszeniem terenu
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
