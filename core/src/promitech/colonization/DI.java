package promitech.colonization;

import promitech.colonization.actors.map.unitanimation.MoveView;

public class DI {

	public GUIGameController guiGameController;
	public MoveController moveController;
	public GUIGameModel guiGameModel;
	public MoveLogic moveLogic;
	public MoveView moveView;
	
	public void createBeans() {
		guiGameModel = new GUIGameModel();
		guiGameController = new GUIGameController();
		moveLogic = new MoveLogic();
		moveController = new MoveController();
		moveView = new MoveView();
		
		GameLogic gameLogic = new GameLogic(guiGameModel);
		
		moveController.inject(moveLogic, guiGameModel, guiGameController, moveView);
		moveLogic.inject(guiGameController, moveController, guiGameModel, moveView);
		
		guiGameController.inject(guiGameModel, moveController, gameLogic, moveLogic);
	}
	
}
