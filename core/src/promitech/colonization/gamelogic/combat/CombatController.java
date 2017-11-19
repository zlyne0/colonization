package promitech.colonization.gamelogic.combat;

import promitech.colonization.GUIGameController;
import promitech.colonization.MoveLogic;
import promitech.colonization.gamelogic.MoveContext;
import promitech.colonization.ui.QuestionDialog;
import promitech.colonization.ui.QuestionDialog.OptionAction;

public class CombatController {
	
	private final GUIGameController guiGameController;
	private final MoveLogic moveLogic;
	private final Combat combat = new Combat();

	private final OptionAction<MoveContext> confirmWarDeclaration = new OptionAction<MoveContext>() {
		@Override
		public void executeAction(MoveContext payload) {
			showPreCombatDialog(payload);
		}
	};
	
	public CombatController(GUIGameController guiGameController, MoveLogic moveLogic) {
		this.guiGameController = guiGameController;
		this.moveLogic = moveLogic;
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

	public void confirmAttack(MoveContext moveContext) {
		moveLogic.forGuiMoveOnlyReallocation(moveContext, new MoveLogic.AfterMoveProcessor() {
			public void afterMove(final MoveContext mc) {
			}
		});
		
		
//		na move logic odpalic show move animation
//		- sukces
//		- porazka
		
		//na zakonczenie przelaczenie na kolejna jednostke
		//skasowanie jednostki lub zmiana polozenia
	}
	
	private void showPreCombatDialog(final MoveContext moveContext) {
		Runnable ca = new Runnable() {
			@Override
			public void run() {
				confirmAttack(moveContext);
			}
		};
		SummaryDialog summaryDialog = new SummaryDialog(ca, combat);
		guiGameController.showDialog(summaryDialog);
	}
}
