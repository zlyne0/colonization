package net.sf.freecol.common.model.ai.missions.indian;

import static promitech.colonization.ai.MissionHandlerLogger.logger;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;
import net.sf.freecol.common.model.ai.missions.indian.IndianBringGiftMission.Phase;
import net.sf.freecol.common.model.map.path.Path;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.ai.MissionHandler;
import promitech.colonization.orders.move.MoveContext;
import promitech.colonization.orders.move.MoveService;
import promitech.colonization.screen.map.hud.GUIGameController;
import promitech.colonization.ui.SimpleMessageDialog;
import promitech.colonization.ui.resources.StringTemplate;

public class IndianBringGiftMissionHandler implements MissionHandler<IndianBringGiftMission> {

	private final Game game;
	private final PathFinder pathFinder;
	private final MoveService moveService;
	private final GUIGameController guiGameController;
	
	public IndianBringGiftMissionHandler(Game game, PathFinder pathFinder, MoveService moveService, GUIGameController guiGameController) {
		this.game = game;
		this.pathFinder = pathFinder;
		this.moveService = moveService;
		this.guiGameController = guiGameController;
	}
	
	@Override
	public void handle(PlayerMissionsContainer playerMissionsContainer, IndianBringGiftMission mission) {
		Player indianPlayer = playerMissionsContainer.getPlayer();
		
		logger.debug("player[%s].IndianBringGiftMission start execute", indianPlayer.getId());
		
		if (!mission.canExecuteMission(indianPlayer)) {
			logger.debug("player[%s].IndianBringGiftMission done: can not execute mission", indianPlayer.getId());
			mission.setDone();
			return;
		}
		
		if (mission.isColonyOwnerChanged(game)) {
			mission.changePhase(IndianBringGiftMission.Phase.BACK_TO_SETTLEMENT);
		}
		
		if (!mission.correctTransportUnitLocation()) {
			logger.debug(
				"player[%s].IndianBringGiftMission done: incorrect transport unit[%s] location",
				indianPlayer.getId(), 
				mission.getTransportUnit().getId()
			);
			mission.setDone();
			return;
		}
		
		if (mission.getPhase() == Phase.TAKE_GOODS) {
			executeTakeGoodsPhase(mission);
		}
		
		if (mission.getPhase() == Phase.MOVE_TO_COLONY) {
			executeMoveToColonyPhase(mission);
		}
		
		if (mission.getPhase() == Phase.BACK_TO_SETTLEMENT) {
			executeBackToSettlementPhase(mission);
		}
	}
	
	private void executeTakeGoodsPhase(IndianBringGiftMission mission) {
		Tile unitActualLocation = mission.getTransportUnit().getTile();

		if (unitActualLocation.equalsCoordinates(mission.getIndianSettlement().tile)) {
			mission.getIndianSettlement().getGoodsContainer().decreaseGoodsQuantity(mission.getGift());
			mission.changePhase(Phase.MOVE_TO_COLONY);
			return;
		}
		if (!mission.getTransportUnit().hasMovesPoints()) {
			return;
		}
		logger.debug(
			"player[%s].IndianBringGiftMission move to settlement[%s] take goods",
			mission.getTransportUnit().getOwner().getId(), 
			mission.getIndianSettlement().getId()
		);
		
		Path path = pathFinder.findToTile(game.map, unitActualLocation, 
			mission.getIndianSettlement().tile, 
			mission.getTransportUnit(),
			PathFinder.includeUnexploredTiles
		);
		if (path.isReachedDestination()) {
			MoveContext moveContext = new MoveContext(mission.getTransportUnit(), path);
			moveService.aiConfirmedMovePath(moveContext);
		}
	}
	
	private void executeMoveToColonyPhase(IndianBringGiftMission mission) {
		if (!mission.getTransportUnit().hasMovesPoints()) {
			return;
		}
		Tile unitActualLocation = mission.getTransportUnit().getTile();
		
		logger.debug(
			"player[%s].IndianBringGiftMission move to colony[%s]",
			mission.getTransportUnit().getOwner().getId(), 
			mission.getDestinationColony().getId()
		);
		
		Path path = pathFinder.findToTile(game.map, unitActualLocation, 
			mission.getDestinationColony().tile, 
			mission.getTransportUnit(),
			PathFinder.includeUnexploredTiles
		);
		if (path.isReachedDestination()) {
			MoveContext moveContext = new MoveContext(mission.getTransportUnit(), path);
			
			if (path.moveStepDest().equalsCoordinates(mission.getDestinationColony().tile)) {
				moveContext.initNextPathStep();
				moveService.showMoveIfRequired(moveContext);

				mission.transferGoodsToColony();
				
				logger.debug("player[%s].IndianBringGiftMission deliver gift[%s:%s] to colony[%s]",
					mission.getIndianSettlement().getOwner().getId(),
					mission.getGift().getTypeId(),
					mission.getGift().getQuantity(),
					mission.getDestinationColony().getId()
				);
				showGiftConfirmation(mission);
			} else {
				moveService.aiConfirmedMovePath(moveContext);
			}
		}
	}
	
	private void showGiftConfirmation(IndianBringGiftMission mission) {
		if (mission.getDestinationColony().getOwner().isAi()) {
			return;
		}
				
    	StringTemplate st =  StringTemplate.template("model.unit.gift")
    		.addStringTemplate("%player%", mission.getTransportUnit().getOwner().getNationName())
    		.addAmount("%amount%", mission.getGift().getQuantity())
    		.addName("%type%", Specification.instance.goodsTypes.getById(mission.getGift().getTypeId()))
    		.add("%settlement%", mission.getDestinationColony().getName());
		
		guiGameController.showDialogBlocked(new SimpleMessageDialog()
			.withContent(st)
			.withButton("ok")
		);
	}
	
	private void executeBackToSettlementPhase(IndianBringGiftMission mission) {
		Tile unitActualLocation = mission.getTransportUnit().getTile();
		
		if (unitActualLocation.equalsCoordinates(mission.getIndianSettlement().tile)) {
			mission.backUnitToSettlement();
			mission.setDone();
			return;
		}
		if (!mission.getTransportUnit().hasMovesPoints()) {
			return;
		}
		logger.debug(
			"IndianBringGiftMission[%s] back to settlement[%s]", 
			mission.getTransportUnit().getOwner().getId(), 
			mission.getIndianSettlement().getId()
		);
		
		Path path = pathFinder.findToTile(game.map, unitActualLocation, 
			mission.getIndianSettlement().tile, 
			mission.getTransportUnit(),
			PathFinder.includeUnexploredTiles
		);
		if (path.isReachedDestination()) {
			MoveContext moveContext = new MoveContext(mission.getTransportUnit(), path);
			moveService.aiConfirmedMovePath(moveContext);
		}
	}
	
}
