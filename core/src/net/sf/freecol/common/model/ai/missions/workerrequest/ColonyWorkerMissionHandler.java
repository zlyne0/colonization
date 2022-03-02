package net.sf.freecol.common.model.ai.missions.workerrequest;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.ColonyFactory;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;
import net.sf.freecol.common.model.map.path.Path;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.ai.ColonyProductionPlaner;
import promitech.colonization.ai.CommonMissionHandler;
import promitech.colonization.ai.MissionHandler;
import promitech.colonization.orders.BuildColonyOrder;
import promitech.colonization.orders.BuildColonyOrder.OrderStatus;
import promitech.colonization.orders.move.MoveContext;
import promitech.colonization.orders.move.MoveService;

public class ColonyWorkerMissionHandler implements MissionHandler<ColonyWorkerMission> {

	private final Game game;
	private final PathFinder pathFinder;
	private final MoveService moveService;

	public ColonyWorkerMissionHandler(Game game, PathFinder pathFinder, MoveService moveService) {
		this.game = game;
		this.pathFinder = pathFinder;
		this.moveService = moveService;
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
				buildColony(playerMissionsContainer, mission);
			}
		} else {
			moveToDestination(playerMissionsContainer, mission);
			if (mission.isUnitAtDestination()) {
				buildColony(playerMissionsContainer, mission);
			}
		}
	}
	
	private void moveToDestination(PlayerMissionsContainer playerMissionsContainer, ColonyWorkerMission mission) {
        Path path = pathFinder.findToTile(
            game.map,
            mission.getUnit().getTile(),
            mission.getTile(),
            mission.getUnit(),
            PathFinder.includeUnexploredTiles
        );
        if (path.reachTile(mission.getTile())) {
            MoveContext moveContext = new MoveContext(mission.getUnit(), path);
            moveContext.initNextPathStep();
            moveService.handlePathMoveContext(moveContext);
        }
	}

	private void addWorkerToColony(ColonyWorkerMission mission) {
		Colony colony = mission.getTile().getSettlement().asColony();
		ColonyProductionPlaner.initColonyBuilderUnit(colony, mission.getUnit());
		ColonyProductionPlaner.createPlan(colony);
		
		mission.setDone();
	}

	private void buildColony(PlayerMissionsContainer playerMissionsContainer, ColonyWorkerMission mission) {
		BuildColonyOrder buildColonyOrder = new BuildColonyOrder(game.map);
		OrderStatus check = buildColonyOrder.check(mission.getUnit(), mission.getTile());
		if (check == OrderStatus.OK) {
            ColonyFactory colonyFactory = new ColonyFactory(game, pathFinder);
            colonyFactory.buildColonyByAI(mission.getUnit(), mission.getTile());
            
            playerMissionsContainer.unblockUnitsFromMission(mission);
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
