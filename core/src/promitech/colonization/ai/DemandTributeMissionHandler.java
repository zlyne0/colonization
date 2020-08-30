package promitech.colonization.ai;

import static promitech.colonization.ai.MissionHandlerLogger.logger;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.MoveType;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.ai.missions.DemandTributeMission;
import net.sf.freecol.common.model.ai.missions.DemandTributeMission.Phase;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;
import net.sf.freecol.common.model.map.path.Path;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.Goods;
import promitech.colonization.orders.combat.CombatService;
import promitech.colonization.orders.move.MoveContext;
import promitech.colonization.orders.move.MoveService;
import promitech.colonization.screen.map.hud.GUIGameController;
import promitech.colonization.ui.QuestionDialog;
import promitech.colonization.ui.QuestionDialog.OptionAction;
import promitech.colonization.ui.resources.StringTemplate;

class DemandTributeMissionHandler implements MissionHandler<DemandTributeMission>{

	private final Game game;
	private final PathFinder pathFinder;
	private final MoveService moveService;
	private final CombatService combatService;
	private final GUIGameController guiGameController;
	
	public DemandTributeMissionHandler(Game game, PathFinder pathFinder, 
		MoveService moveService, 
		CombatService combatService,
		GUIGameController guiGameController
	) {
		this.game = game;
		this.pathFinder = pathFinder;
		this.moveService = moveService;
		this.combatService = combatService;
		this.guiGameController = guiGameController;
	}
	
	@Override
	public void handle(PlayerMissionsContainer playerMissionsContainer, DemandTributeMission mission) {
		Player indianPlayer = playerMissionsContainer.getPlayer();
		
		if (!mission.canExecuteMission(indianPlayer)) {
			logger.debug("player[%s] Can not execute DemandTributeMission[%s]. Set done.", indianPlayer.getId(), mission.getId());
			mission.setDone();
			return;
		}
		
		if (mission.isColonyOwnerChanged(game)) {
			mission.changePhase(DemandTributeMission.Phase.BACK_TO_SETTLEMENT);
		}
		
		if (!mission.correctTransportUnitLocation()) {
			logger.debug(
				"player[%s] DemandTributeMission[%s]. Incorrect transport unit[%s] location. Set done.", 
				indianPlayer.getId(), 
				mission.getId(),
				mission.getUnitToDemandTribute().getId()
			);
			mission.setDone();
			return;
		}
		
		if (mission.getPhase() == Phase.MOVE_TO_COLONY) {
			executeMoveToColonyPhase(mission);
		}
		
		if (mission.getPhase() == Phase.ATTACK) {
			MoveContext moveContext = new MoveContext(
				mission.getUnitToDemandTribute().getTile(), 
				mission.getColony().tile, 
				mission.getUnitToDemandTribute()
			);
			
	        if (moveContext.canHandleMove() &&
	            (moveContext.isMoveType(MoveType.ATTACK_UNIT) 
	            		|| moveContext.isMoveType(MoveType.ATTACK_SETTLEMENT)
        		)
    		) {
	        	combatService.doCombat(moveContext);
	        	mission.backToSettlementAfterSuccessfulAtack();
	        	return;
            }
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
			"player[%s].DemandTributeMission[%s] move to colony[%s]",
			mission.getIndianSettlement().getOwner().getId(),
			mission.getId(),
			mission.getColony().getId()
		);
		
		Path path = pathFinder.findToTile(game.map, unitActualLocation, 
			mission.getColony().tile, 
			mission.getUnitToDemandTribute(), 
			PathFinder.includeUnexploredTiles
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
			mission.acceptDemands(goods, goldAmount);
		} else {
			OptionAction<Goods> acceptDemandsAction = new OptionAction<Goods>() {
				@Override
				public void executeAction(Goods payload) {
					mission.acceptDemands(goods, goldAmount);
				}
			};
			OptionAction<Goods> rejectDemandsAction = new OptionAction<Goods>() {
				@Override
				public void executeAction(Goods payload) {
					mission.rejectDemands(goods, goldAmount);
				}
			};
			
			if (goods != null) {
				if (goods.getType().isFood()) {
			    	StringTemplate st = StringTemplate.template("indianDemand.food.text")
			    		.addStringTemplate("%nation%", mission.getIndianSettlement().getOwner().getNationName())
			    		.add("%colony%", mission.getColony().getName())
			    		.addAmount("%amount%", goods.getAmount());

			    	guiGameController.showDialogBlocked(new QuestionDialog(st)
			    		.addAnswer("indianDemand.food.yes", acceptDemandsAction, goods)
			    		.addAnswer("indianDemand.food.no", rejectDemandsAction, goods)
		    		);
				} else {
			    	StringTemplate st = StringTemplate.template("indianDemand.other.text")
			    		.addStringTemplate("%nation%", mission.getIndianSettlement().getOwner().getNationName())
			    		.add("%colony%", mission.getColony().getName())
			    		.addName("%goods%", goods)
			    		.addAmount("%amount%", goods.getAmount());
			    	
			    	guiGameController.showDialogBlocked(new QuestionDialog(st)
			    		.addAnswer("indianDemand.other.yes", acceptDemandsAction, goods)
			    		.addAnswer("indianDemand.other.no", rejectDemandsAction, goods)
		    		);
				}
			} else {
		    	StringTemplate st = StringTemplate.template("indianDemand.gold.text")
		    		.addStringTemplate("%nation%", mission.getIndianSettlement().getOwner().getNationName())
		    		.add("%colony%", mission.getColony().getName())
		    		.addAmount("%amount%", goldAmount);

		    	guiGameController.showDialogBlocked(new QuestionDialog(st)
		    		.addAnswer("indianDemand.gold.yes", acceptDemandsAction, goods)
		    		.addAnswer("indianDemand.gold.no", rejectDemandsAction, goods)
	    		);
			}
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
			"player[%s].DemandTributeMission[%s] back to settlement[%s]", 
			mission.getUnitToDemandTribute().getOwner().getId(),
			mission.getId(),
			mission.getIndianSettlement().getId()
		);
		
		Path path = pathFinder.findToTile(game.map, unitActualLocation, 
			mission.getIndianSettlement().tile, 
			mission.getUnitToDemandTribute(),
			PathFinder.includeUnexploredTiles
		);
		if (path.isReachedDestination()) {
			MoveContext moveContext = new MoveContext(path);
			moveService.aiConfirmedMovePath(moveContext);
		}
	}
	
}
