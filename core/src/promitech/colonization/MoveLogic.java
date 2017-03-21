package promitech.colonization;

import net.sf.freecol.common.model.Unit.UnitState;
import promitech.colonization.gamelogic.MoveContext;
import promitech.colonization.gamelogic.MoveType;
import promitech.colonization.infrastructure.ThreadsResources;

public class MoveLogic {
	
	public static interface AfterMoveProcessor {
		void afterMove(MoveContext moveContext);
	}
	
	private abstract class RunnableMoveContext implements Runnable {
		protected MoveContext moveContext;
		protected AfterMoveProcessor afterMovePorcessor;
	}
	
	private final MoveDrawerSemaphore moveDrawerSemaphore;
	private final GUIGameController guiGameController;
	
	private final RunnableMoveContext moveHandlerThread = new RunnableMoveContext() {
		@Override
		public void run() {
			gui_handleMoveContext(moveContext, afterMovePorcessor);
		}
	};
	
	private final RunnableMoveContext moveHandlerOnlyReallocation = new RunnableMoveContext() {
		@Override
		public void run() {
			handleOnlyReallocation(moveContext, afterMovePorcessor);
		}
	};
	
	public MoveLogic(GUIGameController guiGameController, MoveDrawerSemaphore moveDrawerSemaphore) {
		this.guiGameController = guiGameController;
		this.moveDrawerSemaphore = moveDrawerSemaphore;
	}
	
	private void gui_handleMoveContext(MoveContext moveContext, AfterMoveProcessor afterMovePorcessor) {
		if (moveContext.isMoveViaPath()) {
			gui_handlePathMoveContext(moveContext);
		} else {
			gui_handleOneTileMoveContext(moveContext, afterMovePorcessor);
		}
	}

	private void gui_handlePathMoveContext(MoveContext moveContext) {
		boolean processMoveViaPath = true;
		while (processMoveViaPath) {
			
			if (!moveContext.canHandleMove()) {
				// move via path but no move points so next unit
				moveContext.unit.setState(UnitState.SKIPPED);
				guiNextActiveUnitForActiveUnit();
				
				processMoveViaPath = false;
				break;
			}

			handleOnlyReallocation(moveContext, null);
			moveContext.initNextPathStep();
			
			if (guiGameController.getGame().map.isUnitSeeHostileUnit(moveContext.unit)) {
				System.out.println("unit: " + moveContext.unit + " see hostile unit");
				processMoveViaPath = false;
				break;
			}
				
			if (moveContext.isEndOfPath()) {
				if (moveContext.unit.isDestinationEurope() && moveContext.unit.getTile().getType().isHighSea()) {
					moveContext.unit.moveUnitToHighSea();
					guiNextActiveUnitForActiveUnit();
				} else {
					moveContext.unit.clearDestination();
					guiGameController.removeDrawableUnitPath();
				}
				
				// no move points so next unit
				if (!moveContext.unit.couldMove()) {
					guiNextActiveUnitForActiveUnit();
				}
				// dest tile and carrier so show colony screen
				if (moveContext.unit.isCarrier() && moveContext.destTile.hasSettlement()) {
					guiGameController.showColonyScreen(moveContext.destTile);
				}
				processMoveViaPath = false;
				break;
			}
			
		}
	}
	
	private void gui_handleOneTileMoveContext(MoveContext moveContext, AfterMoveProcessor afterMovePorcessor) {
		if (moveContext.isRequireUserInteraction()) {
			requiredUserInterationProcessor(moveContext);
			return;
		}
		handleOnlyReallocation(moveContext, afterMovePorcessor);
	}
	
	private void requiredUserInterationProcessor(MoveContext moveContext) {
		switch (moveContext.moveType) {
			case EXPLORE_LOST_CITY_RUMOUR:
				new LostCityRumourLogic(guiGameController, this)
					.handle(moveContext);
				break;
			default:
				throw new IllegalStateException("not implemented required user interaction move type " + moveContext.moveType);
		}
	}
	
	private void handleOnlyReallocation(MoveContext moveContext, AfterMoveProcessor afterMovePorcessor) {
		moveContext.handleMove();
		moveDrawerSemaphore.waitForUnitDislocationAnimation(moveContext);
		
		boolean exloredNewTiles = false;
		if (moveContext.isMoveType(MoveType.MOVE) || moveContext.isMoveType(MoveType.MOVE_HIGH_SEAS)) {
			exloredNewTiles = moveContext.unit.getOwner().revealMapAfterUnitMove(guiGameController.getGame().map, moveContext.unit);
		}
		
		if (moveContext.isHuman()) {
			if (exloredNewTiles) {
				guiGameController.resetUnexploredBorders();
			}
		}
		
		if (afterMovePorcessor != null) {
			afterMovePorcessor.afterMove(moveContext);
		}
	}
	
	public void guiNextActiveUnitForActiveUnit() {
		guiGameController.logicNextActiveUnit();
	}
	public void guiNextActiveUnit(MoveContext moveContext) {
		if (moveContext.isAi()) {
			throw new IllegalStateException("should not run by ai");
		}
		if (!moveContext.unit.couldMove() || moveContext.isUnitKilled()) {
			guiGameController.logicNextActiveUnit();
		}
	}
	private final AfterMoveProcessor oneStepGuiAfterMoveProcessor = new AfterMoveProcessor() {
		@Override
		public void afterMove(MoveContext moveContext) {
			guiNextActiveUnit(moveContext);
		}
	};
	private final AfterMoveProcessor doNothingAfterMoveProcessor = new AfterMoveProcessor() {
		@Override
		public void afterMove(MoveContext moveContext) {
		}
	};
	
	public void forGuiMove(MoveContext moveContext) {
		if (!moveContext.canHandleMove()) {
			return;
		}
		if (moveContext.isRequireUserInteraction()) {
			requiredUserInterationProcessor(moveContext);
			return;
		}
		
		moveHandlerThread.moveContext = moveContext;
		moveHandlerThread.afterMovePorcessor = oneStepGuiAfterMoveProcessor;
		ThreadsResources.instance.executeAImovement(moveHandlerThread);
	}
	
	public void forGuiMoveOnlyReallocation(MoveContext moveContext, AfterMoveProcessor afterMovePorcessor) {
		if (!moveContext.canHandleMove()) {
			throw new IllegalStateException("should not invoke reallocation when can not handle move");
		}
		moveHandlerOnlyReallocation.moveContext = moveContext;
		moveHandlerOnlyReallocation.afterMovePorcessor = afterMovePorcessor;
		ThreadsResources.instance.executeAImovement(moveHandlerOnlyReallocation);
	}

	public void forAiMoveOnlyReallocation(MoveContext moveContext) {
		handleOnlyReallocation(moveContext, doNothingAfterMoveProcessor);
	}

}
