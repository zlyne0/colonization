package promitech.colonization;

import promitech.colonization.orders.combat.CombatService;
import promitech.colonization.orders.diplomacy.FirstContactController;
import promitech.colonization.orders.move.MoveController;
import promitech.colonization.orders.move.MoveService;
import promitech.colonization.screen.map.hud.GUIGameController;
import promitech.colonization.screen.map.hud.GUIGameModel;
import promitech.colonization.screen.map.unitanimation.MoveView;

public class DI {

	public GUIGameController guiGameController;
	public MoveController moveController;
	public FirstContactController firstContactController;
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
		firstContactController = new FirstContactController();
		
		GameLogic gameLogic = new GameLogic(guiGameModel, combatService);
		
		moveController.inject(guiGameModel, guiGameController, moveView, moveService);
		moveService.inject(guiGameController, moveController, guiGameModel, combatService, firstContactController);
		combatService.inject(moveService, guiGameModel);
		
		guiGameController.inject(guiGameModel, moveController, gameLogic, moveService);
		firstContactController.inject(guiGameController, moveService, guiGameModel);
	}
	
}
