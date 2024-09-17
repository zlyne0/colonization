package net.sf.freecol.common.model.ai.missions.goodsToSell;

import static promitech.colonization.ai.MissionHandlerLogger.logger;

import net.sf.freecol.common.model.Europe;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;
import net.sf.freecol.common.model.ai.missions.goodsToSell.TransportGoodsToSellMission.Phase;
import net.sf.freecol.common.model.map.path.Path;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.ai.CommonMissionHandler;
import promitech.colonization.ai.MissionHandler;
import promitech.colonization.orders.move.MoveContext;
import promitech.colonization.orders.move.MoveService;

/**
 * Sprzedaj mi swoja dusze.
 * Inny kupiec sie juz nie trafi.
 * Innego diabla juz nie ma. 
 */
public class TransportGoodsToSellMissionHandler implements MissionHandler<TransportGoodsToSellMission> {
	
	private final Game game;
	private final PathFinder pathFinder;
	private final MoveService moveService;
    private final CommonMissionHandler.MoveToEurope moveToEuropeStep;
	
	public TransportGoodsToSellMissionHandler(Game game, PathFinder pathFinder, MoveService moveService) {
		this.game = game;
		this.pathFinder = pathFinder;
		this.moveService = moveService;
        this.moveToEuropeStep = new CommonMissionHandler.MoveToEurope(pathFinder, moveService, game);
	}
	
	@Override
	public void handle(PlayerMissionsContainer playerMissionsContainer, TransportGoodsToSellMission mission) {
		Player player = playerMissionsContainer.getPlayer();

		Unit unit = player.units.getByIdOrNull(mission.getTransporterId());
		if (unit == null || !CommonMissionHandler.isUnitExists(player, unit)) {
			logger.debug("player[%s].TransportGoodsToSellMissionHandler transport unit does not exists", player.getId());
			mission.setDone();
			return;
		}
		
		if (mission.getPhase() == TransportGoodsToSellMission.Phase.MOVE_TO_COLONY) {
			moveToSettlement(mission, player, unit);
		}

		if (mission.getPhase() == TransportGoodsToSellMission.Phase.MOVE_TO_EUROPE) {
			moveToEurope(mission, player, unit);
		}
	}

	private void moveToSettlement(TransportGoodsToSellMission mission, Player player, Unit transporter) {
		if (transporter.isAtLocation(Europe.class)) {
			if (logger.isDebug()) {
				Settlement firstSettlementToVisit = mission.firstSettlementToVisit(player);
				logger.debug("player[%s].TransportGoodsToSellMissionHandler transporter sail to new world to colony[%s]",
					player.getId(),
					firstSettlementToVisit
				);
			}
			transporter.sailUnitToNewWorld();
			return;
		}
		
		// on HighSeas do nothing
		
		if (transporter.isAtLocation(Tile.class)) {
			Settlement firstSettlementToVisit = mission.firstSettlementToVisit(player);
			if (firstSettlementToVisit == null) {
				firstSettlementToVisit = determineNextSettlementToVisit(mission, player, transporter);
				if (firstSettlementToVisit == null) {
					// end mission or move to Europe
					return;
				}
			}
			
			if (isTransporterOnSettlement(transporter, firstSettlementToVisit)) {
				mission.loadGoodsFrom(firstSettlementToVisit, transporter);
				mission.addVisitedSettlement(firstSettlementToVisit);
				
				firstSettlementToVisit = determineNextSettlementToVisit(mission, player, transporter);
				if (firstSettlementToVisit != null) {
					moveToSettlement(transporter, firstSettlementToVisit);
				} // else move to Europe
			} else {
				moveToSettlement(transporter, firstSettlementToVisit);
				if (isTransporterOnSettlement(transporter, firstSettlementToVisit)) {
					mission.loadGoodsFrom(firstSettlementToVisit, transporter);
					mission.addVisitedSettlement(firstSettlementToVisit);
					// load cargo after move use all move points so wait for another turn
				}
			}
		}
	}
	
	private Settlement determineNextSettlementToVisit(TransportGoodsToSellMission mission, Player player, Unit transporter) {
		TransportGoodsToSellMissionPlaner planer = new TransportGoodsToSellMissionPlaner(game, pathFinder);
		planer.determineNextSettlementToVisit(mission, player, transporter);
		
		Settlement firstSettlementToVisit = mission.firstSettlementToVisit(player);
		if (firstSettlementToVisit == null) {
			if (transporter.hasGoodsCargo()) {
				logger.debug("player[%s].TransportGoodsToSellMissionHandler move to europe",player.getId());
				mission.changePhase(Phase.MOVE_TO_EUROPE);
			} else {
				logger.debug("player[%s].TransportGoodsToSellMissionHandler no cargo no settlement to visit, end mission", player.getId());
				mission.setDone();
			}
		} else {
			logger.debug(
				"player[%s].TransportGoodsToSellMissionHandler determined next settlement to visit %s",
				player.getId(), firstSettlementToVisit.getId()
			);
		}
		return firstSettlementToVisit;
	}

	private void moveToSettlement(Unit transporter, Settlement settlement) {
		logger.debug(
			"player[%s].TransportGoodsToSellMissionHandler transporter move to settlement %s",
			transporter.getOwner().getId(), settlement.getId()
		);
		
		Path path = pathFinder.findToTile(
			game.map,
			transporter.getTile(),
			settlement.tile,
			transporter,
			PathFinder.includeUnexploredAndExcludeNavyThreatTiles
		);
		if (path.isReachedDestination()) {
			MoveContext moveContext = new MoveContext(transporter, path);
			moveService.aiConfirmedMovePath(moveContext);
			// colony accessibility handled by TransportGoodsToSellMission.firstSettlementToVisit(player)
		}
	}

	private void moveToEurope(TransportGoodsToSellMission mission, Player player, Unit transporter) {
		if (transporter.isAtLocation(Europe.class)) {
			StringBuilder logStr = new StringBuilder();
			mission.sellAllCargoInEurope(game, logStr, transporter);
			
			logger.debug("player[%s].TransportGoodsToSellMissionHandler.sellInEurope %s", player.getId(), logStr);
			
			mission.setDone();
			return;
		}
		
		moveToEuropeStep.sail(transporter, mission);
	}

	private boolean isTransporterOnSettlement(Unit transporter, Settlement settlement) {
		return transporter.getTile().equalsCoordinates(settlement.tile);
	}

}
