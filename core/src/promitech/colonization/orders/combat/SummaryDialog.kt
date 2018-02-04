package promitech.colonization.orders.combat

import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Align
import net.sf.freecol.common.model.UnitLabel
import promitech.colonization.GameResources
import promitech.colonization.screen.ui.UnitActor
import promitech.colonization.ui.ClosableDialog
import promitech.colonization.ui.resources.Messages

internal class SummaryDialog (
	val attackConfirmation : java.lang.Runnable,
	val combat : Combat
) : ClosableDialog<SummaryDialog>("", GameResources.instance.getUiSkin()) {
	
	val combatMsg : CombatMsg
	
	init {
		combatMsg = CombatMsg(combat)
	}
	
	override fun init(shapeRenderer : ShapeRenderer) {
		createSummaryContent()
		createButtons()
		
		withHidingOnEsc()
		
	}
	
	private fun createSummaryContent() {
		val uiSkin = GameResources.instance.getUiSkin()
		val summaryTable = Table(uiSkin)
		
		summaryTable.defaults().space(10f)
		summaryTable.defaults().align(Align.left)
		
		summaryTable.add(UnitLabel.getUnitType(combat.combatSides.attacker))
			.colspan(2).center()
		summaryTable.add(" ")
		summaryTable.add(UnitLabel.getUnitType(combat.combatSides.defender))
			.colspan(2).center()
		summaryTable.row()
		
		summaryTable.add(UnitActor(combat.combatSides.attacker)).colspan(2).center()
		summaryTable.add(" ")
		summaryTable.add(UnitActor(combat.combatSides.defender)).colspan(2).center()
		summaryTable.row()
		
		val offenceModifiers = combatMsg.createOffenceModifiersMessages()
		val defenceModifiers = combatMsg.createDefenceModifiersMessages()
		
		for (i in 0 .. Math.max(offenceModifiers.size, defenceModifiers.size)) {
			if (i < offenceModifiers.size) {
				summaryTable.add(offenceModifiers.get(i)[0])
				summaryTable.add(offenceModifiers.get(i)[1]).align(Align.right)
			} else {
				summaryTable.add(" ")
				summaryTable.add(" ")
			}
			
			summaryTable.add(" ")
			
			if (i < defenceModifiers.size) {
				summaryTable.add(defenceModifiers.get(i)[0])
				summaryTable.add(defenceModifiers.get(i)[1]).align(Align.right)
			} else {
				summaryTable.add(" ")
				summaryTable.add(" ")
			}
			summaryTable.row()
		}
		
		summaryTable.add(Messages.msgName("model.source.finalResult"))
		summaryTable.add(CombatMsg.MODIFIER_FORMAT.format(combat.getOffencePower()))
			.align(Align.right)
		
		summaryTable.add(" ")
		
		summaryTable.add(Messages.msgName("model.source.finalResult"))
		summaryTable.add(CombatMsg.MODIFIER_FORMAT.format(combat.getDefencePower()))
			.align(Align.right)
		
		getContentTable().defaults()
		getContentTable().add(summaryTable).pad(20f).row()
	}
	
	private fun createButtons() {
		val cancelButton = TextButton(Messages.msg("cancel"), GameResources.instance.getUiSkin())
		val okButton = TextButton(Messages.msg("ok"), GameResources.instance.getUiSkin())
		
		cancelButton.align(Align.left)
		okButton.align(Align.right)
		
		cancelButton.addListener(object : ChangeListener() {
			override fun changed(event: ChangeListener.ChangeEvent, actor: Actor) {
				hideWithFade()
			}
		})
		
		okButton.addListener(object : ChangeListener() {
			override fun changed(event: ChangeListener.ChangeEvent, actor: Actor) {
				hideWithoutFade()
				attackConfirmation.run()
			}
		})
		
		getButtonTable().add(cancelButton).right().pad(0f, 20f, 20f, 20f)
		getButtonTable().add(okButton).left().pad(0f, 20f, 20f, 20f)
	}
}