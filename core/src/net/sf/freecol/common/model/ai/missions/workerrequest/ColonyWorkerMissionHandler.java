package net.sf.freecol.common.model.ai.missions.workerrequest;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.ColonyFactory;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;
import net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.player.Player;

import promitech.colonization.ai.ColonyProductionPlaner;
import promitech.colonization.ai.CommonMissionHandler;
import promitech.colonization.ai.MissionHandler;
import promitech.colonization.orders.BuildColonyOrder;
import promitech.colonization.orders.BuildColonyOrder.OrderStatus;

public class ColonyWorkerMissionHandler implements MissionHandler<ColonyWorkerMission> {

	private static final int NOT_WORTH_EMBARK_RANGE = 5;

	private final Game game;
	private final PathFinder pathFinder;

	public ColonyWorkerMissionHandler(Game game, PathFinder pathFinder) {
		this.game = game;
		this.pathFinder = pathFinder;
	}
	
	@Override
	public void handle(PlayerMissionsContainer playerMissionsContainer, ColonyWorkerMission mission) {
		Player player = playerMissionsContainer.getPlayer();
		
		if (!mission.getUnit().isAtTileLocation()) {
			return;
		}
		
		if (!CommonMissionHandler.isUnitExists(player, mission.getUnit())) {
			mission.setDone();
			return;
		}

		if (mission.isUnitAtDestination()) {
			if (mission.getTile().hasSettlement()) {
				addWorkerToColony(mission);
			} else {
				buildColony(mission);
			}
		} else {
			TransportUnitRequestMission transportUnitRequestMission = new TransportUnitRequestMission(
				mission.getUnit(), mission.getTile(), true, true, NOT_WORTH_EMBARK_RANGE
			);
			playerMissionsContainer.addMission(mission, transportUnitRequestMission);
		}
	}

	private void addWorkerToColony(ColonyWorkerMission mission) {
		Colony colony = mission.getTile().getSettlement().asColony();
		ColonyProductionPlaner.initColonyBuilderUnit(colony, mission.getUnit());
		ColonyProductionPlaner.createPlan(colony);
		
		mission.setDone();
	}

	private void buildColony(ColonyWorkerMission mission) {
		BuildColonyOrder buildColonyOrder = new BuildColonyOrder(game.map);
		OrderStatus check = buildColonyOrder.check(mission.getUnit(), mission.getTile());
		if (check == OrderStatus.OK) {
            ColonyFactory colonyFactory = new ColonyFactory(game, pathFinder);
            colonyFactory.buildColonyByAI(mission.getUnit(), mission.getTile());
            mission.setDone();
		} else {
            if (check == OrderStatus.NO_MOVE_POINTS) {
                // do nothing wait on next turn for move points
            } else {
            	// planer should create new mission for unit
            	mission.setDone();
            }
		}
	}
}
