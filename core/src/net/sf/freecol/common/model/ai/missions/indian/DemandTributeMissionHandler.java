package net.sf.freecol.common.model.ai.missions.indian;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.IndianSettlement;
import net.sf.freecol.common.model.MoveType;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;
import net.sf.freecol.common.model.ai.missions.indian.DemandTributeMission.Phase;
import net.sf.freecol.common.model.map.path.Path;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.Goods;
import net.sf.freecol.common.model.specification.NationType;

import promitech.colonization.ai.CommonMissionHandler;
import promitech.colonization.ai.MissionHandler;
import promitech.colonization.orders.combat.CombatService;
import promitech.colonization.orders.combat.OffencePower;
import promitech.colonization.orders.move.MoveContext;
import promitech.colonization.orders.move.MoveService;
import promitech.colonization.screen.map.hud.GUIGameController;
import promitech.colonization.ui.QuestionDialog;
import promitech.colonization.ui.QuestionDialog.OptionAction;
import promitech.colonization.ui.resources.StringTemplate;

import static promitech.colonization.ai.MissionHandlerLogger.logger;

public class DemandTributeMissionHandler implements MissionHandler<DemandTributeMission>{

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

		Unit unit = indianPlayer.units.getByIdOrNull(mission.getUnitToDemandTributeId());
		if (unit == null || !CommonMissionHandler.isUnitExists(indianPlayer, unit)) {
			logger.debug("player[%s].DemandTributeMission[%s] no unit or destination. Set done.", indianPlayer.getId(), mission.getId());
			mission.setDone();
			return;
		}
		if (!mission.canExecuteMission(indianPlayer)) {
			logger.debug("player[%s].DemandTributeMission[%s] no unit or destination. Set done.", indianPlayer.getId(), mission.getId());
			mission.setDone();
			return;
		}
		
		if (mission.isColonyOwnerChanged(game)) {
			mission.changePhase(DemandTributeMission.Phase.BACK_TO_SETTLEMENT);
		}
		
		if (mission.getPhase() == Phase.MOVE_TO_COLONY) {
			IndianSettlement indianSettlement = indianPlayer.settlements.getById(mission.getIndianSettlementId()).asIndianSettlement();
			executeMoveToColonyPhase(mission, unit, mission.getDestinationColony(game), indianSettlement);
		}
		
		if (mission.getPhase() == Phase.ATTACK) {
			Colony colony = mission.getDestinationColony(game);
			// assumption unit is next to settlement
			MoveContext moveContext = new MoveContext(
				unit.getTile(),
				colony.tile,
				unit
			);
			
	        if (moveContext.canAiHandleMove() &&
	            (moveContext.isMoveType(MoveType.ATTACK_UNIT) 
	            		|| moveContext.isMoveType(MoveType.ATTACK_SETTLEMENT)
        		)
    		) {
	        	combatService.doCombat(moveContext);
	        	mission.backToSettlementAfterSuccessfulAttack(unit);
	        	return;
            }
		}
		
		if (mission.getPhase() == Phase.BACK_TO_SETTLEMENT) {
			IndianSettlement indianSettlement = indianPlayer.settlements.getById(mission.getIndianSettlementId()).asIndianSettlement();
			executeBackToSettlementPhase(mission, unit, indianSettlement);
		}
	}
	
	private void executeMoveToColonyPhase(DemandTributeMission mission, Unit unit, Colony colony, IndianSettlement indianSettlement) {
		mission.exitFromSettlementWhenOnIt(unit, indianSettlement);

		if (!unit.hasMovesPoints()) {
			return;
		}
		Tile unitActualLocation = unit.getTile();
		
		logger.debug(
			"player[%s].DemandTributeMission[%s] move to colony[%s]",
			indianSettlement.getOwner().getId(),
			mission.getId(),
			colony.getId()
		);
		
		Path path = pathFinder.findToTile(game.map, unitActualLocation, 
			colony.tile,
			unit,
			PathFinder.includeUnexploredTiles
		);
		if (path.isReachedDestination()) {
			MoveContext moveContext = new MoveContext(unit, path);
			if (path.moveStepDest().equalsCoordinates(colony.tile)) {
				moveContext.initNextPathStep();
				moveService.showMoveIfRequired(moveContext);

				demandConfirmation(mission, unit, colony, indianSettlement);
			} else {
				moveService.aiConfirmedMovePath(moveContext);
			}
		}
	}
	
	private void demandConfirmation(DemandTributeMission mission, Unit unit, Colony colony, IndianSettlement indianSettlement) {
		int gold = 0;
		Goods selectedGoods = mission.selectGoods(colony, unit);
		if (selectedGoods == null) {
			gold = mission.selectGold(colony);
		}

		if (colony.getOwner().isAi()) {
			demandFromAiPlayer(selectedGoods, gold, mission, unit, colony, indianSettlement);
		} else {
			demandFromHumanPlayer(selectedGoods, gold, mission, unit, colony, indianSettlement);
		}
	}

	private void demandFromAiPlayer(Goods goods, int goldAmount, DemandTributeMission mission, Unit unit, Colony colony, IndianSettlement indianSettlement) {
		boolean agreeForDemands = false;
		Player colonyOwner = colony.getOwner();
		if (colonyOwner.nationType().equalsId(NationType.COOPERATION) || colonyOwner.nationType().equalsId(NationType.TRADE)) {
			agreeForDemands = true;
		} else {
			if (OffencePower.calculateTileDefencePowerForAttacker(unit, colony.tile) <= 3) {
				agreeForDemands = true;
			}
		}
		if (agreeForDemands) {
			mission.acceptDemands(colony, goods, goldAmount, unit, indianSettlement);
		} else {
			mission.rejectDemands(colony, goods, goldAmount, unit, indianSettlement);
		}
	}

	private void demandFromHumanPlayer(
			final Goods goods,
			final int goldAmount,
			final DemandTributeMission mission,
			final Unit unit,
			final Colony colony,
			final IndianSettlement indianSettlement
	) {
		OptionAction<Goods> acceptDemandsAction = new OptionAction<Goods>() {
			@Override
			public void executeAction(Goods payload) {
				mission.acceptDemands(colony, goods, goldAmount, unit, indianSettlement);
			}
		};
		OptionAction<Goods> rejectDemandsAction = new OptionAction<Goods>() {
			@Override
			public void executeAction(Goods payload) {
				mission.rejectDemands(colony, goods, goldAmount, unit, indianSettlement);
			}
		};

		if (goods != null) {
			if (goods.getType().isFood()) {
				StringTemplate st = StringTemplate.template("indianDemand.food.text")
					.addStringTemplate("%nation%", indianSettlement.getOwner().getNationName())
					.add("%colony%", colony.getName())
					.addAmount("%amount%", goods.getAmount());

				guiGameController.showDialogBlocked(new QuestionDialog(st)
					.addAnswer("indianDemand.food.yes", acceptDemandsAction, goods)
					.addAnswer("indianDemand.food.no", rejectDemandsAction, goods)
				);
			} else {
				StringTemplate st = StringTemplate.template("indianDemand.other.text")
					.addStringTemplate("%nation%", indianSettlement.getOwner().getNationName())
					.add("%colony%", colony.getName())
					.addName("%goods%", goods)
					.addAmount("%amount%", goods.getAmount());

				guiGameController.showDialogBlocked(new QuestionDialog(st)
					.addAnswer("indianDemand.other.yes", acceptDemandsAction, goods)
					.addAnswer("indianDemand.other.no", rejectDemandsAction, goods)
				);
			}
		} else {
			StringTemplate st = StringTemplate.template("indianDemand.gold.text")
				.addStringTemplate("%nation%", indianSettlement.getOwner().getNationName())
				.add("%colony%", colony.getName())
				.addAmount("%amount%", goldAmount);

			guiGameController.showDialogBlocked(new QuestionDialog(st)
				.addAnswer("indianDemand.gold.yes", acceptDemandsAction, goods)
				.addAnswer("indianDemand.gold.no", rejectDemandsAction, goods)
			);
		}
	}

	private void executeBackToSettlementPhase(DemandTributeMission mission, Unit unit, IndianSettlement indianSettlement) {
		Tile unitActualLocation = unit.getTile();
		
		if (unitActualLocation.equalsCoordinates(indianSettlement.tile)) {
			unit.changeUnitLocation(indianSettlement);
			mission.setDone();
			return;
		}
		if (!unit.hasMovesPoints()) {
			return;
		}
		logger.debug(
			"player[%s].DemandTributeMission[%s] back to settlement[%s]", 
			unit.getOwner().getId(),
			mission.getId(),
			mission.getIndianSettlementId()
		);
		
		Path path = pathFinder.findToTile(game.map, unitActualLocation,
			indianSettlement.tile,
			unit,
			PathFinder.includeUnexploredTiles
		);
		if (path.isReachedDestination()) {
			MoveContext moveContext = new MoveContext(unit, path);
			moveService.aiConfirmedMovePath(moveContext);
		}
	}
	
}
