package promitech.colonization.ai;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.TradeRoute;
import net.sf.freecol.common.model.TradeRouteDefinition;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.Unit.UnitState;
import net.sf.freecol.common.model.map.path.Path;
import net.sf.freecol.common.model.map.path.PathFinder;
import promitech.colonization.orders.move.MoveContext;
import promitech.colonization.orders.move.MoveService;
import promitech.colonization.screen.map.hud.GUIGameModel;
import promitech.colonization.ui.resources.StringTemplate;

public class TradeRouteMissionHandler {

	private final PathFinder pathFinder;
	private final GUIGameModel guiGameModel;
	private final MoveService moveService;

	public TradeRouteMissionHandler(PathFinder pathFinder, GUIGameModel guiGameModel, MoveService moveService) {
		this.pathFinder = pathFinder;
		this.guiGameModel = guiGameModel;
		this.moveService = moveService;
	}
	
    public void executeTradeRoute(Unit wagon) {
        TradeRoute tradeRoute = wagon.getTradeRoute();
        
        TradeRouteDefinition tradeRouteDef = wagon.getOwner().tradeRoutes.getByIdOrNull(tradeRoute);
        if (tradeRouteDef == null) {
        	wagon.removeTradeRoute();
        	return;
        }
        StringTemplate assignedMsg = tradeRouteDef.canBeAssignedMsg(wagon.getOwner());
        if (assignedMsg != null) {
        	wagon.getOwner().eventsNotifications.addMessageNotification(
	        	StringTemplate.template("tradeRoute.invalidTradeRouteDefinition")
	            	.add("%name%", tradeRouteDef.getName())
	            	.add("%reason%", assignedMsg.eval())
        	);
        	wagon.removeTradeRoute();
        	return;
        }
        
        if (wagon.getTile().hasSettlement() && 
    		tradeRoute.containsStop(wagon.getOwner(), wagon.getTile().getSettlement())
		) {
    		tradeRoute.loadCargo(wagon, wagon.getTile().getSettlement().asColony());
        }
        
        Colony nextStopLocation = tradeRoute.nextStopLocation(wagon.getOwner());
        if (nextStopLocation == null) {
        	wagon.removeTradeRoute();
            return;
        }
        Path path = pathFinder.findToTile(
            guiGameModel.game.map,
            wagon.getTile(),
            nextStopLocation.tile,
            wagon,
            PathFinder.excludeUnexploredTiles
        );
        if (!path.reachTile(nextStopLocation.tile)) {
            // just stop, and wait when path reach stop
            wagon.setState(UnitState.SKIPPED);
            return;
        }
        
        MoveContext moveContext = new MoveContext(path);
        moveContext.initNextPathStep();
        moveService.handlePathMoveContext(moveContext);
        if (nextStopLocation.tile.equalsCoordinates(wagon.getTile())) {
        	tradeRoute.unloadCargo(wagon, nextStopLocation);
            tradeRoute.increaseNextStop(wagon.getOwner());
        }
        // end of move
    }
	
}
