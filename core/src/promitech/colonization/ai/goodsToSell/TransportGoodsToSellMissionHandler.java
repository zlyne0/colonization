package promitech.colonization.ai.goodsToSell;

import static promitech.colonization.ai.MissionHandlerLogger.logger;

import net.sf.freecol.common.model.Europe;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;
import net.sf.freecol.common.model.ai.missions.TransportGoodsToSellMission;
import net.sf.freecol.common.model.ai.missions.TransportGoodsToSellMission.Phase;
import net.sf.freecol.common.model.map.path.Path;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.ai.MissionHandler;
import promitech.colonization.orders.move.MoveContext;
import promitech.colonization.orders.move.MoveService;

public class TransportGoodsToSellMissionHandler implements MissionHandler<TransportGoodsToSellMission> {
	
	private final Game game;
	private final PathFinder pathFinder;
	private final MoveService moveService;
	
	public TransportGoodsToSellMissionHandler(Game game, PathFinder pathFinder, MoveService moveService) {
		this.game = game;
		this.pathFinder = pathFinder;
		this.moveService = moveService;
	}
	
	@Override
	public void handle(PlayerMissionsContainer playerMissionsContainer, TransportGoodsToSellMission mission) {
		Player player = playerMissionsContainer.getPlayer();
		
		logger.debug("TransportGoodsToSellMissionHandler[%s] start execute", player.getId());

		if (!mission.isTransportUnitExists(player)) {
			mission.setDone();
			return;
		}
		
		if (mission.getPhase() == TransportGoodsToSellMission.Phase.MOVE_TO_COLONY) {
			moveToSettlement(mission, player);
		}

		if (mission.getPhase() == TransportGoodsToSellMission.Phase.MOVE_TO_EUROPE) {
			moveToEurope(mission);
		}
	}

	private void moveToSettlement(TransportGoodsToSellMission mission, Player player) {
		if (mission.getTransporter().isAtLocation(Europe.class)) {
			mission.getTransporter().sailUnitToNewWorld();
			return;
		}
		
		// on HighSeas do nothing
		
		if (mission.getTransporter().isAtLocation(Tile.class)) {
			
			Settlement firstSettlementToVisit = mission.firstSettlementToVisit(player);
			if (firstSettlementToVisit == null) {
				
				firstSettlementToVisit = determineNextSettlementToVisit(mission, player);
				if (firstSettlementToVisit == null) {
					// end mission or move to Europe
					return;
				}
			}
			
			if (mission.isTransporterOnSettlement(firstSettlementToVisit)) {
				mission.loadGoodsFrom(firstSettlementToVisit);
				
				firstSettlementToVisit = determineNextSettlementToVisit(mission, player);
				if (firstSettlementToVisit != null) {
					moveToSettlement(mission.getTransporter(), firstSettlementToVisit);
				} // else move to Europe
			} else {
				moveToSettlement(mission.getTransporter(), firstSettlementToVisit);
				if (mission.isTransporterOnSettlement(firstSettlementToVisit)) {
					mission.loadGoodsFrom(firstSettlementToVisit);
					// load cargo after move use all move points so wait for another turn 
				}
			}
		}
	}
	
	private Settlement determineNextSettlementToVisit(TransportGoodsToSellMission mission, Player player) {
		TransportGoodsToSellMissionPlaner planer = new TransportGoodsToSellMissionPlaner(pathFinder);
		planer.determineNextSettlementToVisit(game, mission, player);
		
		Settlement firstSettlementToVisit = mission.firstSettlementToVisit(player);
		if (firstSettlementToVisit == null) {
			if (mission.hasTransporterCargo()) {
				mission.changePhase(Phase.MOVE_TO_EUROPE);
			} else {
				mission.setDone();
			}
		}
		return firstSettlementToVisit;
	}

	private void moveToSettlement(Unit transporter, Settlement settlement) {
		Path path = pathFinder.findToTile(game.map, transporter.getTile(), settlement.tile, transporter);
		if (path.isReachedDestination()) {
			MoveContext moveContext = new MoveContext(path);
			moveService.aiConfirmedMovePath(moveContext);
		}
	}

	private void moveToEurope(TransportGoodsToSellMission mission) {
		if (mission.getTransporter().isAtLocation(Europe.class)) {
			// TODO: sell goods, end mission
		}
		
		// do nothing in HighSeas
		
		if (mission.getTransporter().isAtLocation(Tile.class)) {
			Path findToEurope = pathFinder.findToEurope(game.map, mission.getTransporter().getTile(), mission.getTransporter(), false);
			// TODO:
		}
	}

}
