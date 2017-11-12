package promitech.colonization.gamelogic.combat;

import promitech.colonization.GUIGameController;
import promitech.colonization.gamelogic.MoveContext;
import promitech.colonization.ui.QuestionDialog;
import promitech.colonization.ui.QuestionDialog.OptionAction;

public class CombatController {
	
	private final GUIGameController guiGameController;
	private final Combat combat = new Combat();

	private final OptionAction<MoveContext> confirmAttack = new OptionAction<MoveContext>() {
		@Override
		public void executeAction(MoveContext payload) {
			showPreCombatDialog();
		}
	};
	
	public CombatController(GUIGameController guiGameController) {
		this.guiGameController = guiGameController;
	}
	
	public void userInteraction(MoveContext moveContext) {
		combat.init(moveContext.unit, moveContext.destTile);

		if (combat.canAttackWithoutConfirmation()) {
			showPreCombatDialog();
		} else {
			CombatMsg combatMsg = new CombatMsg(combat);
			
			QuestionDialog questionDialog = new QuestionDialog();
			questionDialog.addQuestion(combatMsg.createAttackConfirmationMessageTemplate());
			questionDialog.addAnswer("model.diplomacy.attack.confirm", confirmAttack, moveContext);
			questionDialog.addOnlyCloseAnswer("cancel");
			guiGameController.showDialog(questionDialog);
		}
	}

	private void showPreCombatDialog() {
		SummaryDialog summaryDialog = new SummaryDialog(this, combat);
		guiGameController.showDialog(summaryDialog);
	}
}
