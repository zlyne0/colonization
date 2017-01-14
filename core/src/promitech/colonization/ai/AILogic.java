package promitech.colonization.ai;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.map.PathFinder;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.Direction;
import promitech.colonization.GameLogic;
import promitech.colonization.gamelogic.MoveContext;

public class AILogic {

	private final Game game;
	private final GameLogic gameLogic;
	private final PathFinder pathFinder = new PathFinder();
	private final NavyExplorer navyExplorer;
	
	public AILogic(Game game, GameLogic gameLogic) {
		this.game = game;
		this.gameLogic = gameLogic;
		navyExplorer = new NavyExplorer(game.map);
	}
	
	public void aiNewTurn(Player player) {
		gameLogic.newTurn(player);
		
		if (player.isIndian()) {
			return;
		}
		if (player.isLiveEuropeanPlayer()) {
			exploreSea(player);
		}
	}

	private void exploreSea(Player player) {
		Unit ship = findNavy(player);
		if (ship == null) {
			return;
		}
		if (!ship.hasMovesPoints()) {
			return;
		}
		pathFinder.generateRangeMap(game.map, ship.getTile(), ship);
		navyExplorer.generateExploreDestination(pathFinder, ship.getOwner());
		
        if (!navyExplorer.isFoundExploreDestination()) {
        	// maybe is everything explored or blocked in some how
        	System.out.println("can not find tile to explore");
        	return;
        }
        
        if (navyExplorer.isExploreDestinationInOneTurn()) {
        	exploreByOneMove(ship, player);
        } else {
        	System.out.println("exploration path " + navyExplorer.getExploreDestinationAsPath());
        }
	}

	public void exploreByOneMove(Unit ship, Player player) {
    	boolean canHandleMove = false;
        do {
            Direction direction = navyExplorer.getExploreDestinationAsDirection();
            System.out.println("exploration destination " + direction);
            
            Tile sourceTile = ship.getTile();            
			Tile destTile = game.map.getTile(sourceTile.x, sourceTile.y, direction);
			MoveContext moveContext = new MoveContext(sourceTile, destTile, ship, direction);
           
			canHandleMove = moveContext.canHandleMove();
			if (canHandleMove) {
				moveContext.handleMove();
				ship.getOwner().revealMapAfterUnitMove(game.map, ship);
				
				pathFinder.generateRangeMap(game.map, ship.getTile(), ship);
				navyExplorer.generateExploreDestination(pathFinder, ship.getOwner());
				if (navyExplorer.isFoundExploreDestination() && navyExplorer.isExploreDestinationInOneTurn()) {
					
				} else {
					System.out.println("end move points for player " + player);
					break;
				}
			}
        } while (canHandleMove);
	}
	
	public Unit findNavy(Player player) {
		for (Unit unit : player.units.entities()) {
			if (unit.isNaval()) {
				return unit;
			}
		}
		return null;
	}
	
}
