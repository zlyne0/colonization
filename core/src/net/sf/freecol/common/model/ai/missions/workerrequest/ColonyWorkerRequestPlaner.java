package net.sf.freecol.common.model.ai.missions.workerrequest;

import net.sf.freecol.common.model.Europe;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.util.Predicate;

import promitech.colonization.ai.MissionHandlerLogger;
import promitech.colonization.ai.score.ScoreableObjectsList;
import promitech.colonization.ai.Units;

public class ColonyWorkerRequestPlaner {

	private final Player player;
	private final PlayerMissionsContainer playerMissionContainer;
	private final Game game;
	private final PathFinder pathFinder;
    private WorkerRequestScoreValue tmpWorkerRequestPlace;

	public ColonyWorkerRequestPlaner(Player player, PlayerMissionsContainer playerMissionContainer, Game game, PathFinder pathFinder) {
		this.player = player;
		this.playerMissionContainer = playerMissionContainer;
		this.game = game;
		this.pathFinder = pathFinder;
	}

	/**
	 * Prepare mission for create new colony or put worker in colony.
	 * Unit can be on europe dock, on carrier on tile.
	 */
	public void prepareMissionsAndBuyWorkers() {
		Unit transporter = Units.findCarrier(player);
		if (transporter == null) {
			MissionHandlerLogger.logger.debug("player[%s] no carrier unit", player.getId());
			return;
		}

		EntryPointTurnRange entryPointTurnRange = new EntryPointTurnRange(game.map, pathFinder, player, transporter);
		ColonyWorkerRequestPlaceCalculator placeCalculator = new ColonyWorkerRequestPlaceCalculator(
			player, game.map, entryPointTurnRange
		);

		for (Unit unit : player.units) {
			if (unit.isAtLocation(Tile.class) || unit.isAtLocation(Unit.class) || unit.isAtLocation(Europe.class)) {
				if (playerMissionContainer.isUnitBlockedForMission(unit)) {
					continue;
				}
				if (Unit.isColonist(unit.unitType, unit.getOwner())) {
					ScoreableObjectsList<WorkerRequestScoreValue> tileScore = placeCalculator.score(playerMissionContainer);
					assignExistingUnitToColony(unit, tileScore);
				}
			}
		}

		ColonistsPurchaseRecommendations purchaseRecommendations = new ColonistsPurchaseRecommendations(player, playerMissionContainer);
		purchaseRecommendations.buyRecommendations(placeCalculator, entryPointTurnRange);
	}

	private void assignExistingUnitToColony(Unit unit, ScoreableObjectsList<WorkerRequestScoreValue> tileScore) {
		WorkerRequestScoreValue unitPlace = null;
		WorkerRequestScoreValue freeColonistsPlace = null;

		for (WorkerRequestScoreValue workerRequestScore : tileScore) {
			if (!hasMissionToWorkerRequestPlace(playerMissionContainer, workerRequestScore)) {
				if (unit.unitType.equalsId(workerRequestScore.workerType())) {
					unitPlace = workerRequestScore;
				}
				if (workerRequestScore.workerType().equalsId(UnitType.FREE_COLONIST)) {
					freeColonistsPlace = workerRequestScore;
				}
			}
		}

		WorkerRequestScoreValue place = unitPlace;
		if (place == null) {
			place = freeColonistsPlace;
		}
		if (place != null) {
			ColonyWorkerMission mission = new ColonyWorkerMission(place.location(), unit, place.goodsType());
			playerMissionContainer.addMission(mission);
		}
	}

	private boolean hasMissionToWorkerRequestPlace(PlayerMissionsContainer playerMissionContainer, WorkerRequestScoreValue workerRequestPlace) {
	    try {
	    	this.tmpWorkerRequestPlace = workerRequestPlace;
			return playerMissionContainer.hasMission(ColonyWorkerMission.class, hasMissionPredicate);
		} finally {
			this.tmpWorkerRequestPlace = null;
		}
	}

    private final Predicate<ColonyWorkerMission> hasMissionPredicate = new Predicate<ColonyWorkerMission>() {
        @Override
        public boolean test(ColonyWorkerMission obj) {
            return obj.getTile().equalsCoordinates(tmpWorkerRequestPlace.getLocation())
				&& obj.getGoodsType().equalsId(tmpWorkerRequestPlace.getGoodsType());
        }
    };

}
