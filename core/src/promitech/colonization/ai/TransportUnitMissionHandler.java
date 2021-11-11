package promitech.colonization.ai;

import static promitech.colonization.ai.MissionHandlerLogger.logger;

import java.util.List;

import net.sf.freecol.common.model.Europe;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.MoveType;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;
import net.sf.freecol.common.model.ai.missions.TransportUnitMission;
import net.sf.freecol.common.model.map.path.Path;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.player.HighSeas;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.orders.move.MoveContext;
import promitech.colonization.orders.move.MoveService;

class TransportUnitMissionHandler implements MissionHandler<TransportUnitMission> {

    private final PathFinder pathFinder;
    private final PathFinder pathFinder2;
    private final Game game;
    private final MoveService moveService;
    private final CommonMissionHandler.MoveToEurope moveToEuropeStep;

    public TransportUnitMissionHandler(Game game, PathFinder pathFinder, MoveService moveService, PathFinder pathFinder2) {
        this.pathFinder = pathFinder;
        this.pathFinder2 = pathFinder2;
        this.game = game;
        this.moveService = moveService;
        this.moveToEuropeStep = new CommonMissionHandler.MoveToEurope(pathFinder, moveService, game);
    }

	@Override
	public void handle(PlayerMissionsContainer playerMissionsContainer, TransportUnitMission mission) {
    	Player player = playerMissionsContainer.getPlayer();
    	
    	if (!mission.isTransportUnitExists(player)) {
			logger.debug("player[%s].TransportUnitMissionHandler transport unit does not exists", player.getId());
			mission.setDone();
			return;
    	}

    	// TODO: choose unit dest the closes to actual carrier location, first disembark then move to europe
		TransportUnitMission.UnitDest firstUnitDest = mission.firstUnitToTransport();
		if (firstUnitDest == null) {
			logger.debug("player[%s].TransportUnitMissionHandler[%s] no units to transport", player.getId(), mission.getId());
			mission.setDone();
			return;
    	}

    	if (firstUnitDest.unit.isAtLocation(Europe.class)) {
    		if (mission.getCarrier().isAtLocation(Tile.class)) { moveToEurope(mission); }
    		if (mission.getCarrier().isAtLocation(HighSeas.class)) { moveViaHighSeas(); }
    		if (mission.getCarrier().isAtLocation(Europe.class)) { mission.embarkColonistsInEurope(); }
    	} else if (firstUnitDest.unit.isAtLocation(Unit.class)) {
    		if (mission.getCarrier().isAtLocation(Europe.class)) { mission.embarkColonistsInEurope(); }
    		if (mission.getCarrier().isAtLocation(HighSeas.class)) { moveViaHighSeas(); }
    		if (mission.getCarrier().isAtLocation(Tile.class)) { moveAndDisemberkUnits(mission, firstUnitDest); }
    	} else {
			mission.removeUnit(firstUnitDest);
			if (mission.firstUnitToTransport() == null) {
				logger.debug("player[%s].TransportUnitMissionHandler transport unit destination does not exists", player.getId());
				mission.setDone();
			} else {
				// TODO: try handle another unit
			}
    	}
    }

	private void moveToEurope(TransportUnitMission mission) {
		moveToEuropeStep.sail(mission.getCarrier(), mission);
	}
	
	private void moveAndDisemberkUnits(TransportUnitMission mission, TransportUnitMission.UnitDest unitDest) {
    	Unit carrier = mission.getCarrier();

		Path path = pathFinder.findToTile(game.map, carrier, unitDest.dest, PathFinder.includeUnexploredAndExcludeNavyThreatTiles);
		if (path.isReachedDestination()) {
			moveViaPathToReachableDestination(mission, path, unitDest.dest, unitDest.dest);
		} else {
			moveToCloseToDestination(mission, unitDest);
		}
	}

	private void moveToCloseToDestination(TransportUnitMission mission, TransportUnitMission.UnitDest unitDest) {
		// Not generate rangeMap for carrier. Preview use of pathFinder should have generated
		// pathFinder.generateRangeMap(game.map, carrier, unitDest, PathFinder.includeUnexploredTiles);
		pathFinder2.generateRangeMap(game.map, unitDest.dest, unitDest.unit, PathFinder.includeUnexploredTiles);

		Tile theClosestTileToDisembark = pathFinder.findFirstTheBestSumTurnCost(pathFinder2);
		if (theClosestTileToDisembark == null) {
			logger.debug(
				"player[%s].TransportUnitMissionHandler there is no direct and transfer disembark location to destination %s",
				mission.getCarrier().getOwner().getId(),
				unitDest.dest.toStringCords()
			);
			// TODO: no posibility to disembark, blocked?, stop mission, notify parent mission to change destination
			return;
		}
		Path pathToDisembark = pathFinder.createPath(theClosestTileToDisembark);
		logger.debug(
			"player[%s].TransportUnitMissionHandler transfer disembark location [%s] to destination [%s]",
			mission.getCarrier().getOwner().getId(),
			theClosestTileToDisembark.toStringCords(),
			unitDest.dest.toStringCords()
		);
		moveViaPathToReachableDestination(mission, pathToDisembark, unitDest.dest, theClosestTileToDisembark);
	}

	private void moveViaPathToReachableDestination(
		TransportUnitMission mission,
		Path path,
		Tile unitDestination,
		Tile disembarkLocation
	) {
		Unit carrier = mission.getCarrier();
		Player player = carrier.getOwner();

		MoveContext moveContext = new MoveContext(carrier, path);
		MoveType aiConfirmedMovePath = moveService.aiConfirmedMovePath(moveContext);

		if (MoveType.MOVE.equals(aiConfirmedMovePath) || MoveType.DISEMBARK.equals(aiConfirmedMovePath)) {
			if (moveContext.destTile.equalsCoordinates(disembarkLocation)) {
				List<Unit> unitsToDisembark = mission.unitsToDisembark(unitDestination);
				// disembarkLocation should equals moveContext.destTile
				boolean unitsDisembarked = moveService.disembarkUnits(
					carrier,
					unitsToDisembark,
					carrier.getTile(),
					moveContext.destTile
				);
				if (unitsDisembarked) {
					mission.removeDisembarkedUnits(player, unitDestination, disembarkLocation);
				} else {
					// ignore action, wait turn maybe something change, or next run disembark units co closest place
				}
				if (mission.getUnitsDest().isEmpty()) {
					logger.debug("player[%s].TransportUnitMissionHandler.disembark all units, end mission", player.getId());
					mission.setDone();
				}
			}
		} else {
			// ignore action, wait turn maybe something change on map, or next run disembark units co closest place
			mission.removeDisembarkedUnits(player, unitDestination, disembarkLocation);
		}
	}

	private void moveViaHighSeas() {
		// do nothing, move via high seas done in ... 
	}
}