package promitech.colonization.gamelogic.combat;

import promitech.colonization.GUIGameController;
import promitech.colonization.gamelogic.combat.Combat.CombatResult;
import promitech.colonization.orders.move.MoveContext;
import promitech.colonization.orders.move.MoveService;
import promitech.colonization.screen.map.unitanimation.MoveView;
import promitech.colonization.ui.QuestionDialog;
import promitech.colonization.ui.QuestionDialog.OptionAction;

public class CombatController {
	
	private final GUIGameController guiGameController;
	private final MoveView moveView;
	private final MoveService moveService;
	
	private final Combat combat = new Combat();

	private final OptionAction<MoveContext> confirmWarDeclaration = new OptionAction<MoveContext>() {
		@Override
		public void executeAction(MoveContext payload) {
			showPreCombatDialog(payload);
		}
	};
    
	public CombatController(GUIGameController guiGameController, MoveView moveView, MoveService moveService) {
		this.guiGameController = guiGameController;
		this.moveView = moveView;
		this.moveService = moveService;
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

	public void confirmAttack(final MoveContext moveContext) {
		final Runnable endOfAnimation = new Runnable() {
			@Override
			public void run() {
				combat.processAttackResult();
				moveService.postMoveProcessor(moveContext);
				guiGameController.nextActiveUnitAsGdxPostRunnable();
			}
		};
		
		// TODO: random
		CombatResult combatResult = combat.generateGreatLoss();
		if (combatResult.equals(Combat.CombatResult.WIN)) {
			moveView.showSuccessfulAttackWithMove(moveContext, combat.combatResolver.loser, endOfAnimation);
		}
		if (combatResult.equals(Combat.CombatResult.LOSE)) {
		    moveContext.setUnitKilled();
			moveView.showFailedAttackMoveUnblocked(moveContext, endOfAnimation);
		}
		if (combatResult.equals(Combat.CombatResult.EVADE_ATTACK)) {
			moveView.showAttackRetreat(moveContext, endOfAnimation);
		}		
		
		// TODO: SeekAndDestroyAiMission jesli jednostak widzi do zniszenia to niszczy
		
//		sukces,
//		porazka -> zmiana roli,
//		porazka -> calkowite zniszczenie
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
