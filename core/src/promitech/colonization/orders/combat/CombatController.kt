package promitech.colonization.orders.combat

import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import net.sf.freecol.common.model.player.Player
import promitech.colonization.screen.map.hud.GUIGameController
import promitech.colonization.screen.map.hud.GUIGameModel
import promitech.colonization.orders.move.MoveContext
import promitech.colonization.ui.QuestionDialog
import promitech.colonization.ui.SimpleMessageDialog
import promitech.colonization.ui.resources.StringTemplate
import promitech.colonization.orders.diplomacy.FirstContactController

class CombatController (
	private val guiGameController: GUIGameController,
	private val combatService: CombatService,
	private val guiGameModel: GUIGameModel,
	private val firstContactController: FirstContactController 
) {
	
	private val combat: Combat = Combat()

    private val confirmWarDeclaration = QuestionDialog.OptionAction<MoveContext> {
        payload -> showPreCombatDialog(payload)
    }
    private val nextActiveUnitAction = Runnable {
        guiGameController.nextActiveUnitAsGdxPostRunnable()
    }
	private val nextActiveUnitActionEventListener = object : EventListener {
		override fun handle(event: Event?) : Boolean {
		    guiGameController.nextActiveUnitAsGdxPostRunnable()
			return true
		}
	}

    fun confirmCombat(moveContext: MoveContext) {
		combat.init(guiGameModel.game, moveContext.unit, moveContext.destTile)

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

	fun confirmSettlementCombat(moveContext: MoveContext) {
		var questionMsg = settlementActionChoice(moveContext.unit.getOwner(), moveContext.destTile.settlement.getOwner())

		var questionDialog = QuestionDialog()
		questionDialog.addQuestion(questionMsg)
		questionDialog.addAnswer("armedUnitSettlement.tribute",
			QuestionDialog.OptionAction {
				mc -> firstContactController.demandTributeFromSettlement(mc)
			},
			moveContext
		)
		questionDialog.addAnswer("armedUnitSettlement.attack",
			QuestionDialog.OptionAction {
				mc -> confirmCombat(mc)
			},
			moveContext
        )
		questionDialog.addAnswer("cancel", QuestionDialog.DO_NOTHING_ACTION, moveContext)
		guiGameController.showDialog(questionDialog)
	}
	
	private fun settlementActionChoice(attacker : Player, settlementOwner: Player) : StringTemplate {
		if (settlementOwner.isIndian()) {
			return StringTemplate.template("indianSettlement." + settlementOwner.getTension(attacker).getKey())
		        .addStringTemplate("%nation%", settlementOwner.getNationName());
		} else {
			return StringTemplate.template("colony.tension." + settlementOwner.getStance(attacker).getKey())
			    .addStringTemplate("%nation%", settlementOwner.getNationName())
		}
	}
	
    private fun showPreCombatDialog(moveContext: MoveContext) {
        val confirmCombatAction = Runnable {
            combatService.doConfirmedCombat(moveContext, combat, Runnable() {
				if (combat.getBlockingCombatNotifications().isEmpty()) {
					nextActiveUnitAction.run()
				} else {
    				guiGameController.showDialog(SimpleMessageDialog()
    					.withContent(combat.getBlockingCombatNotifications().first())
    					.withButton("ok")
    					.addOnCloseListener(nextActiveUnitActionEventListener)
    				);
				}
			})
        }
        val summaryDialog = SummaryDialog(confirmCombatAction, combat)
        guiGameController.showDialog(summaryDialog)
    }

}
