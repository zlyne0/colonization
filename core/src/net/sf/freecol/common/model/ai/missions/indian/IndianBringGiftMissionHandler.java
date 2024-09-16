package net.sf.freecol.common.model.ai.missions.indian;

import static promitech.colonization.ai.MissionHandlerLogger.logger;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.IndianSettlement;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;
import net.sf.freecol.common.model.ai.missions.indian.IndianBringGiftMission.Phase;
import net.sf.freecol.common.model.map.path.Path;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.AbstractGoods;

import promitech.colonization.ai.CommonMissionHandler;
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

		Unit unit = indianPlayer.units.getByIdOrNull(mission.getTransportUnitId());
		if (unit == null || !CommonMissionHandler.isUnitExists(indianPlayer, unit)) {
			logger.debug("player[%s].DemandTributeMission[%s] no unit or destination. Set done.", indianPlayer.getId(), mission.getId());
			mission.setDone();
			return;
		}

		if (!mission.canExecuteMission(indianPlayer)) {
			logger.debug("player[%s].IndianBringGiftMission done: can not execute mission", indianPlayer.getId());
			mission.setDone();
			return;
		}
		
		if (mission.isColonyOwnerChanged(game)) {
			mission.changePhase(IndianBringGiftMission.Phase.BACK_TO_SETTLEMENT);
		}
		
		if (!mission.correctTransportUnitLocation(unit)) {
			logger.debug(
				"player[%s].IndianBringGiftMission done: incorrect transport unit[%s] location",
				indianPlayer.getId(), 
				unit.getId()
			);
			mission.setDone();
			return;
		}
		
		if (mission.getPhase() == Phase.TAKE_GOODS) {
			executeTakeGoodsPhase(mission, unit);
		}
		
		if (mission.getPhase() == Phase.MOVE_TO_COLONY) {
			executeMoveToColonyPhase(mission, unit);
		}
		
		if (mission.getPhase() == Phase.BACK_TO_SETTLEMENT) {
			executeBackToSettlementPhase(mission, unit);
		}
	}
	
	private void executeTakeGoodsPhase(IndianBringGiftMission mission, Unit transportUnit) {
		Tile unitActualLocation = transportUnit.getTile();

		if (unitActualLocation.equalsCoordinates(mission.getIndianSettlement().tile)) {
			mission.getIndianSettlement().getGoodsContainer().decreaseGoodsQuantity(mission.getGift());
			mission.changePhase(Phase.MOVE_TO_COLONY);
			return;
		}
		if (!transportUnit.hasMovesPoints()) {
			return;
		}
		logger.debug(
			"player[%s].IndianBringGiftMission move to settlement[%s] take goods",
			transportUnit.getOwner().getId(),
			mission.getIndianSettlement().getId()
		);
		
		Path path = pathFinder.findToTile(game.map, unitActualLocation, 
			mission.getIndianSettlement().tile,
			transportUnit,
			PathFinder.includeUnexploredTiles
		);
		if (path.isReachedDestination()) {
			MoveContext moveContext = new MoveContext(transportUnit, path);
			moveService.aiConfirmedMovePath(moveContext);
		}
	}
	
	private void executeMoveToColonyPhase(IndianBringGiftMission mission, Unit transportUnit) {
		if (!transportUnit.hasMovesPoints()) {
			return;
		}
		Tile unitActualLocation = transportUnit.getTile();
		
		logger.debug(
			"player[%s].IndianBringGiftMission move to colony[%s]",
			transportUnit.getOwner().getId(),
			mission.getDestinationColony().getId()
		);
		
		Path path = pathFinder.findToTile(game.map, unitActualLocation, 
			mission.getDestinationColony().tile,
			transportUnit,
			PathFinder.includeUnexploredTiles
		);
		if (path.isReachedDestination()) {
			MoveContext moveContext = new MoveContext(transportUnit, path);
			
			if (path.moveStepDest().equalsCoordinates(mission.getDestinationColony().tile)) {
				moveContext.initNextPathStep();
				moveService.showMoveIfRequired(moveContext);

				AbstractGoods gift = mission.transferGoodsToColony(transportUnit);
				
				logger.debug("player[%s].IndianBringGiftMission deliver gift[%s:%s] to colony[%s]",
					mission.getIndianSettlement().getOwner().getId(),
					gift.getTypeId(),
					gift.getQuantity(),
					mission.getDestinationColony().getId()
				);
				showGiftConfirmation(mission, gift, transportUnit);
			} else {
				moveService.aiConfirmedMovePath(moveContext);
			}
		}
	}
	
	private void showGiftConfirmation(IndianBringGiftMission mission, AbstractGoods gift, Unit transportUnit) {
		if (mission.getDestinationColony().getOwner().isAi()) {
			return;
		}
				
    	StringTemplate st =  StringTemplate.template("model.unit.gift")
    		.addStringTemplate("%player%", transportUnit.getOwner().getNationName())
    		.addAmount("%amount%", gift.getQuantity())
    		.addName("%type%", Specification.instance.goodsTypes.getById(gift.getTypeId()))
    		.add("%settlement%", mission.getDestinationColony().getName());
		
		guiGameController.showDialogBlocked(new SimpleMessageDialog()
			.withContent(st)
			.withButton("ok")
		);
	}
	
	private void executeBackToSettlementPhase(IndianBringGiftMission mission, Unit transportUnit) {
		if (transportUnit.isAtLocation(IndianSettlement.class)) {
			mission.setDone();
			return;
		}
		Tile unitActualLocation = transportUnit.getTile();
		
		if (unitActualLocation.equalsCoordinates(mission.getIndianSettlement().tile)) {
			mission.backUnitToSettlement(transportUnit);
			mission.setDone();
			return;
		}
		if (!transportUnit.hasMovesPoints()) {
			return;
		}
		logger.debug(
			"IndianBringGiftMission[%s] back to settlement[%s]",
			transportUnit.getOwner().getId(),
			mission.getIndianSettlement().getId()
		);
		
		Path path = pathFinder.findToTile(game.map, unitActualLocation, 
			mission.getIndianSettlement().tile,
			transportUnit,
			PathFinder.includeUnexploredTiles
		);
		if (path.isReachedDestination()) {
			MoveContext moveContext = new MoveContext(transportUnit, path);
			moveService.aiConfirmedMovePath(moveContext);
		}
	}
	
}
