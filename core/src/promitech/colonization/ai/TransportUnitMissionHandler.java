package promitech.colonization.ai;

import com.badlogic.gdx.utils.Disposable;

import static promitech.colonization.ai.MissionHandlerLogger.logger;

import java.util.List;

import net.sf.freecol.common.model.Europe;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.MoveType;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitMoveType;
import net.sf.freecol.common.model.ai.missions.AbstractMission;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;
import net.sf.freecol.common.model.ai.missions.TransportUnitMission;
import net.sf.freecol.common.model.map.path.Path;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.player.HighSeas;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.util.CollectionUtils;

import promitech.colonization.orders.move.MoveContext;
import promitech.colonization.orders.move.MoveService;

class TransportUnitMissionHandler implements MissionHandler<TransportUnitMission>, Disposable {

    private final PathFinder pathFinder;
    private final PathFinder pathFinder2;
    private final Game game;
    private final MoveService moveService;
    private final CommonMissionHandler.MoveToEurope moveToEuropeStep;
	private final UnitMoveType unitMoveType = new UnitMoveType();

	private MissionExecutor missionExecutor;
	private PlayerMissionsContainer playerMissionsContainer;

    public TransportUnitMissionHandler(MissionExecutor missionExecutor, Game game, PathFinder pathFinder, MoveService moveService, PathFinder pathFinder2) {
        this.missionExecutor = missionExecutor;
        this.pathFinder = pathFinder;
        this.pathFinder2 = pathFinder2;
        this.game = game;
        this.moveService = moveService;
        this.moveToEuropeStep = new CommonMissionHandler.MoveToEurope(pathFinder, moveService, game);
    }

    @Override
    public void dispose() {
    	this.missionExecutor = null;
    	this.playerMissionsContainer = null;
	}

	@Override
	public void handle(PlayerMissionsContainer playerMissionsContainer, TransportUnitMission mission) {
    	this.playerMissionsContainer = playerMissionsContainer;
    	Player player = playerMissionsContainer.getPlayer();
    	
    	if (!mission.isTransportUnitExists(player)) {
			logger.debug("player[%s].TransportUnitMissionHandler transport unit does not exists", player.getId());
			mission.setDone();
			return;
    	}

    	// order should be set by parent mission, parent mission should decide what destination is more important
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
    	} else if (firstUnitDest.unit.isAtLocation(Tile.class)) {
			if (mission.getCarrier().isAtLocation(Europe.class)) { mission.embarkColonistsInEurope(); }
			if (mission.getCarrier().isAtLocation(HighSeas.class)) { moveViaHighSeas(); }
			if (mission.getCarrier().isAtLocation(Tile.class)) { moveAndEmbarkUnits(mission, firstUnitDest); }
		} else {
			mission.removeUnit(firstUnitDest);
			logger.debug("player[%s].TransportUnitMissionHandler[%s] unrecognize dest unit destynation type", player.getId(), mission.getId());
			mission.setDone();
    	}
    }

	private void moveToEurope(TransportUnitMission mission) {
		moveToEuropeStep.sail(mission.getCarrier(), mission);
	}
	
	private void moveAndDisemberkUnits(TransportUnitMission mission, TransportUnitMission.UnitDest unitDest) {
    	Unit carrier = mission.getCarrier();
		Path path = pathFinder.findToTile(game.map, carrier, unitDest.dest, PathFinder.includeUnexploredAndExcludeNavyThreatTiles);
		MoveType lastMoveType = unitMoveType.calculateMoveType(carrier, carrier.getTileLocationOrNull(), path.endTile);
		if (path.isReachedDestination() && (lastMoveType == MoveType.DISEMBARK || lastMoveType == MoveType.MOVE)) {
			// disembark on seaside and move to colony
			moveViaPathToReachableDestination(mission, path, unitDest.dest, unitDest.dest);
		} else {
			moveToCloseToDestination(mission, unitDest);
		}
	}

	private void moveToCloseToDestination(TransportUnitMission mission, TransportUnitMission.UnitDest unitDest) {
		// Not generate rangeMap for carrier. Preview use of pathFinder should have generated
		// pathFinder.generateRangeMap(game.map, carrier, unitDest, PathFinder.includeUnexplored...
		pathFinder2.generateRangeMap(game.map, unitDest.dest, unitDest.unit, PathFinder.includeUnexploredTiles);

		Tile theClosestTileToDisembark = pathFinder.findFirstTheBestSumTurnCost(pathFinder2, PathFinder.SumPolicy.SIMPLY_SUM);
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
		if (logger.isDebug()) {
			logger.debug(
				"player[%s].TransportUnitMissionHandler transfer disembark location [%s] to destination [%s]",
				mission.getCarrier().getOwner().getId(),
				theClosestTileToDisembark.toStringCords(),
				unitDest.dest.toStringCords()
			);
		}
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
			if (logger.isDebug()) {
				logger.debug(
					"player[%s].TransportUnitMissionHandler.disembark.error can not disembark to [%s]. MoveType: %s",
					player.getId(),
					disembarkLocation.toStringCords(),
					aiConfirmedMovePath
				);
			}
			mission.setDone();
			notifyParentMissionAboutNoDisembarkAccess(mission, unitDestination);
		}
	}

	private void moveViaHighSeas() {
		// do nothing, move via high seas done in ... 
	}

	private void moveAndEmbarkUnits(TransportUnitMission mission, TransportUnitMission.UnitDest unitDest) {
		Player player = mission.getCarrier().getOwner();

		Path shipPath = pathFinder.findToTile(
			game.map,
			mission.getCarrier(),
			unitDest.unit.getTile(),
			CollectionUtils.enumSet(PathFinder.includeUnexploredTiles, PathFinder.FlagTypes.AvoidDisembark)
		);

		if (!shipPath.isReachedDestination()) {
			pathFinder2.generateRangeMap(
				game.map,
				unitDest.unit,
				CollectionUtils.enumSet(PathFinder.includeUnexploredTiles, PathFinder.FlagTypes.AllowEmbark)
			);
			Tile transferLocation = pathFinder.findFirstTheBestSumTurnCost(pathFinder2, PathFinder.SumPolicy.SIMPLY_SUM);
			if (transferLocation == null) {
				logger.debug("player[%s].TransportUnitMissionHandler can not find transfer location. End mission.", player.getId());
				// TODO: ship can not sail to embark unit, end mission, notify parent mission to change destination
				mission.setDone();
				return;
			}
			logger.debug(
				"player[%s].TransportUnitMissionHandler move unit and carrier to transfer location [%s]",
				player.getId(),
				transferLocation.toStringCords()
			);

			shipPath = pathFinder.createPath(transferLocation);
			MoveContext moveContext = new MoveContext(mission.getCarrier(), shipPath);
			moveService.aiConfirmedMovePath(moveContext);

			Path unitPath = pathFinder2.createPath(transferLocation);
			moveContext = new MoveContext(unitDest.unit, unitPath);
			moveService.aiConfirmedMovePath(moveContext);
		} else {
			logger.debug("player[%s].TransportUnitMissionHandler move carrier to unit location [%s]", player.getId(), shipPath.endTile.toStringCords());

			MoveContext moveContext = new MoveContext(mission.getCarrier(), shipPath);
			moveService.aiConfirmedMovePath(moveContext);

			// TODO: when unitDest.allowUnitMove unit move to carrier and when carrier move turns > 1
		}

        tryEmbarkUnitsIfInRange(mission);
	}

	private void tryEmbarkUnitsIfInRange(TransportUnitMission mission) {
        Unit carrier = mission.getCarrier();
        Tile carrierLocation = carrier.getTile();
        Player player = carrier.getOwner();

    	if (logger.isDebug()) {
			String str = "";
			for (TransportUnitMission.UnitDest unitDest : mission.getUnitsDest()) {
                if (unitDest.unit.getTile().isStepNextTo(carrierLocation)
                    || unitDest.unit.getTile().equalsCoordinates(carrierLocation)) {
					if (!str.isEmpty()) {
						str += ", ";
					}
					str += unitDest.toString();
				}
			}
			if (!str.isEmpty()) {
				logger.debug("player[%s].TransportUnitMissionHandler embark units %s", player.getId(), str);
			}
		}

		for (TransportUnitMission.UnitDest unitDest : mission.getUnitsDest()) {
			if (unitDest.unit.getTile().isStepNextTo(carrierLocation)) {
				MoveContext moveContext = MoveContext.embarkUnit(unitDest.unit, carrier);
				moveService.aiConfirmedMoveProcessor(moveContext);
			} else if (unitDest.unit.getTile().equalsCoordinates(carrierLocation)) {
			    unitDest.unit.embarkTo(carrier);
            }
		}
	}

	private void notifyParentMissionAboutNoDisembarkAccess(
		TransportUnitMission transportUnitMission,
		Tile unitDestination
	) {
		Player player = playerMissionsContainer.getPlayer();
		if (logger.isDebug()) {
			logger.debug("player[%s].TransportUnitMissionHandler notify parent mission about no disembark access", player.getId());
		}
		AbstractMission parentMission = playerMissionsContainer.findParentMission(transportUnitMission);
		if (parentMission == null) {
			return;
		}
		MissionHandler<AbstractMission> missionHandler = missionExecutor.findMissionHandler(parentMission);
		if (missionHandler instanceof TransportUnitNoDisembarkAccessNotification) {
			((TransportUnitNoDisembarkAccessNotification)missionHandler).noDisembarkAccessNotification(
				transportUnitMission,
				unitDestination,
				parentMission
			);
		}
	}
}