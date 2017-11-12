package promitech.colonization.gamelogic.combat;

import java.util.List;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import net.sf.freecol.common.model.UnitLabel;
import promitech.colonization.GameResources;
import promitech.colonization.actors.UnitActor;
import promitech.colonization.ui.ClosableDialog;
import promitech.colonization.ui.resources.Messages;

class SummaryDialog extends ClosableDialog<SummaryDialog> {

	private final CombatController combatController;
	private final Combat combat;
	private final CombatMsg combatMsg;
	
	public SummaryDialog(CombatController combatController, Combat combat) {
		super("", GameResources.instance.getUiSkin());
		
		this.combatController = combatController;
		this.combat = combat;
		this.combatMsg = new CombatMsg(combat);
	}

	@Override
	public void init(ShapeRenderer shapeRenderer) {
		createSummaryContent();
		createButtons();
		
		withHidingOnEsc();
	}

	private void createSummaryContent() {
		Skin uiSkin = GameResources.instance.getUiSkin();
		Table summaryTable = new Table(uiSkin);
		summaryTable.defaults().space(10);
		summaryTable.defaults().align(Align.left);
		
		summaryTable.add(new Label(
			UnitLabel.getUnitType(combat.combatSides.attacker), 
			uiSkin
		)).colspan(2).center();
		summaryTable.add(" ");
		summaryTable.add(new Label(
			UnitLabel.getUnitType(combat.combatSides.defender), 
			uiSkin
		)).colspan(2).center();
		summaryTable.row();
		
		summaryTable.add(new UnitActor(combat.combatSides.attacker)).colspan(2).center();
		summaryTable.add(" ");
		summaryTable.add(new UnitActor(combat.combatSides.defender)).colspan(2).center();
		summaryTable.row();
		
		List<String[]> offenceModifiers = combatMsg.createOffenceModifiersMessages();
		List<String[]> defenceModifiers = combatMsg.createDefenceModifiersMessages();
		
		for (int i = 0, max = Math.max(offenceModifiers.size(), defenceModifiers.size()); i < max; i++) {
			if (i < offenceModifiers.size()) {
				summaryTable.add(offenceModifiers.get(i)[0]);
				summaryTable.add(offenceModifiers.get(i)[1]).align(Align.right);
			} else {
				summaryTable.add(" ");
				summaryTable.add(" ");
			}
			
			summaryTable.add(" ");
			
			if (i < defenceModifiers.size()) {
				summaryTable.add(defenceModifiers.get(i)[0]);
				summaryTable.add(defenceModifiers.get(i)[1]).align(Align.right);
			} else {
				summaryTable.add(" ");
				summaryTable.add(" ");
			}
			summaryTable.row();
		}

		
		summaryTable.add(Messages.msgName("model.source.finalResult"));
		summaryTable.add(CombatMsg.MODIFIER_FORMAT.format(combat.getOffencePower())).align(Align.right);
		
		summaryTable.add(" ");
		
		summaryTable.add(Messages.msgName("model.source.finalResult"));
		summaryTable.add(CombatMsg.MODIFIER_FORMAT.format(combat.getDefencePower())).align(Align.right);
		
		getContentTable().defaults();
		getContentTable().add(summaryTable).pad(20).row();
	}
	
	private void createButtons() {
		TextButton cancelButton = new TextButton(Messages.msg("cancel"), GameResources.instance.getUiSkin());
		TextButton okButton = new TextButton(Messages.msg("ok"), GameResources.instance.getUiSkin());
		
		cancelButton.align(Align.left);
		okButton.align(Align.right);
		
		cancelButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				hideWithFade();
			}
		});
		
		getButtonTable().add(cancelButton).right().pad(0, 20, 20, 20);
		getButtonTable().add(okButton).left().pad(0, 20, 20, 20);
	}
}
