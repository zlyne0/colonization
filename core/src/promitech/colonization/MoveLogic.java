package promitech.colonization;

import promitech.colonization.gamelogic.MoveContext;
import promitech.colonization.infrastructure.ThreadsResources;

class MoveDrawer {
	public void startAIUnitDislocationAnimation(MoveContext moveContext) {
		// TODO: analogia jak w AIMoveDrawer - uwspolnienie
	}
}

public class MoveLogic {
	private class RunnableMoveContext implements Runnable {
		private MoveContext moveContext;
		
		@Override
		public void run() {
			handleMoveContext(moveContext);
		}
	}
	
	private final MoveDrawer moveDrawer;
	private final GUIGameController guiGameController;
	private final RunnableMoveContext moveBackgroundThread = new RunnableMoveContext();
	
	public MoveLogic(GUIGameController guiGameController, MoveDrawer moveDrawer) {
		this.guiGameController = guiGameController;
		this.moveDrawer = moveDrawer;
	}
	
	protected void handleMoveContext(MoveContext moveContext) {
		// TODO: przeniesienie logiki z moveContext.handleMove do tej klasy
		// wraz z logika odkrywania mapy 
		moveContext.handleMove();
		moveDrawer.startAIUnitDislocationAnimation(moveContext);
		
		/*

		if (gui.player moze zobaczyc ruch jednostki)
			guiMoveInteraction
		moveDrawer.startMoveAnimation

		 */
	}

	public void fromAiMove(MoveContext moveContext) {
		handleMoveContext(moveContext);
	}
	
	public void fromGuiMoveViaDirection(MoveContext moveContext) {
		moveBackgroundThread.moveContext = moveContext;
		ThreadsResources.instance.executeAImovement(moveBackgroundThread);
	}
}

