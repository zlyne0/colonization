package promitech.colonization.ai;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;
import net.sf.freecol.common.model.ai.missions.TransportUnitMission;
import net.sf.freecol.common.model.ai.missions.goodsToSell.TransportGoodsToSellMissionPlaner;
import net.sf.freecol.common.model.ai.missions.pioneer.PioneerBuyPlan;
import net.sf.freecol.common.model.ai.missions.pioneer.PioneerMission;
import net.sf.freecol.common.model.ai.missions.pioneer.PioneerMissionPlaner;
import net.sf.freecol.common.model.ai.missions.pioneer.RequestGoodsMission;
import net.sf.freecol.common.model.ai.missions.scout.ScoutBuyPlan;
import net.sf.freecol.common.model.ai.missions.scout.ScoutMission;
import net.sf.freecol.common.model.ai.missions.scout.ScoutMissionPlaner;
import net.sf.freecol.common.model.ai.missions.workerrequest.ColonyWorkerRequestPlaner;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.player.Player;

import java.util.List;

import promitech.colonization.screen.debug.BuyShipOrder;
import promitech.colonization.screen.debug.BuyShipPlaner;

import static net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission.hasNotTransportUnitMission;
import static net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission.isAtShipLocation;
import static net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission.isFromEurope;
import static net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission.isFromTileLocation;
import static net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission.isTransportHasParentType;
import static net.sf.freecol.common.util.PredicateUtil.and;

/*
   Zadaniem człowieka jest życie, nie zaś egzystencja.
   Nie będę tracić dni na próbę ich przedłużenia.
   Wykorzystam swój czas.
 */
public class EuropeanMissionPlaner {

	private final TransportGoodsToSellMissionPlaner transportGoodsToSellMissionPlaner;
	private final ScoutMissionPlaner scoutMissionPlaner;
	private final PioneerMissionPlaner pioneerMissionPlaner;
	private final ColonyWorkerRequestPlaner colonyWorkerRequestPlaner;
	private final BuyGoodsPlaner buyGoodsPlaner;
	private final Game game;
	private final PathFinder pathFinder;
	private final PathFinder pathFinder2;
	private boolean avoidPurchasesAndCollectGold = false;

	private final NavyMissionPlaner navyMissionPlaner = new NavyMissionPlaner();

	public EuropeanMissionPlaner(Game game, PathFinder pathFinder, PathFinder pathFinder2) {
		this.transportGoodsToSellMissionPlaner = new TransportGoodsToSellMissionPlaner(game, pathFinder);
		this.scoutMissionPlaner = new ScoutMissionPlaner(game, pathFinder, pathFinder2);
		this.pioneerMissionPlaner = new PioneerMissionPlaner(game, pathFinder);
		this.colonyWorkerRequestPlaner = new ColonyWorkerRequestPlaner(game, pathFinder);
		this.buyGoodsPlaner = new BuyGoodsPlaner(game);

		this.game = game;
		this.pathFinder = pathFinder;
		this.pathFinder2 = pathFinder2;
	}

	public void prepareMissions(Player player, PlayerMissionsContainer playerMissionContainer) {
		avoidPurchasesAndCollectGold = false;

		scoutMissionPlaner.createMissionFromUnusedUnits(player, playerMissionContainer);
		colonyWorkerRequestPlaner.createMissionFromUnusedUnits(player, playerMissionContainer);

		handlePurchases(player, playerMissionContainer);

		List<Unit> navyUnitsList = navyMissionPlaner.createNavyUnitsList(player, playerMissionContainer);
		for (Unit unit : navyUnitsList) {
			navyUnitPlaner(unit, playerMissionContainer);
		}

		new ColonyProductionPlaner().generateAndSetColonyProductionPlan(player);
	}

	private void handlePurchases(Player player, PlayerMissionsContainer playerMissionContainer) {
		BuyShipPlaner buyShipPlaner = new BuyShipPlaner(player, Specification.instance, transportGoodsToSellMissionPlaner, playerMissionContainer);
		BuyShipOrder buyShipPlan = buyShipPlaner.createBuyShipPlan();
		if (buyShipPlan instanceof BuyShipOrder.CollectGoldAndBuy) {
			avoidPurchasesAndCollectGold = true;
			return;
		}
		buyShipPlaner.handleBuyOrders(buyShipPlan);

		ScoutBuyPlan scoutBuyPlan = scoutMissionPlaner.createBuyPlan(player, playerMissionContainer);
		if (scoutBuyPlan != null) {
			scoutMissionPlaner.handleBuyPlan(scoutBuyPlan, player, playerMissionContainer);
		}
		PioneerBuyPlan pioneerBuyPlan = pioneerMissionPlaner.createBuyPlan(player, playerMissionContainer);
		if (pioneerBuyPlan != null) {
			pioneerMissionPlaner.handlePioneerBuyPlan(pioneerBuyPlan, playerMissionContainer);
		}
		buyGoodsPlaner.handleBuyOrders(player, playerMissionContainer);
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
			if (status == MissionPlanStatus.NO_MISSION) {
				navyTransportPlanerNotDependLocation(navyUnit, playerMissionContainer);
			}
		}

		if (navyUnit.isAtTileLocation()) {
			status = navyMissionPlaner.navyTransportPioneerFromTileToTile(navyUnit, playerMissionContainer);
			if (status == MissionPlanStatus.MISSION_CREATED) {
				return;
			}

			status = navyTransportPlanerNotDependLocation(navyUnit, playerMissionContainer);
			if (status == MissionPlanStatus.MISSION_CREATED) {
				return;
			}

			status = transportUnitFromEurope(navyUnit, playerMissionContainer);
			if (status == MissionPlanStatus.MISSION_CREATED) {
				return;
			}

			// one turn mission
			navyMissionPlaner.prepareExploreMissions(navyUnit, playerMissionContainer);
		}
	}

	private MissionPlanStatus navyTransportPlanerNotDependLocation(Unit navyUnit, PlayerMissionsContainer playerMissionContainer) {
		MissionPlanStatus status;

		TransportUnitMission tum = null;
		// scenario from beggining of game ship with colonist without transport mission, create colonyWorkerMission
		// and then TransportMission
		tum = navyMissionPlaner.createTransportMissionFromTransportRequest(
			tum,
			navyUnit,
			playerMissionContainer,
			and(hasNotTransportUnitMission, isAtShipLocation(navyUnit))
		);
		if (tum != null) {
			playerMissionContainer.addMission(tum);
			return MissionPlanStatus.MISSION_CREATED;
		}

		status = transportGoodsToSellMissionPlaner.plan(navyUnit);
		if (status == MissionPlanStatus.MISSION_CREATED) {
			return status;
		}

		tum = navyMissionPlaner.createTransportMissionFromTransportRequest(
			tum,
			navyUnit,
			playerMissionContainer,
			and(hasNotTransportUnitMission, isFromTileLocation)
		);
		if (tum != null) {
			playerMissionContainer.addMission(tum);
			return MissionPlanStatus.MISSION_CREATED;
		}

		status = transportGoodsToSellMissionPlaner.planSellGoodsToBuyUnit(navyUnit);
		if (status == MissionPlanStatus.MISSION_CREATED) {
			return status;
		}
		return status;
	}

	protected MissionPlanStatus transportUnitFromEurope(Unit navyUnit, PlayerMissionsContainer playerMissionContainer) {
		TransportUnitMission tum = null;

		if (navyUnit.isAtEuropeLocation()) {
			tum = createTransportMissionFromRequestGoodsMission(tum, navyUnit, playerMissionContainer);
		}

		tum = navyMissionPlaner.createTransportMissionFromTransportRequest(
			tum,
			navyUnit,
			playerMissionContainer,
			and(hasNotTransportUnitMission, isFromEurope, isTransportHasParentType(playerMissionContainer, ScoutMission.class))
		);
		tum = navyMissionPlaner.createTransportMissionFromTransportRequest(
			tum,
			navyUnit,
			playerMissionContainer,
			and(hasNotTransportUnitMission, isFromEurope, isTransportHasParentType(playerMissionContainer, PioneerMission.class))
		);
		tum = navyMissionPlaner.createTransportMissionFromTransportRequest(
			tum,
			navyUnit,
			playerMissionContainer,
			and(hasNotTransportUnitMission, isFromEurope)
		);

		if (!avoidPurchasesAndCollectGold) {
			colonyWorkerRequestPlaner.buyUnitsToNavyCapacity(playerMissionContainer.getPlayer(), playerMissionContainer, navyUnit);
			tum = navyMissionPlaner.createTransportMissionFromTransportRequest(
				tum,
				navyUnit,
				playerMissionContainer,
				and(hasNotTransportUnitMission, isFromEurope)
			);
		}

		if (tum != null) {
			playerMissionContainer.addMission(tum);
			return MissionPlanStatus.MISSION_CREATED;
		}
		return MissionPlanStatus.NO_MISSION;
	}

	private TransportUnitMission createTransportMissionFromRequestGoodsMission(
		TransportUnitMission tum,
		final Unit navyUnit,
		final PlayerMissionsContainer playerMissionContainer
	) {
		List<RequestGoodsMission> transportRequestMissions = playerMissionContainer.findMissions(
			RequestGoodsMission.class,
			RequestGoodsMission.isBoughtPredicate
		);

		for (RequestGoodsMission transportRequestMission : transportRequestMissions) {
			if (navyUnit.hasSpaceForAdditionalCargo(transportRequestMission.getGoodsCollection())) {
				if (tum == null) {
					tum = new TransportUnitMission(navyUnit);
				}
				if (tum.isNotLoaded(transportRequestMission)) {
					tum.addCargoDest(navyUnit.getOwner(), transportRequestMission);
				}
			}
		}
		return tum;
	}

	protected void setAvoidPurchasesAndCollectGold() {
		avoidPurchasesAndCollectGold = true;
	}
}