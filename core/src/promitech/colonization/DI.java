package promitech.colonization;

import promitech.colonization.gamelogic.combat.CombatService;
import promitech.colonization.orders.move.MoveController;
import promitech.colonization.orders.move.MoveService;
import promitech.colonization.screen.map.unitanimation.MoveView;

public class DI {

	public GUIGameController guiGameController;
	public MoveController moveController;
	public GUIGameModel guiGameModel;
	public MoveService moveService;
	public CombatService combatService;
	public MoveView moveView;
	
	public void createBeans() {
		guiGameModel = new GUIGameModel();
		guiGameController = new GUIGameController();
		moveService = new MoveService();
		combatService = new CombatService();
		moveController = new MoveController();
		moveView = new MoveView();
		
		GameLogic gameLogic = new GameLogic(guiGameModel);
		
		moveController.inject(guiGameModel, guiGameController, moveView, moveService);
		moveService.inject(guiGameController, moveController, guiGameModel, combatService);
		combatService.inject(moveService);
		
		guiGameController.inject(guiGameModel, moveController, gameLogic, moveService);
	}
	
}
