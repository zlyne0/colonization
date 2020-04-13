package promitech.colonization.ai;

import static promitech.colonization.ai.MissionHandlerLogger.logger;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.ai.missions.DemandTributeMission;
import net.sf.freecol.common.model.ai.missions.DemandTributeMission.Phase;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;
import net.sf.freecol.common.model.map.path.Path;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.Goods;
import promitech.colonization.orders.move.MoveContext;
import promitech.colonization.orders.move.MoveService;
import promitech.colonization.screen.map.hud.GUIGameController;
import promitech.colonization.ui.QuestionDialog;
import promitech.colonization.ui.QuestionDialog.OptionAction;
import promitech.colonization.ui.SimpleMessageDialog;
import promitech.colonization.ui.SimpleMessageDialog.ButtonActionListener;
import promitech.colonization.ui.resources.StringTemplate;

class DemandTributeMissionHandler implements MissionHandler<DemandTributeMission>{

	private final Game game;
	private final PathFinder pathFinder;
	private final MoveService moveService;
	private final GUIGameController guiGameController;
	
	public DemandTributeMissionHandler(Game game, PathFinder pathFinder, MoveService moveService, GUIGameController guiGameController) {
		this.game = game;
		this.pathFinder = pathFinder;
		this.moveService = moveService;
		this.guiGameController = guiGameController;
	}
	
	@Override
	public void handle(PlayerMissionsContainer playerMissionsContainer, DemandTributeMission mission) {
		Player indianPlayer = playerMissionsContainer.getPlayer();
		
		logger.debug("DemandTributeMission[%s] start execute", indianPlayer.getId());
		
		if (!mission.canExecuteMission(indianPlayer)) {
			logger.debug("DemandTributeMission[%s] done: can not execute mission", indianPlayer.getId());
			mission.setDone();
			return;
		}
		
		if (mission.isColonyOwnerChanged(game)) {
			mission.changePhase(DemandTributeMission.Phase.BACK_TO_SETTLEMENT);
		}
		
		if (!mission.correctTransportUnitLocation()) {
			logger.debug(
				"DemandTributeMission[%s] done: incorrect transport unit[%s] location", 
				indianPlayer.getId(), 
				mission.getUnitToDemandTribute().getId()
			);
			mission.setDone();
			return;
		}
		
		if (mission.getPhase() == Phase.MOVE_TO_COLONY) {
			executeMoveToColonyPhase(mission);
		}
		
		if (mission.getPhase() == Phase.BACK_TO_SETTLEMENT) {
			executeBackToSettlementPhase(mission);
		}
	}
	
	private void executeMoveToColonyPhase(DemandTributeMission mission) {
		if (!mission.getUnitToDemandTribute().hasMovesPoints()) {
			return;
		}
		Tile unitActualLocation = mission.getUnitToDemandTribute().getTile();
		
		logger.debug(
			"DemandTributeMission[%s] move to colony[%s]", 
			mission.getUnitToDemandTribute().getOwner().getId(), 
			mission.getColony().getId()
		);
		
		Path path = pathFinder.findToTile(game.map, unitActualLocation, 
			mission.getColony().tile, 
			mission.getUnitToDemandTribute(), 
			false
		);
		if (path.isReachedDestination()) {
			MoveContext moveContext = new MoveContext(path);
			
			if (path.moveStepDest().equalsCoordinates(mission.getColony().tile)) {
				moveContext.initNextPathStep();
				moveService.showMoveIfRequired(moveContext);

				int gold = 0;
				Goods selectedGoods = mission.selectGoods();
				if (selectedGoods == null) {
					gold = mission.selectGold();
				}
				
				demandConfirmation(selectedGoods, gold, mission);
			} else {
				moveService.aiConfirmedMovePath(moveContext);
			}
		}
	}
	
	private void demandConfirmation(final Goods goods, final int goldAmount, final DemandTributeMission mission) {
		if (mission.getColony().getOwner().isAi()) {
			acceptDemandLogMsg(goods, goldAmount, mission);
			
			mission.acceptDemands(goods, goldAmount);
		} else {
			OptionAction<Goods> acceptDemandsAction = new OptionAction<Goods>() {
				@Override
				public void executeAction(Goods payload) {
					acceptDemandLogMsg(goods, goldAmount, mission);
					
					mission.acceptDemands(goods, goldAmount);
				}
			};
			OptionAction<Goods> rejectDemandsAction = new OptionAction<Goods>() {
				@Override
				public void executeAction(Goods payload) {
					rejectDemandsLogMsg(goods, goldAmount, mission);
					
					mission.rejectDemands();
				}
			};
			
			if (goods != null) {
				if (goods.getType().isFood()) {
			    	StringTemplate st = StringTemplate.template("indianDemand.food.text")
			    		.addStringTemplate("%nation%", mission.getIndianSettlement().getOwner().getNationName())
			    		.add("%colony%", mission.getColony().getName())
			    		.addAmount("%amount%", goods.getAmount());

			    	guiGameController.showDialog(new QuestionDialog(st)
			    		.addAnswer("indianDemand.food.yes", acceptDemandsAction, goods)
			    		.addAnswer("indianDemand.food.no", rejectDemandsAction, goods)
		    		);
				} else {
			    	StringTemplate st = StringTemplate.template("indianDemand.other.text")
			    		.addStringTemplate("%nation%", mission.getIndianSettlement().getOwner().getNationName())
			    		.add("%colony%", mission.getColony().getName())
			    		.addName("%goods%", goods)
			    		.addAmount("%amount%", goods.getAmount());
			    	
			    	guiGameController.showDialog(new QuestionDialog(st)
			    		.addAnswer("indianDemand.other.yes", acceptDemandsAction, goods)
			    		.addAnswer("indianDemand.other.no", rejectDemandsAction, goods)
		    		);
				}
			} else {
		    	StringTemplate st = StringTemplate.template("indianDemand.gold.text")
		    		.addStringTemplate("%nation%", mission.getIndianSettlement().getOwner().getNationName())
		    		.add("%colony%", mission.getColony().getName())
		    		.addAmount("%amount%", goldAmount);

		    	guiGameController.showDialog(new QuestionDialog(st)
		    		.addAnswer("indianDemand.gold.yes", acceptDemandsAction, goods)
		    		.addAnswer("indianDemand.gold.no", rejectDemandsAction, goods)
	    		);
			}
		}
	}

	private void acceptDemandLogMsg(final Goods goods, final int goldAmount, final DemandTributeMission mission) {
		if (logger.isDebug()) {
			logger.debug(
				"DemandTributeMission[%s] player[%s] accept demand %s:%d", 
				mission.getUnitToDemandTribute().getOwner().getId(),
				mission.getColony().getOwner().getId(),
				goods != null ? goods.getType() : "gold",
				goods != null ? goods.getAmount() : goldAmount
			);
		}
	}

	private void rejectDemandsLogMsg(final Goods goods, final int goldAmount, final DemandTributeMission mission) {
		if (logger.isDebug()) {
			logger.debug(
				"DemandTributeMission[%s] player[%s] reject demand %s:%d", 
				mission.getUnitToDemandTribute().getOwner().getId(),
				mission.getColony().getOwner().getId(),
				goods != null ? goods.getType() : "gold",
				goods != null ? goods.getAmount() : goldAmount
			);
		}
	}
	
	private void executeBackToSettlementPhase(DemandTributeMission mission) {
		Tile unitActualLocation = mission.getUnitToDemandTribute().getTile();
		
		if (unitActualLocation.equalsCoordinates(mission.getIndianSettlement().tile)) {
			mission.backUnitToSettlement();
			mission.setDone();
			return;
		}
		if (!mission.getUnitToDemandTribute().hasMovesPoints()) {
			return;
		}
		logger.debug(
			"DemandTributeMission[%s] back to settlement[%s]", 
			mission.getUnitToDemandTribute().getOwner().getId(), 
			mission.getIndianSettlement().getId()
		);
		
		Path path = pathFinder.findToTile(game.map, unitActualLocation, 
			mission.getIndianSettlement().tile, 
			mission.getUnitToDemandTribute(), 
			false
		);
		if (path.isReachedDestination()) {
			MoveContext moveContext = new MoveContext(path);
			moveService.aiConfirmedMovePath(moveContext);
		}
	}
	
}
