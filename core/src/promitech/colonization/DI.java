package promitech.colonization;

import promitech.colonization.actors.map.unitanimation.MoveView;
import promitech.colonization.move.MoveController;
import promitech.colonization.move.MoveService;

public class DI {

	public GUIGameController guiGameController;
	public MoveController moveController;
	public GUIGameModel guiGameModel;
	public MoveLogic moveLogic;
	public MoveService moveService;
	public MoveView moveView;
	
	public void createBeans() {
		guiGameModel = new GUIGameModel();
		guiGameController = new GUIGameController();
		moveLogic = new MoveLogic();
		moveService = new MoveService();
		moveController = new MoveController();
		moveView = new MoveView();
		
		GameLogic gameLogic = new GameLogic(guiGameModel);
		
		moveController.inject(guiGameModel, guiGameController, moveView, moveService);
		moveLogic.inject(guiGameController, moveController, guiGameModel, moveView);
		moveService.inject(guiGameController, moveController, guiGameModel, moveView);
		
		guiGameController.inject(guiGameModel, moveController, gameLogic, moveLogic);
	}
	
}
