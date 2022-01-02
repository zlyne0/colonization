package promitech.colonization.ai;

import net.sf.freecol.common.model.Europe;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitRole;
import net.sf.freecol.common.model.ai.missions.ExplorerMission;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;
import net.sf.freecol.common.model.ai.missions.TransportUnitMission;
import net.sf.freecol.common.model.ai.missions.goodsToSell.ColoniesProductionValue;
import net.sf.freecol.common.model.ai.missions.goodsToSell.TransportGoodsToSellMission;
import net.sf.freecol.common.model.ai.missions.goodsToSell.TransportGoodsToSellMissionPlaner;
import net.sf.freecol.common.model.ai.missions.scout.ScoutMission;
import net.sf.freecol.common.model.ai.missions.scout.ScoutMissionPlaner;
import net.sf.freecol.common.model.ai.missions.workerrequest.ColonyWorkerMission;
import net.sf.freecol.common.model.ai.missions.workerrequest.ColonyWorkerRequestPlaner;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.player.Player;

import java.util.List;

public class EuropeanMissionPlaner {

	private final TransportGoodsToSellMissionPlaner transportGoodsToSellMissionPlaner;
	private final ScoutMissionPlaner scoutMissionPlaner;
	private final Game game;
	private final PathFinder pathFinder;
	private final PathFinder pathFinder2;
	private final MissionExecutor missionExecutor;

	public EuropeanMissionPlaner(Game game, PathFinder pathFinder, MissionExecutor missionExecutor, PathFinder pathFinder2) {
		this.transportGoodsToSellMissionPlaner = new TransportGoodsToSellMissionPlaner(game, pathFinder);
		this.scoutMissionPlaner = new ScoutMissionPlaner(game, pathFinder, pathFinder2);
		this.game = game;
		this.pathFinder = pathFinder;
		this.missionExecutor = missionExecutor;
		this.pathFinder2 = pathFinder2;
	}

	public void prepareMissions(Player player, PlayerMissionsContainer playerMissionContainer) {
		// transport goods(sell) and then better plan mission
		missionExecutor.executeMissions(playerMissionContainer, TransportGoodsToSellMission.class);

		scoutMissionPlaner.prepareMission(player, playerMissionContainer);

		ColonyWorkerRequestPlaner colonyWorkerRequestPlaner = new ColonyWorkerRequestPlaner(
			player, playerMissionContainer, game, pathFinder
		);
		colonyWorkerRequestPlaner.prepareMissionsAndBuyWorkers();

		for (Unit unit : player.units.copy()) {
			if (unit.isNaval() && !unit.isDamaged()) {
				navyUnitPlaner(unit, playerMissionContainer);
			}
		}

		ColonyProductionPlaner.createPlan(player, playerMissionContainer);
	}
	
	private void navyUnitPlaner(Unit navyUnit, PlayerMissionsContainer playerMissionContainer) {
		if (playerMissionContainer.isUnitBlockedForMission(navyUnit)) {
			return;
		}
		MissionPlanStatus status;

		status = transportContainedUnits(navyUnit, playerMissionContainer);
		if (status == MissionPlanStatus.MISSION_CREATED) {
			return;
		}

		if (navyUnit.isAtLocation(Europe.class)) {
			status = transportUnitFromEurope(navyUnit, playerMissionContainer);
			if (status == MissionPlanStatus.MISSION_CREATED) {
				return;
			}
		}

		if (navyUnit.isAtTileLocation()) {
			status = transportScoutUnits(navyUnit, playerMissionContainer);
			if (status == MissionPlanStatus.MISSION_CREATED) {
				return;
			}

			status = transportGoodsToSellMissionPlaner.plan(navyUnit);
			if (status == MissionPlanStatus.MISSION_CREATED) {
				return;
			}

			status = transportUnitFromEuropeWhenOnNewWorld(navyUnit, playerMissionContainer);
			if (status == MissionPlanStatus.MISSION_CREATED) {
				return;
			}

			// one turn mission
			prepareExploreMissions(navyUnit, playerMissionContainer);
		}
	}

	private MissionPlanStatus transportScoutUnits(Unit navyUnit, PlayerMissionsContainer playerMissionContainer) {
		ScoutMission scoutMission = playerMissionContainer.findFirstMission(ScoutMission.class);
		if (scoutMission != null && scoutMission.isWaitingForTransport() && !scoutMission.hasDependMissions()) {
			TransportUnitMission transportUnitMission = new TransportUnitMission(navyUnit);
			transportUnitMission.addUnitDest(scoutMission.getScout(), scoutMission.getScoutDistantDestination(), true);
			scoutMission.addDependMission(transportUnitMission);
			return MissionPlanStatus.MISSION_CREATED;
		}
		return MissionPlanStatus.NO_MISSION;
	}

	private MissionPlanStatus transportUnitFromEuropeWhenOnNewWorld(Unit navyUnit, PlayerMissionsContainer playerMissionContainer) {
		ColoniesProductionValue coloniesProductionValue = new ColoniesProductionValue(navyUnit.getOwner());
		if (!coloniesProductionValue.findSettlementWorthTakeGoodsToBuyUnit(navyUnit)) {
			return transportUnitFromEurope(navyUnit, playerMissionContainer);
		}
		return MissionPlanStatus.NO_MISSION;
	}

	private MissionPlanStatus transportUnitFromEurope(Unit navyUnit, PlayerMissionsContainer playerMissionContainer) {
		List<TransportUnitMission> transportMissions = playerMissionContainer.findMissions(TransportUnitMission.class);
		TransportUnitMission tum = null;

		Europe europe = navyUnit.getOwner().getEurope();

        for (Unit dockUnit : europe.getUnits().entities()) {
            if (dockUnit.unitRole.equalsId(UnitRole.SCOUT) && !isUnitExistsOnTransportMission(transportMissions, dockUnit)) {
                ScoutMission scoutMission = playerMissionContainer.findFirstMission(ScoutMission.class, dockUnit);
                if (scoutMission == null) {
                    continue;
                }
                if (scoutMission.isWaitingForTransport() && !scoutMission.hasDependMissions() && canEmbarkUnit(navyUnit, tum, dockUnit)) {
					if (tum == null) {
						tum = new TransportUnitMission(navyUnit);
					}
					tum.addUnitDest(dockUnit, scoutMission.getScoutDistantDestination(), true);
					scoutMission.addDependMission(tum);
                }
            }
        }

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
			return MissionPlanStatus.MISSION_CREATED;
		}
		return MissionPlanStatus.NO_MISSION;
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
	
	private MissionPlanStatus transportContainedUnits(Unit navyUnit, PlayerMissionsContainer playerMissionContainer) {
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
				return MissionPlanStatus.MISSION_CREATED;
			}
		}
		return MissionPlanStatus.NO_MISSION;
	}
	
	private void prepareExploreMissions(Unit navyUnit, PlayerMissionsContainer playerMissionContainer) {
		if (navyUnit.getTileLocationOrNull() != null) {
			ExplorerMission explorerMission = new ExplorerMission(navyUnit);
			playerMissionContainer.addMission(explorerMission);
		}
	}
	
}
