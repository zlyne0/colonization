package promitech.colonization.ai;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.ai.missions.ExplorerMission;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;
import net.sf.freecol.common.model.ai.missions.TransportUnitMission;
import net.sf.freecol.common.model.ai.missions.goodsToSell.TransportGoodsToSellMissionPlaner;
import net.sf.freecol.common.model.ai.missions.pioneer.PioneerBuyPlan;
import net.sf.freecol.common.model.ai.missions.pioneer.PioneerMission;
import net.sf.freecol.common.model.ai.missions.pioneer.PioneerMissionPlaner;
import net.sf.freecol.common.model.ai.missions.scout.ScoutBuyPlan;
import net.sf.freecol.common.model.ai.missions.scout.ScoutMission;
import net.sf.freecol.common.model.ai.missions.scout.ScoutMissionPlaner;
import net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission;
import net.sf.freecol.common.model.ai.missions.workerrequest.ColonyWorkerRequestPlaner;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.util.Predicate;

import java.util.List;

import static net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission.hasNotTransportUnitMission;
import static net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission.isAtShipLocation;
import static net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission.isFromEurope;
import static net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission.isFromTileLocation;
import static net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission.isTransportHasParentType;
import static net.sf.freecol.common.util.PredicateUtil.and;

public class EuropeanMissionPlaner {

	private final TransportGoodsToSellMissionPlaner transportGoodsToSellMissionPlaner;
	private final ScoutMissionPlaner scoutMissionPlaner;
	private final PioneerMissionPlaner pioneerMissionPlaner;
	private final ColonyWorkerRequestPlaner colonyWorkerRequestPlaner;
	private final Game game;
	private final PathFinder pathFinder;
	private final PathFinder pathFinder2;

	public EuropeanMissionPlaner(Game game, PathFinder pathFinder, PathFinder pathFinder2) {
		this.transportGoodsToSellMissionPlaner = new TransportGoodsToSellMissionPlaner(game, pathFinder);
		this.scoutMissionPlaner = new ScoutMissionPlaner(game, pathFinder, pathFinder2);
		this.pioneerMissionPlaner = new PioneerMissionPlaner(game, pathFinder);
		this.colonyWorkerRequestPlaner = new ColonyWorkerRequestPlaner(game, pathFinder);

		this.game = game;
		this.pathFinder = pathFinder;
		this.pathFinder2 = pathFinder2;
	}

	public void prepareMissions(Player player, PlayerMissionsContainer playerMissionContainer) {
		scoutMissionPlaner.createMissionFromUnusedUnits(player, playerMissionContainer);
		colonyWorkerRequestPlaner.createMissionFromUnusedUnits(player, playerMissionContainer);

		ScoutBuyPlan scoutBuyPlan = scoutMissionPlaner.createBuyPlan(player, playerMissionContainer);
		if (scoutBuyPlan != null) {
			scoutMissionPlaner.handleBuyPlan(scoutBuyPlan, player, playerMissionContainer);
		}
		PioneerBuyPlan pioneerBuyPlan = pioneerMissionPlaner.createBuyPlan(player, playerMissionContainer);
		if (pioneerBuyPlan != null) {
			pioneerMissionPlaner.handlePioneerBuyPlan(pioneerBuyPlan, player, playerMissionContainer);
		}

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
			// scenario from beggining of game ship with colonist without transport mission, create colonyWorkerMission
			// and then TransportMission
			tum = createTransportMissionFromTransportRequest(
				tum,
				navyUnit,
				playerMissionContainer,
				and(hasNotTransportUnitMission, isAtShipLocation(navyUnit))
			);
			if (tum != null) {
				playerMissionContainer.addMission(tum);
				return;
			}

			tum = createTransportMissionFromTransportRequest(
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
		TransportUnitMission tum = null;

		tum = createTransportMissionFromTransportRequest(
			tum,
			navyUnit,
			playerMissionContainer,
			and(hasNotTransportUnitMission, isFromEurope, isTransportHasParentType(playerMissionContainer, ScoutMission.class))
		);
		tum = createTransportMissionFromTransportRequest(
			tum,
			navyUnit,
			playerMissionContainer,
			and(hasNotTransportUnitMission, isFromEurope, isTransportHasParentType(playerMissionContainer, PioneerMission.class))
		);
		tum = createTransportMissionFromTransportRequest(
			tum,
			navyUnit,
			playerMissionContainer,
			and(hasNotTransportUnitMission, isFromEurope)
		);

		colonyWorkerRequestPlaner.buyUnitsToNavyCapacity(playerMissionContainer.getPlayer(), playerMissionContainer, navyUnit);

		tum = createTransportMissionFromTransportRequest(
			tum,
			navyUnit,
			playerMissionContainer,
			and(hasNotTransportUnitMission, isFromEurope)
		);

		if (tum != null) {
			playerMissionContainer.addMission(tum);
			return MissionPlanStatus.MISSION_CREATED;
		}
		return MissionPlanStatus.NO_MISSION;
	}

	private void prepareExploreMissions(Unit navyUnit, PlayerMissionsContainer playerMissionContainer) {
		if (navyUnit.getTileLocationOrNull() != null) {
			ExplorerMission explorerMission = new ExplorerMission(navyUnit);
			playerMissionContainer.addMission(explorerMission);
		}
	}

	public TransportUnitMission createTransportMissionFromTransportRequest(
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
			if (canEmbarkUnit(navyUnit, tum, unitToTransport) || isAlreadyEmbarked(navyUnit, unitToTransport)) {
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

	private boolean isAlreadyEmbarked(Unit navyUnit, Unit unit) {
		return navyUnit.getUnitContainer().isContainUnit(unit);
	}
}