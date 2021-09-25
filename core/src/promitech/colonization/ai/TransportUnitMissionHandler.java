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
    private final Game game;
    private final MoveService moveService;
    private final CommonMissionHandler.MoveToEurope moveToEuropeStep;

    public TransportUnitMissionHandler(Game game, PathFinder pathFinder, MoveService moveService) {
        this.pathFinder = pathFinder;
        this.game = game;
        this.moveService = moveService;
        this.moveToEuropeStep = new CommonMissionHandler.MoveToEurope(pathFinder, moveService, game);
    }

	@Override
	public void handle(PlayerMissionsContainer playerMissionsContainer, TransportUnitMission mission) {
    	Player player = playerMissionsContainer.getPlayer();
    	
    	if (!mission.isTransportUnitExists(player)) {
			logger.debug("TransportUnitMissionHandler[%s] transport unit does not exists", player.getId());
			mission.setDone();
			return;
    	}

		Unit firstUnit = mission.firstUnit();
		if (firstUnit == null) {
			logger.debug("TransportUnitMissionHandler[%s] no units to transport", player.getId());
			mission.setDone();
			return;
    	}

    	if (firstUnit.isAtLocation(Europe.class)) {
    		if (mission.getCarrier().isAtLocation(Tile.class)) { moveToEurope(mission); }
    		if (mission.getCarrier().isAtLocation(HighSeas.class)) { moveViaHighSeas(); }
    		if (mission.getCarrier().isAtLocation(Europe.class)) { mission.embarkColonistsInEurope(); }
    	} else if (firstUnit.isAtLocation(Unit.class)) {
    		if (mission.getCarrier().isAtLocation(Europe.class)) { mission.embarkColonistsInEurope(); }
    		if (mission.getCarrier().isAtLocation(HighSeas.class)) { moveViaHighSeas(); }
    		if (mission.getCarrier().isAtLocation(Tile.class)) { moveAndDisemberkUnits(player, mission); }
    	} else {
			mission.removeUnit(firstUnit);
			if (mission.firstUnit() == null) {
				logger.debug("TransportUnitMissionHandler[%s] transport unit destination does not exists", player.getId());
				mission.setDone();
			} else {
				// TODO: try handle another unit
			}
    	}
    }

	private void moveToEurope(TransportUnitMission mission) {
		moveToEuropeStep.sail(mission.getCarrier(), mission);
	}
	
	private void moveAndDisemberkUnits(Player player, TransportUnitMission mission) {
		Unit carrier = mission.getCarrier();
		
		Path path = pathFinder.findTheQuickestToTile(
			game.map,
			carrier.getTile(),
			mission.destTiles(),
			carrier,
			PathFinder.includeUnexploredAndExcludeNavyThreatTiles
		);

		if (path.isReachedDestination()) {
			Tile destination = path.endTile;
			
			MoveContext moveContext = new MoveContext(carrier, path);
			MoveType aiConfirmedMovePath = moveService.aiConfirmedMovePath(moveContext);
			
			if (MoveType.MOVE.equals(aiConfirmedMovePath) || MoveType.DISEMBARK.equals(aiConfirmedMovePath)) {
				if (moveContext.destTile.equalsCoordinates(destination)) {
					
					List<Unit> unitsToDisembark = mission.unitsToDisembark(destination);
					boolean unitsDisembarked = moveService.disembarkUnits(
						carrier, 
						unitsToDisembark, 
						carrier.getTile(), 
						moveContext.destTile
					);
					if (unitsDisembarked) {
						mission.removeDisembarkedUnits(player, destination);
					} else {
						// TODO: handle lack of posibility to disembark unit and try find another place
					}

					if (mission.getUnitsDest().isEmpty()) {
						logger.debug("TransportUnitMissionHandler[%s].disembark all units, end mission", player.getId());
						mission.setDone();
					}
				}
			} else {
				mission.removeDisembarkedUnits(player, destination);
				// TODO: handle lack of posibility to disembark unit and try find another place
			}
		}
	}

	private void moveViaHighSeas() {
		// do nothing, move via high seas done in ... 
	}
}