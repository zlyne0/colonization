package promitech.colonization.ai;

import java.util.List;

import net.sf.freecol.common.model.Europe;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.ai.ColoniesProductionGoldValue;
import net.sf.freecol.common.model.ai.missions.ExplorerMission;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;
import net.sf.freecol.common.model.ai.missions.TransportUnitMission;
import net.sf.freecol.common.model.ai.missions.goodsToSell.TransportGoodsToSellMissionPlaner;
import net.sf.freecol.common.model.ai.missions.workerrequest.ColonyWorkerMission;
import net.sf.freecol.common.model.ai.missions.workerrequest.ColonyWorkerRequestPlaceCalculator;
import net.sf.freecol.common.model.ai.missions.workerrequest.ColonyWorkerRequestPlaner;
import net.sf.freecol.common.model.ai.missions.workerrequest.EntryPointTurnRange;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.player.Player;

import static promitech.colonization.orders.NewTurnLogger.logger;

public class EuropeanMissionPlaner {

	private final TransportGoodsToSellMissionPlaner transportGoodsToSellMissionPlaner;
	private final Game game;
	private final PathFinder pathFinder;

	public EuropeanMissionPlaner(Game game, PathFinder pathFinder) {
		this.transportGoodsToSellMissionPlaner = new TransportGoodsToSellMissionPlaner(game, pathFinder);
		this.game = game;
		this.pathFinder = pathFinder;
	}

	public void prepareMissions(Player player, PlayerMissionsContainer playerMissionContainer) {
//		ColoniesProductionGoldValue colniesProdValue = new ColoniesProductionGoldValue(player, Specification.instance.goodsTypeToScoreByPrice);
//		if (logger.isDebug()) {
//			logger.debug("player[%s] colonies production gold value %s", player.getId(), colniesProdValue.goldValue());
//		}

		ColonyWorkerRequestPlaner colonyWorkerRequestPlaner = new ColonyWorkerRequestPlaner(
			player, playerMissionContainer, game, pathFinder
		);
		colonyWorkerRequestPlaner.prepareMissions();

		for (Unit unit : player.units.copy()) {
			if (unit.isNaval() && !unit.isDamaged()) {
				navyUnitPlaner(unit, playerMissionContainer);
			}
		}
		
		//colonyProductionPlaner.createPlan(player, playerMissionContainer);
	}
	
	private void navyUnitPlaner(Unit navyUnit, PlayerMissionsContainer playerMissionContainer) {
		if (playerMissionContainer.isUnitBlockedForMission(navyUnit)) {
			return;
		}
		transportContainedUnits(navyUnit, playerMissionContainer);
		if (playerMissionContainer.isUnitBlockedForMission(navyUnit)) {
			return;
		}

		if (navyUnit.isAtLocation(Europe.class)) {
			transportUnitFromEurope(navyUnit, playerMissionContainer);
		} else {
			transportGoodsToSellMissionPlaner.plan(navyUnit);
		}
		if (playerMissionContainer.isUnitBlockedForMission(navyUnit)) {
			return;
		}
		// one turn mission
		prepareExploreMissions(navyUnit, playerMissionContainer);
	}

	private void transportUnitFromEurope(Unit navyUnit, PlayerMissionsContainer playerMissionContainer) {
		if (playerMissionContainer.isUnitBlockedForMission(navyUnit)) {
			return;
		}
		Europe europe = navyUnit.getOwner().getEurope();
		
		List<TransportUnitMission> transportMissions = playerMissionContainer.findMissions(TransportUnitMission.class);
		
		TransportUnitMission tum = null;
		
		for (Unit dockUnit : europe.getUnits().entities()) {
			if (!Unit.isColonist(dockUnit.unitType, dockUnit.getOwner()) || isUnitExistsOnTransportMission(transportMissions, dockUnit)) {
				continue;
			}
			List<ColonyWorkerMission> colonyWorkerMissions = playerMissionContainer.findMissions(ColonyWorkerMission.class, dockUnit);
			
			// should be one mission
			if (colonyWorkerMissions.size() == 1 && canEmbarkUnit(navyUnit, tum, dockUnit)) {
				if (tum == null) {
					tum = new TransportUnitMission(navyUnit);
				}
				ColonyWorkerMission colonyWorkerMission = colonyWorkerMissions.get(0);
				tum.addUnitDest(dockUnit, colonyWorkerMission.getTile());
			}
		}
		if (tum != null) {
			playerMissionContainer.addMission(tum);
		}
	}

	private boolean isUnitExistsOnTransportMission(List<TransportUnitMission> transportMissions, Unit unit) {
		for (TransportUnitMission transportUnitMission : transportMissions) {
			if (transportUnitMission.isTransportedUnit(unit)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean canEmbarkUnit(Unit navyUnit, TransportUnitMission mission, Unit unit) {
		if (mission == null) {
			return navyUnit.hasSpaceForAdditionalUnit(unit.unitType);
		}
		return mission.canEmbarkUnit(unit);
	}
	
	private void transportContainedUnits(Unit navyUnit, PlayerMissionsContainer playerMissionContainer) {
		if (navyUnit.getUnitContainer() != null) {
			TransportUnitMission tum = null;
			
			for (Unit u : navyUnit.getUnitContainer().getUnits()) {
				List<ColonyWorkerMission> findMissions = playerMissionContainer.findMissions(ColonyWorkerMission.class, u);
				for (ColonyWorkerMission cwm : findMissions) {
					if (tum == null) {
						tum = new TransportUnitMission(navyUnit);
					}
					tum.addUnitDest(u, cwm.getTile());
				}
			}
			if (tum != null) {
				playerMissionContainer.addMission(tum);
			}
		}
	}
	
	private void prepareExploreMissions(Unit navyUnit, PlayerMissionsContainer playerMissionContainer) {
		if (navyUnit.getTileLocationOrNull() != null) {
			ExplorerMission explorerMission = new ExplorerMission(navyUnit);
			playerMissionContainer.addMission(explorerMission);
		}
	}
	
}
