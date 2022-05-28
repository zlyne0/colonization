package promitech.colonization.ai;

import static promitech.colonization.ai.MissionHandlerLogger.logger;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.MoveType;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.ai.missions.AbstractMission;
import net.sf.freecol.common.model.map.path.Path;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.orders.move.MoveContext;
import promitech.colonization.orders.move.MoveService;

public class CommonMissionHandler {

	public static class MoveToEurope {
		private final PathFinder pathFinder;
		private final MoveService moveService;
		private final Game game;
		
		public MoveToEurope(PathFinder pathFinder, MoveService moveService, Game game) {
			this.pathFinder = pathFinder;
			this.moveService = moveService;
			this.game = game;
		}

		public void sail(Unit unit, AbstractMission mission) {
			// do nothing in HighSeas, handled in NewTurnService.sailOnHighSeas

			if (unit.isAtLocation(Tile.class)) {
				if (logger.isDebug()) {
					logger.debug("%s[%s] move to highseas", mission.getClass().getSimpleName(), mission.getId());
				}
				
				Path pathEurope = pathFinder.findToEurope(
					game.map,
					unit.getTile(),
					unit,
					PathFinder.includeUnexploredAndExcludeNavyThreatTiles
				);
				
				MoveContext moveContext = new MoveContext(unit, pathEurope);
		    	MoveType aiConfirmedMovePath = moveService.aiConfirmedMovePath(moveContext);
		    	
		    	if (aiConfirmedMovePath == MoveType.MOVE_HIGH_SEAS) {
		    		unit.sailUnitToEurope(moveContext.destTile);
		    	}
			}
		}
	}
	
	public static boolean isUnitExists(Player player, Unit unit) {
		if (player.isDead()) {
			return false;
		}
		if (unit == null || unit.isDisposed() || unit.isDamaged() || !player.units.containsId(unit) 
				|| !unit.isOwner(player)) {
			return false;
		}
		return true;
	}

	public static boolean isColonyOwner(Player player, String settlementId) {
		if (player.isDead()) {
			return false;
		}
		return player.settlements.containsId(settlementId);
	}
}
