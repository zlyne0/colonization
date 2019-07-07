package promitech.colonization.orders.move;

import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.map.path.PathFinder;
import promitech.colonization.ai.TradeRouteMissionHandler;
import promitech.colonization.infrastructure.ThreadsResources;
import promitech.colonization.orders.move.MoveService.AfterMoveProcessor;
import promitech.colonization.screen.map.hud.GUIGameModel;

public class MoveInThreadService {

	private PathFinder pathFinder;
	private GUIGameModel guiGameModel;
	private MoveService moveService;

	public void inject(PathFinder pathFinder, GUIGameModel guiGameModel, MoveService moveService) {
		this.pathFinder = pathFinder;
		this.guiGameModel = guiGameModel;
		this.moveService = moveService;
	}
	
    public void executeTradeRoute(final Unit tradeRouteUnit, final AfterMoveProcessor afterMoveProcessor) {
    	ThreadsResources.instance.execute(new Runnable() {
			@Override
			public void run() {
				TradeRouteMissionHandler handler = new TradeRouteMissionHandler(pathFinder, guiGameModel, moveService);
				handler.executeTradeRoute(tradeRouteUnit);
				afterMoveProcessor.afterMove(tradeRouteUnit);
			}
		});
    }
	
}
