package promitech.colonization.ai;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.ai.missions.ExplorerMission;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;
import net.sf.freecol.common.model.map.path.PathFinder;
import promitech.colonization.Direction;
import promitech.colonization.move.MoveContext;
import promitech.colonization.move.MoveService;

public class ExplorerMissionHandler implements MissionHandler<ExplorerMission> {
    public static enum ExploreStatus {
        NO_MOVE_POINTS,
        NO_DESTINATION,
        MORE_THEN_ONE_TURN,
        OK
    }
    
	private final MoveService moveService;
	private final Game game;
	private final PathFinder pathFinder;
	private final NavyExplorer navyExplorer;

	public ExplorerMissionHandler(Game game, PathFinder pathFinder, MoveService moveService) {
		this.game = game;
		this.pathFinder = pathFinder;
		this.moveService = moveService;
		this.navyExplorer = new NavyExplorer(game.map);
	}

	@Override
	public void handle(PlayerMissionsContainer playerMissionsContainer, ExplorerMission mission) {
		exploreByAllMoves(mission.unit);
	}

	private ExploreStatus prepareExploration(Unit ship) {
        if (!ship.hasMovesPoints()) {
            return ExploreStatus.NO_MOVE_POINTS;
        }
        pathFinder.generateRangeMap(game.map, ship.getTile(), ship);
        navyExplorer.generateExploreDestination(pathFinder, ship.getOwner());

        if (!navyExplorer.isFoundExploreDestination()) {
            // maybe is everything explored or blocked in some how
            System.out.println("can not find tile to explore");
            return ExploreStatus.NO_DESTINATION;
        }
        
        return ExploreStatus.OK;
	}

	private ExploreStatus exploreByAllMoves(Unit ship) {
	    ExploreStatus exploreStatus = ExploreStatus.OK;
	    
	    do {
	        exploreStatus = prepareExploration(ship);
	        if (exploreStatus != ExploreStatus.OK) {
	            return exploreStatus;
	        }
	        if (!navyExplorer.isExploreDestinationInOneTurn()) {
	            return ExploreStatus.MORE_THEN_ONE_TURN;
	        }
            Direction direction = navyExplorer.getExploreDestinationAsDirection();
            System.out.println("exploration destination " + direction);

            Tile sourceTile = ship.getTile();            
            Tile destTile = game.map.getTile(sourceTile.x, sourceTile.y, direction);
            MoveContext moveContext = new MoveContext(sourceTile, destTile, ship, direction);
            if (!moveContext.canHandleMove()) {
                return ExploreStatus.NO_MOVE_POINTS;
            }
            moveService.aiConfirmedMoveProcessor(moveContext);
            
            if (!ship.hasMovesPoints()) {
                return ExploreStatus.OK;
            }
	    } while (exploreStatus == ExploreStatus.OK);
	    
	    return exploreStatus;
	}

	public ExploreStatus exploreByOneMove(Unit ship) {
        ExploreStatus exploreStatus = prepareExploration(ship);
        if (exploreStatus != ExploreStatus.OK) {
            return exploreStatus;
        }
	    
        Direction direction = navyExplorer.getExploreDestinationAsDirection();
        System.out.println("exploration destination " + direction);
        
        Tile sourceTile = ship.getTile();            
        Tile destTile = game.map.getTile(sourceTile.x, sourceTile.y, direction);
        MoveContext moveContext = new MoveContext(sourceTile, destTile, ship, direction);
       
        if (moveContext.canHandleMove()) {
            moveService.aiConfirmedMoveProcessor(moveContext);
        } else {
            exploreStatus = ExploreStatus.NO_MOVE_POINTS;
        }
        return exploreStatus;
	}
}
