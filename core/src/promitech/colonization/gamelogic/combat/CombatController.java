package promitech.colonization.gamelogic.combat;

import promitech.colonization.GUIGameController;
import promitech.colonization.orders.move.MoveContext;
import promitech.colonization.ui.QuestionDialog;
import promitech.colonization.ui.QuestionDialog.OptionAction;

public class CombatController {
	
	private final GUIGameController guiGameController;
	private final CombatService combatService;
	
	private final Combat combat = new Combat();

	private final OptionAction<MoveContext> confirmWarDeclaration = new OptionAction<MoveContext>() {
		@Override
		public void executeAction(MoveContext payload) {
			showPreCombatDialog(payload);
		}
	};
	private final Runnable nextActiveUnitAction = new Runnable() {
	    @Override
	    public void run() {
	        guiGameController.nextActiveUnitAsGdxPostRunnable();
	    }
	};
    
	public CombatController(GUIGameController guiGameController, CombatService combatService) {
		this.guiGameController = guiGameController;
		this.combatService = combatService;
	}
	
	public void confirmCombat(MoveContext moveContext) {
		combat.init(moveContext.unit, moveContext.destTile);

		if (combat.canAttackWithoutConfirmation()) {
			showPreCombatDialog(moveContext);
		} else {
			CombatMsg combatMsg = new CombatMsg(combat);
			
			QuestionDialog questionDialog = new QuestionDialog();
			questionDialog.addQuestion(combatMsg.createAttackConfirmationMessageTemplate());
			questionDialog.addAnswer("model.diplomacy.attack.confirm", confirmWarDeclaration, moveContext);
			questionDialog.addOnlyCloseAnswer("cancel");
			guiGameController.showDialog(questionDialog);
		}
	}
	
	private void showPreCombatDialog(final MoveContext moveContext) {
		Runnable confirmCombatAction = new Runnable() {
			@Override
			public void run() {
                combatService.doConfirmedCombat(moveContext, combat, nextActiveUnitAction);
			}
		};
		SummaryDialog summaryDialog = new SummaryDialog(confirmCombatAction, combat);
		guiGameController.showDialog(summaryDialog);
	}
}
