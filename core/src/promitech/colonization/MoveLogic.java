package promitech.colonization;

import promitech.colonization.gamelogic.MoveContext;
import promitech.colonization.gamelogic.MoveType;
import promitech.colonization.infrastructure.ThreadsResources;

public class MoveLogic {
	private class RunnableMoveContext implements Runnable {
		private MoveContext moveContext;
		
		@Override
		public void run() {
			handleMoveContext(moveContext);
		}
	}
	
	private final MoveDrawerSemaphore moveDrawerSemaphore;
	private final GUIGameController guiGameController;
	private final RunnableMoveContext moveBackgroundThread = new RunnableMoveContext();
	
	public MoveLogic(GUIGameController guiGameController, MoveDrawerSemaphore moveDrawerSemaphore) {
		this.guiGameController = guiGameController;
		this.moveDrawerSemaphore = moveDrawerSemaphore;
	}
	
	protected void handleMoveContext(MoveContext moveContext) {
		moveContext.handleMove();
		moveDrawerSemaphore.waitForUnitDislocationAnimation(moveContext);
		
		boolean exloredNewTiles = false;
		if (moveContext.isMoveType(MoveType.MOVE) || moveContext.isMoveType(MoveType.MOVE_HIGH_SEAS)) {
			exloredNewTiles = moveContext.unit.getOwner().revealMapAfterUnitMove(guiGameController.getGame().map, moveContext.unit);
		}
		
		if (moveContext.unit.getOwner().isHuman()) {
			if (exloredNewTiles) {
				guiGameController.resetUnexploredBorders();
			}
			if (!moveContext.unit.couldMove() || moveContext.isUnitKilled()) {
				guiGameController.logicNextActiveUnit();
			}
		}
	}

	public void forAiMove(MoveContext moveContext) {
		handleMoveContext(moveContext);
	}
	
	public void forGuiMove(MoveContext moveContext) {
		if (moveContext.isRequireUserInteraction()) {
			switch (moveContext.moveType) {
				case EXPLORE_LOST_CITY_RUMOUR:
					new LostCityRumourLogic(guiGameController, this)
						.handle(moveContext);
				break;
			default:
				throw new IllegalStateException("not implemented required user interaction move type " + moveContext.moveType);
			}
		} else {
			if (!moveContext.canHandleMove()) {
				return;
			}
			forGuiMoveWithUserInteractionApproved(moveContext);
		}
	}
	
	public void forGuiMoveWithUserInteractionApproved(MoveContext moveContext) {
		moveBackgroundThread.moveContext = moveContext;
		// it's run handleMoveContext(moveContext); in thread
		ThreadsResources.instance.executeAImovement(moveBackgroundThread);
	}
}

