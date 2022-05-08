package promitech.colonization.ai;

import net.sf.freecol.common.model.Europe;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitRole;
import net.sf.freecol.common.model.ai.missions.ExplorerMission;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;
import net.sf.freecol.common.model.ai.missions.TransportUnitMission;
import net.sf.freecol.common.model.ai.missions.goodsToSell.TransportGoodsToSellMissionPlaner;
import net.sf.freecol.common.model.ai.missions.pioneer.PioneerMissionPlaner;
import net.sf.freecol.common.model.ai.missions.scout.ScoutMission;
import net.sf.freecol.common.model.ai.missions.scout.ScoutMissionPlaner;
import net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission;
import net.sf.freecol.common.model.ai.missions.workerrequest.ColonyWorkerMission;
import net.sf.freecol.common.model.ai.missions.workerrequest.ColonyWorkerRequestPlaner;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.util.Predicate;

import java.util.List;

import static net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission.hasNotTransportUnitMission;
import static net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission.isFromEurope;
import static net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission.isFromTileLocation;
import static net.sf.freecol.common.util.PredicateUtil.and;

public class EuropeanMissionPlaner {

	private final TransportGoodsToSellMissionPlaner transportGoodsToSellMissionPlaner;
	private final ScoutMissionPlaner scoutMissionPlaner;
	private final PioneerMissionPlaner pioneerMissionPlaner;
	private final Game game;
	private final PathFinder pathFinder;
	private final PathFinder pathFinder2;

	public EuropeanMissionPlaner(Game game, PathFinder pathFinder, PathFinder pathFinder2) {
		this.transportGoodsToSellMissionPlaner = new TransportGoodsToSellMissionPlaner(game, pathFinder);
		this.scoutMissionPlaner = new ScoutMissionPlaner(game, pathFinder, pathFinder2);
		this.pioneerMissionPlaner = new PioneerMissionPlaner(game, pathFinder);
		this.game = game;
		this.pathFinder = pathFinder;
		this.pathFinder2 = pathFinder2;
	}

	public void prepareMissions(Player player, PlayerMissionsContainer playerMissionContainer) {
		scoutMissionPlaner.prepareMission(player, playerMissionContainer);
		pioneerMissionPlaner.prepareMission(player, playerMissionContainer);

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
	
	protected void navyUnitPlaner(Unit navyUnit, PlayerMissionsContainer playerMissionContainer) {
		if (playerMissionContainer.isUnitBlockedForMission(navyUnit)) {
			return;
		}
		MissionPlanStatus status;

		status = transportContainedUnits(navyUnit, playerMissionContainer);
		if (status == MissionPlanStatus.MISSION_CREATED) {
			return;
		}

		if (navyUnit.isAtEuropeLocation()) {
			status = transportUnitFromEurope(navyUnit, playerMissionContainer);
			if (status == MissionPlanStatus.MISSION_CREATED) {
				return;
			}
		}

		if (navyUnit.isAtTileLocation()) {
			status = prepareTransportForScoutUnits(navyUnit, playerMissionContainer);
			if (status == MissionPlanStatus.MISSION_CREATED) {
				return;
			}

			TransportUnitMission tum = null;
			tum = createTransportMissionForCondition(
				tum,
				navyUnit,
				playerMissionContainer,
				and(hasNotTransportUnitMission, isFromTileLocation)
			);
			if (tum != null) {
				playerMissionContainer.addMission(tum);
				return;
			}

			status = transportGoodsToSellMissionPlaner.plan(navyUnit);
			if (status == MissionPlanStatus.MISSION_CREATED) {
				return;
			}

			status = transportGoodsToSellMissionPlaner.planSellGoodsToBuyUnit(navyUnit);
			if (status == MissionPlanStatus.MISSION_CREATED) {
				return;
			}

			status = transportUnitFromEurope(navyUnit, playerMissionContainer);
			if (status == MissionPlanStatus.MISSION_CREATED) {
				return;
			}

			// one turn mission
			prepareExploreMissions(navyUnit, playerMissionContainer);
		}
	}

	private MissionPlanStatus prepareTransportForScoutUnits(Unit navyUnit, PlayerMissionsContainer playerMissionContainer) {
		ScoutMission scoutMission = playerMissionContainer.findFirstMission(ScoutMission.class);
		if (scoutMission != null
			&& scoutMission.isWaitingForTransport()
			&& TransportUnitMission.isUnitExistsOnTransportMission(playerMissionContainer, scoutMission.getScout())
		) {
			TransportUnitMission transportUnitMission = new TransportUnitMission(navyUnit);
			transportUnitMission.addUnitDest(scoutMission.getScout(), scoutMission.getScoutDistantDestination(), true);
			playerMissionContainer.addMission(transportUnitMission);
			return MissionPlanStatus.MISSION_CREATED;
		}
		return MissionPlanStatus.NO_MISSION;
	}

	private MissionPlanStatus transportUnitFromEurope(Unit navyUnit, PlayerMissionsContainer playerMissionContainer) {
		List<TransportUnitMission> transportMissions = playerMissionContainer.findMissions(TransportUnitMission.class);
		TransportUnitMission tum = null;

		Europe europe = navyUnit.getOwner().getEurope();

		tum = createTransportMissionForCondition(
			tum,
			navyUnit,
			playerMissionContainer,
			and(hasNotTransportUnitMission, isFromEurope)
		);

        for (Unit dockUnit : europe.getUnits().entities()) {
            if (dockUnit.unitRole.equalsId(UnitRole.SCOUT) && !isUnitExistsOnTransportMission(transportMissions, dockUnit)) {
                ScoutMission scoutMission = playerMissionContainer.findFirstMission(ScoutMission.class, dockUnit);
                if (scoutMission == null) {
                    continue;
                }
                if (scoutMission.isWaitingForTransport() && canEmbarkUnit(navyUnit, tum, dockUnit)) {
					if (tum == null) {
						tum = new TransportUnitMission(navyUnit);
					}
					tum.addUnitDest(dockUnit, scoutMission.getScoutDistantDestination(), true);
					playerMissionContainer.addMission(tum);
                }
            }
        }

		for (Unit dockUnit : europe.getUnits().entities()) {
			if (!Unit.isColonist(dockUnit.unitType, dockUnit.getOwner()) || isUnitExistsOnTransportMission(transportMissions, dockUnit)) {
				continue;
			}
			ColonyWorkerMission colonyWorkerMission = playerMissionContainer.findFirstMission(ColonyWorkerMission.class, dockUnit);

			// should be one mission
			if (colonyWorkerMission != null && canEmbarkUnit(navyUnit, tum, dockUnit)) {
				if (tum == null) {
					tum = new TransportUnitMission(navyUnit);
				}
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
	
	private MissionPlanStatus transportContainedUnits(Unit navyUnit, PlayerMissionsContainer playerMissionContainer) {
		// scenario from beggining of game ship with colonist without transport mission, create colonyWorkerMission
		// and then TransportMission

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

	private TransportUnitMission createTransportMissionForCondition(
		TransportUnitMission tum,
		final Unit navyUnit,
		final PlayerMissionsContainer playerMissionContainer,
		final Predicate<TransportUnitRequestMission> transportUnitRequestMissionPredicate
	) {
		List<TransportUnitRequestMission> transportRequestMissions = playerMissionContainer.findMissions(
			TransportUnitRequestMission.class,
			transportUnitRequestMissionPredicate
		);
		for (TransportUnitRequestMission transportRequestMission : transportRequestMissions) {
			Unit unitToTransport = transportRequestMission.getUnit();
			if (canEmbarkUnit(navyUnit, tum, unitToTransport)) {
				if (tum == null) {
					tum = new TransportUnitMission(navyUnit);
				}
				tum.addUnitDest(transportRequestMission);
				transportRequestMission.setTransportUnitMissionId(tum.getId());
			}
		}
		return tum;
	}

	private boolean canEmbarkUnit(Unit navyUnit, TransportUnitMission mission, Unit unit) {
		if (mission == null) {
			return navyUnit.hasSpaceForAdditionalUnit(unit.unitType);
		}
		return mission.canEmbarkUnit(unit);
	}
}