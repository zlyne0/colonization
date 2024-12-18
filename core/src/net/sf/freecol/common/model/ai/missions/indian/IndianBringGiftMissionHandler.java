package net.sf.freecol.common.model.ai.missions.indian;

import static promitech.colonization.ai.MissionHandlerLogger.logger;

import net.sf.freecol.common.model.Colony;
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
		IndianSettlement indianSettlement = indianPlayer.settlements.getById(mission.getIndianSettlementId()).asIndianSettlement();

		if (mission.isColonyOwnerChanged(game)) {
			mission.changePhase(IndianBringGiftMission.Phase.BACK_TO_SETTLEMENT);
		}
		
		if (!mission.correctTransportUnitLocation(unit, indianSettlement)) {
			logger.debug(
				"player[%s].IndianBringGiftMission done: incorrect transport unit[%s] location",
				indianPlayer.getId(), 
				unit.getId()
			);
			mission.setDone();
			return;
		}
		
		if (mission.getPhase() == Phase.TAKE_GOODS) {
			executeTakeGoodsPhase(mission, unit, indianSettlement);
		}
		
		if (mission.getPhase() == Phase.MOVE_TO_COLONY) {
			executeMoveToColonyPhase(mission, unit, mission.getDestinationColony(game), indianSettlement);
		}
		
		if (mission.getPhase() == Phase.BACK_TO_SETTLEMENT) {
			executeBackToSettlementPhase(mission, unit, indianSettlement);
		}
	}
	
	private void executeTakeGoodsPhase(IndianBringGiftMission mission, Unit transportUnit, IndianSettlement indianSettlement) {
		Tile unitActualLocation = transportUnit.getTile();

		if (unitActualLocation.equalsCoordinates(indianSettlement.tile)) {
			indianSettlement.getGoodsContainer().decreaseGoodsQuantity(mission.getGift());
			mission.changePhase(Phase.MOVE_TO_COLONY);
			return;
		}
		if (!transportUnit.hasMovesPoints()) {
			return;
		}
		logger.debug(
			"player[%s].IndianBringGiftMission move to settlement[%s] take goods",
			transportUnit.getOwner().getId(),
			indianSettlement.getId()
		);
		
		Path path = pathFinder.findToTile(game.map, unitActualLocation, 
			indianSettlement.tile,
			transportUnit,
			PathFinder.includeUnexploredTiles
		);
		if (path.isReachedDestination()) {
			MoveContext moveContext = new MoveContext(transportUnit, path);
			moveService.aiConfirmedMovePath(moveContext);
		}
	}
	
	private void executeMoveToColonyPhase(IndianBringGiftMission mission, Unit transportUnit, Colony destinationColony, IndianSettlement indianSettlement) {
		if (!transportUnit.hasMovesPoints()) {
			return;
		}
		Tile unitActualLocation = transportUnit.getTile();

		logger.debug(
			"player[%s].IndianBringGiftMission move to colony[%s]",
			transportUnit.getOwner().getId(),
			destinationColony.getId()
		);
		
		Path path = pathFinder.findToTile(game.map, unitActualLocation,
			destinationColony.tile,
			transportUnit,
			PathFinder.includeUnexploredTiles
		);
		if (path.isReachedDestination()) {
			MoveContext moveContext = new MoveContext(transportUnit, path);
			
			if (path.moveStepDest().equalsCoordinates(destinationColony.tile)) {
				moveContext.initNextPathStep();
				moveService.showMoveIfRequired(moveContext);

				AbstractGoods gift = mission.transferGoodsToColony(transportUnit, destinationColony);
				
				logger.debug("player[%s].IndianBringGiftMission deliver gift[%s:%s] to colony[%s]",
					indianSettlement.getOwner().getId(),
					gift.getTypeId(),
					gift.getQuantity(),
					destinationColony.getId()
				);
				showGiftConfirmation(destinationColony, gift, transportUnit);
			} else {
				moveService.aiConfirmedMovePath(moveContext);
			}
		}
	}
	
	private void showGiftConfirmation(Colony destinationColony, AbstractGoods gift, Unit transportUnit) {
		if (destinationColony.getOwner().isAi()) {
			return;
		}
				
    	StringTemplate st =  StringTemplate.template("model.unit.gift")
    		.addStringTemplate("%player%", transportUnit.getOwner().getNationName())
    		.addAmount("%amount%", gift.getQuantity())
    		.addName("%type%", Specification.instance.goodsTypes.getById(gift.getTypeId()))
    		.add("%settlement%", destinationColony.getName());
		
		guiGameController.showDialogBlocked(new SimpleMessageDialog()
			.withContent(st)
			.withButton("ok")
		);
	}
	
	private void executeBackToSettlementPhase(IndianBringGiftMission mission, Unit transportUnit, IndianSettlement indianSettlement) {
		if (transportUnit.isAtLocation(IndianSettlement.class)) {
			mission.setDone();
			return;
		}
		Tile unitActualLocation = transportUnit.getTile();
		
		if (unitActualLocation.equalsCoordinates(indianSettlement.tile)) {
			mission.backUnitToSettlement(transportUnit, indianSettlement);
			mission.setDone();
			return;
		}
		if (!transportUnit.hasMovesPoints()) {
			return;
		}
		logger.debug(
			"IndianBringGiftMission[%s] back to settlement[%s]",
			transportUnit.getOwner().getId(),
			mission.getIndianSettlementId()
		);
		
		Path path = pathFinder.findToTile(game.map, unitActualLocation, 
			indianSettlement.tile,
			transportUnit,
			PathFinder.includeUnexploredTiles
		);
		if (path.isReachedDestination()) {
			MoveContext moveContext = new MoveContext(transportUnit, path);
			moveService.aiConfirmedMovePath(moveContext);
		}
	}
	
}
