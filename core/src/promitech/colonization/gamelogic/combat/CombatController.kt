package promitech.colonization.gamelogic.combat

import promitech.colonization.GUIGameController
import promitech.colonization.orders.move.MoveContext
import promitech.colonization.ui.QuestionDialog

class CombatController (
	private val guiGameController: GUIGameController,
	private val combatService: CombatService
) {
	
	private val combat: Combat = Combat()

    private val confirmWarDeclaration = QuestionDialog.OptionAction<MoveContext> {
        payload -> showPreCombatDialog(payload)
    }
    private val nextActiveUnitAction = Runnable {
        guiGameController.nextActiveUnitAsGdxPostRunnable()
    }

    fun confirmCombat(moveContext: MoveContext) {
		combat.init(moveContext.unit, moveContext.destTile)

        if (combat.canAttackWithoutConfirmation()) {
            showPreCombatDialog(moveContext)
        } else {
            val combatMsg = CombatMsg(combat)

            val questionDialog = QuestionDialog()
            questionDialog.addQuestion(combatMsg.createAttackConfirmationMessageTemplate())
            questionDialog.addAnswer("model.diplomacy.attack.confirm", confirmWarDeclaration, moveContext)
            questionDialog.addOnlyCloseAnswer("cancel")
            guiGameController.showDialog(questionDialog)
        }
    }

    private fun showPreCombatDialog(moveContext: MoveContext) {
        val confirmCombatAction = Runnable {
            combatService.doConfirmedCombat(moveContext, combat, nextActiveUnitAction)
        }
        val summaryDialog = SummaryDialog(confirmCombatAction, combat)
        guiGameController.showDialog(summaryDialog)
    }

}
