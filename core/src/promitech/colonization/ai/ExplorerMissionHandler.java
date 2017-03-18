package promitech.colonization.ai;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.map.PathFinder;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.Direction;
import promitech.colonization.MoveDrawerSemaphore;
import promitech.colonization.gamelogic.MoveContext;

public class ExplorerMissionHandler {
	private final MoveDrawerSemaphore moveDrawerSemaphore;
	private final Game game;
	private final PathFinder pathFinder;
	private final NavyExplorer navyExplorer;
	
	public ExplorerMissionHandler(Game game, PathFinder pathFinder, MoveDrawerSemaphore moveDrawerSemaphore) {
		this.game = game;
		this.pathFinder = pathFinder;
		this.moveDrawerSemaphore = moveDrawerSemaphore;
		this.navyExplorer = new NavyExplorer(game.map);
	}

	public void executeMission(ExplorerMission mission) {
		exploreSea(mission.unit);
	}
	
	private void exploreSea(Unit ship) {
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
        	exploreByOneMove(ship, ship.getOwner());
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
				
				moveDrawerSemaphore.waitForUnitDislocationAnimation(moveContext);
				
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
	
	
}
