package promitech.colonization;

public class DI {

	public GUIGameController guiGameController;
	public MoveController moveController;
	public GUIGameModel guiGameModel;
	public MoveLogic moveLogic;
	
	public void createBeans() {
		guiGameModel = new GUIGameModel();
		guiGameController = new GUIGameController();
		moveLogic = new MoveLogic();
		moveController = new MoveController();
		
		GameLogic gameLogic = new GameLogic(guiGameModel);
		
		moveController.inject(moveLogic, guiGameModel, guiGameController);
		moveLogic.inject(guiGameController, moveController, guiGameModel);
		
		guiGameController.inject(guiGameModel, moveController, gameLogic, moveLogic);
	}
	
}
