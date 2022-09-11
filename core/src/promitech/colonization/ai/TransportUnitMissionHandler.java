package promitech.colonization.ai;

import com.badlogic.gdx.utils.Disposable;

import net.sf.freecol.common.model.Europe;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.MoveType;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitMoveType;
import net.sf.freecol.common.model.ai.missions.AbstractMission;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;
import net.sf.freecol.common.model.ai.missions.TransportUnitMission;
import net.sf.freecol.common.model.ai.missions.TransportUnitMission.CargoDest;
import net.sf.freecol.common.model.ai.missions.TransportUnitMission.UnitDest;
import net.sf.freecol.common.model.ai.missions.pioneer.RequestGoodsMission;
import net.sf.freecol.common.model.ai.missions.pioneer.RequestGoodsMissionHandler;
import net.sf.freecol.common.model.map.path.Path;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.player.HighSeas;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import promitech.colonization.orders.move.MoveContext;
import promitech.colonization.orders.move.MoveService;

import static promitech.colonization.ai.MissionHandlerLogger.logger;

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

    	// small optimization, transport mission can be executed several times on different parent missions
    	if (mission.getCarrier().isAtTileLocation() && !mission.getCarrier().hasMovesPoints()) {
			return;
		}
		validateDestinations(mission);

		TransportUnitMission.TransportDestination transportDestination = firstFirstDestinationToTransport(mission);
    	if (transportDestination == null) {
			logger.debug("player[%s].TransportUnitMissionHandler[%s] no units to transport", player.getId(), mission.getId());
			mission.setDone();
			return;
		}

		if (transportDestination instanceof CargoDest) {
			CargoDest cargoDest = (CargoDest)transportDestination;
			if (mission.getCarrier().isAtLocation(Europe.class)) {
				// do nothing ship should be loaded already
				mission.embarkColonistsInEurope();
			}
			if (mission.getCarrier().isAtLocation(HighSeas.class)) { moveViaHighSeas(); }
			if (mission.getCarrier().isAtLocation(Tile.class)) { moveAndUnloadCargo(mission, cargoDest); }
		}

		if (transportDestination instanceof UnitDest) {
			UnitDest unitDest = (UnitDest)transportDestination;
			if (unitDest.unit.isAtLocation(Europe.class)) {
				if (mission.getCarrier().isAtLocation(Tile.class)) { moveToEurope(mission); }
				if (mission.getCarrier().isAtLocation(HighSeas.class)) { moveViaHighSeas(); }
				if (mission.getCarrier().isAtLocation(Europe.class)) { mission.embarkColonistsInEurope(); }
			} else if (unitDest.unit.isAtLocation(Unit.class)) {
				if (mission.getCarrier().isAtLocation(Europe.class)) { mission.embarkColonistsInEurope(); }
				if (mission.getCarrier().isAtLocation(HighSeas.class)) { moveViaHighSeas(); }
				if (mission.getCarrier().isAtLocation(Tile.class)) { moveAndDisemberkUnits(mission, unitDest); }
			} else if (unitDest.unit.isAtLocation(Tile.class)) {
				if (mission.getCarrier().isAtLocation(Europe.class)) { mission.embarkColonistsInEurope(); }
				if (mission.getCarrier().isAtLocation(HighSeas.class)) { moveViaHighSeas(); }
				if (mission.getCarrier().isAtLocation(Tile.class)) { moveAndEmbarkUnits(mission, unitDest); }
			} else {
				mission.removeUnit(unitDest);
				logger.debug("player[%s].TransportUnitMissionHandler[%s] unrecognize dest unit destynation type", player.getId(), mission.getId());
				mission.setDone();
			}
    	}

		if (mission.isDestinationEmpty()) {
			logger.debug("player[%s].TransportUnitMissionHandler.disembark all units, end mission", player.getId());
			mission.setDone();
		}
    }

	private void validateDestinations(TransportUnitMission mission) {
    	mission.validateUnitDestination();
		validateCargoDestinations(mission);
	}

    private void validateCargoDestinations(TransportUnitMission mission) {
    	Player player = mission.getCarrier().getOwner();

    	List<CargoDest> cargoToRemove = null;
		for (CargoDest cargoDest : mission.getCargoDests()) {
			if (!cargoDest.dest.hasSettlement() || !cargoDest.dest.getSettlement().getOwner().equalsId(player)) {
				if (cargoToRemove == null) {
					cargoToRemove = new ArrayList<CargoDest>();
				}
				if (player.settlements.isEmpty()) {
					cargoToRemove.add(cargoDest);
				} else {
					Tile newDestination = theClosestSettlementTile(mission, cargoDest.dest);
					if (newDestination == null) {
						cargoToRemove.add(cargoDest);
					} else {
						cargoToRemove.add(cargoDest);
						mission.addCargoDest(cargoDest, newDestination);
					}
				}
			}
		}
		if (cargoToRemove != null) {
			mission.getCargoDests().removeAll(cargoToRemove);
		}
	}

	private void moveToEurope(TransportUnitMission mission) {
		moveToEuropeStep.sail(mission.getCarrier(), mission);
	}
	
	private void moveAndDisemberkUnits(TransportUnitMission mission, UnitDest unitDest) {
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

	private void moveToCloseToDestination(TransportUnitMission mission, UnitDest unitDest) {
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

		if (MoveType.MOVE_NO_MOVES.equals(aiConfirmedMovePath)) {
			return;
		}

		if (MoveType.MOVE.equals(aiConfirmedMovePath)
			|| MoveType.DISEMBARK.equals(aiConfirmedMovePath)
			|| MoveType.MOVE_HIGH_SEAS.equals(aiConfirmedMovePath)
		) {
			if (moveContext.destTile.equalsCoordinates(disembarkLocation)) {
				tryDisembarkUnits(mission, unitDestination, disembarkLocation);
				tryUnloadCargo(mission, disembarkLocation);
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

			List<Unit> unitsToDisembark = mission.unitsToDisembark(unitDestination);
			notifyParentMissionAboutNoDisembarkAccess(mission, unitDestination, unitsToDisembark);

			// planer should take case about unit and generate another destination
			List<UnitDest> unitDests = mission.removeNoAccessTileUnits(player, unitDestination);
			endTransportRequestMissions(unitDests);
		}
	}

	private void moveViaHighSeas() {
		// do nothing, move via high seas done in ... 
	}

	private void moveAndEmbarkUnits(TransportUnitMission mission, UnitDest unitDest) {
		Player player = mission.getCarrier().getOwner();

		Path shipPath = pathFinder.findToTile(
			game.map,
			mission.getCarrier(),
			unitDest.unit.getTile(),
			CollectionUtils.enumSum(PathFinder.includeUnexploredTiles, PathFinder.FlagTypes.AvoidDisembark)
		);

		if (!shipPath.isReachedDestination()) {
			pathFinder2.generateRangeMap(
				game.map,
				unitDest.unit,
				CollectionUtils.enumSum(PathFinder.includeUnexploredTiles, PathFinder.FlagTypes.AllowEmbark)
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
			for (UnitDest unitDest : mission.getUnitsDest()) {
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

		for (UnitDest unitDest : mission.getUnitsDest()) {
			if (unitDest.unit.getTile().isStepNextTo(carrierLocation)) {
				MoveContext moveContext = MoveContext.embarkUnit(unitDest.unit, carrier);
				if (MoveType.EMBARK.equals(moveContext.moveType)) {
					moveService.aiConfirmedMoveProcessor(moveContext);
				}
			} else if (unitDest.unit.getTile().equalsCoordinates(carrierLocation)) {
			    unitDest.unit.embarkTo(carrier);
            }
		}
	}

	private void endTransportRequestMissions(List<UnitDest> unitDestList) {
		for (UnitDest unitDest : unitDestList) {
			if (unitDest.transportRequestMissionId == null) {
				continue;
			}
			AbstractMission am = playerMissionsContainer.findMission(unitDest.transportRequestMissionId);
			if (am != null) {
				am.setDone();
			}
		}
	}

	private void notifyParentMissionAboutNoDisembarkAccess(
		TransportUnitMission transportUnitMission,
		Tile unitDestination,
		List<Unit> unitsToDisembark
	) {
		Player player = playerMissionsContainer.getPlayer();
		if (logger.isDebug()) {
			logger.debug("player[%s].TransportUnitMissionHandler notify parent mission about no disembark access", player.getId());
		}
		for (MissionHandler<? extends AbstractMission> missionHandler : missionExecutor.allMissionHandlers()) {
			if (missionHandler instanceof TransportUnitNoDisembarkAccessNotification) {
				for (Unit unitToDisembark : unitsToDisembark) {
					((TransportUnitNoDisembarkAccessNotification)missionHandler).noDisembarkAccessNotification(
						playerMissionsContainer,
						transportUnitMission,
						unitDestination,
						unitToDisembark
					);
				}
			}
		}
	}

	private TransportUnitMission.TransportDestination firstFirstDestinationToTransport(TransportUnitMission mission) {
		// first looking for by path, path can not exist(next destination disembark), so take first from list
		TransportUnitMission.TransportDestination theClosestTransportDestination = findTheClosestTransportDestination(mission);
		if (theClosestTransportDestination != null) {
			return theClosestTransportDestination;
		}
		return mission.firstTransportOrder();
	}

	private TransportUnitMission.TransportDestination findTheClosestTransportDestination(TransportUnitMission mission) {
		Tile sourceTile = mission.carrierPosition(game.map);
		if (sourceTile == null)	{
			return null;
		}
		Collection<Tile> tiles = mission.destTiles();
		Tile tile = pathFinder.findTheQuickestTile(game.map, sourceTile, tiles, mission.getCarrier(), PathFinder.includeUnexploredTiles);
		if (tile != null) {
			return mission.firstTransportDestinationForTile(tile);
		}
		return null;
	}

	private void moveAndUnloadCargo(TransportUnitMission mission, CargoDest cargoDest) {
		Path path = pathFinder.findToTile(game.map, mission.getCarrier(), cargoDest.dest, PathFinder.includeUnexploredAndExcludeNavyThreatTiles);
		if (path.isReachedDestination()) {
			MoveContext moveContext = new MoveContext(mission.getCarrier(), path);
			moveService.aiConfirmedMovePath(moveContext);

			if (mission.getCarrier().getTile().equalsCoordinates(cargoDest.dest)) {
				tryUnloadCargo(mission, cargoDest.dest);
				tryDisembarkUnits(mission, cargoDest.dest, cargoDest.dest);
			}
		}
	}

	private Tile theClosestSettlementTile(TransportUnitMission mission, Tile sourceTile) {
    	Player player = mission.getCarrier().getOwner();

    	List<Tile> tiles = new ArrayList<Tile>(player.settlements.size());
		for (Settlement settlement : player.settlements) {
			tiles.add(settlement.tile);
		}
		return pathFinder.findTheQuickestTile(
			game.map,
			sourceTile,
			tiles,
			mission.getCarrier(),
			PathFinder.excludeUnexploredTiles
		);
	}

	private void tryUnloadCargo(TransportUnitMission mission, Tile unloadDestination) {
		if (unloadDestination.hasSettlement() && mission.getCarrier().getTile().equalsCoordinates(unloadDestination)) {
			List<CargoDest> unloadedCargo = mission.unloadCargo(unloadDestination);
			if (!unloadedCargo.isEmpty()) {
				RequestGoodsMissionHandler requestGoodsMissionHandler = (RequestGoodsMissionHandler)missionExecutor.findMissionHandler(RequestGoodsMission.class);

				for (CargoDest cargo : unloadedCargo) {
					if (cargo.requestGoodsMissionId != null) {
						requestGoodsMissionHandler.handleUnloadCargoForGoodsRequest(cargo.requestGoodsMissionId);
					}
				}
			}
		}
	}

	private void tryDisembarkUnits(TransportUnitMission mission, Tile unitDestination, Tile disembarkLocation) {
    	Player player = mission.getCarrier().getOwner();
    	Unit carrier = mission.getCarrier();

		List<Unit> unitsToDisembark = mission.unitsToDisembark(unitDestination);
		if (unitsToDisembark.isEmpty()) {
			return;
		}

		// disembarkLocation should equals moveContext.destTile
		boolean unitsDisembarked = moveService.disembarkUnits(
			carrier,
			unitsToDisembark,
			carrier.getTile(),
			disembarkLocation
		);
		if (unitsDisembarked) {
			List<UnitDest> unitDests = mission.removeDisembarkedUnits(player, unitDestination, disembarkLocation);
			endTransportRequestMissions(unitDests);
		} else {
			// ignore action, wait turn maybe something change, or next run disembark units co closest place
		}
	}
}