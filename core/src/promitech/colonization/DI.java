package promitech.colonization;

import net.sf.freecol.common.model.map.path.PathFinder;
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
	public PathFinder pathFinder;
	
	public void createBeans() {
		guiGameModel = new GUIGameModel();
		guiGameController = new GUIGameController();
		moveService = new MoveService();
		combatService = new CombatService();
		moveController = new MoveController();
		moveView = new MoveView();
		firstContactController = new FirstContactController();
		pathFinder = new PathFinder();
		
		
		GameLogic gameLogic = new GameLogic(guiGameModel, combatService);
		
		moveController.inject(guiGameModel, guiGameController, moveView, moveService, pathFinder);
		moveService.inject(guiGameController, moveController, guiGameModel, combatService, firstContactController);
		combatService.inject(moveService, guiGameModel);
		
		guiGameController.inject(guiGameModel, moveController, gameLogic, moveService, pathFinder);
		firstContactController.inject(guiGameController, moveService, guiGameModel);
	}
	
}
