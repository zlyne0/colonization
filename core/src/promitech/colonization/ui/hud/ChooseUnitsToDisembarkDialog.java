package promitech.colonization.ui.hud;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitLabel;
import promitech.colonization.GameResources;
import promitech.colonization.actors.UnitActor;
import promitech.colonization.move.MoveContext;
import promitech.colonization.move.MoveController;
import promitech.colonization.ui.ClosableDialog;
import promitech.colonization.ui.STable;
import promitech.colonization.ui.STableSelectListener;
import promitech.colonization.ui.resources.Messages;

public class ChooseUnitsToDisembarkDialog extends ClosableDialog<ChooseUnitsToDisembarkDialog> {

	private STable unitTable;
	private final MoveController moveController;
	private final MoveContext moveContext;
	
	public ChooseUnitsToDisembarkDialog(MoveContext moveContext, MoveController moveController) {
		super("", GameResources.instance.getUiSkin());
		this.moveController = moveController;
		this.moveContext = moveContext;
	}

	@Override
	public void init(ShapeRenderer shapeRenderer) {
		unitTable = new STable(shapeRenderer);
		
		UnitActor carrierUnitActor = new UnitActor(moveContext.unit, null); 
		Label disembarkLabel = new Label(Messages.msg("disembark.text"), GameResources.instance.getUiSkin());
		
		HorizontalGroup hg = new HorizontalGroup();
		hg.addActor(carrierUnitActor);
		hg.addActor(disembarkLabel);
		getContentTable().add(hg).space(0, 0, 20, 0).row();
		getContentTable().add(unitTable);
		
		createButtonsPanel();
		createUnitsList();
	}

	private void createUnitsList() {
		int[] alligment = new int[] { Align.center, Align.left };
		for (Unit u : moveContext.unit.getUnitContainer().getUnits().entities()) {
			UnitActor ua = new UnitActor(u, null);
			Label ual = new Label(UnitLabel.getUnitType(u), GameResources.instance.getUiSkin());
			unitTable.addRow(u, alligment, ua, ual);
		}
		unitTable.addSelectListener(new STableSelectListener() {
			@Override
			public void onSelect(Object payload) {
				hideWithoutFade();
				
				moveController.disembarkUnitToLocation(
					moveContext.unit, 
					(Unit)payload, 
					moveContext.destTile
				);
			}
		});
	}

	private void createButtonsPanel() {
		TextButton allUnitsButton = new TextButton(Messages.msg("all"), GameResources.instance.getUiSkin());
		TextButton noneUnitsButton = new TextButton(Messages.msg("disembark.cancel"), GameResources.instance.getUiSkin());
		allUnitsButton.align(Align.right);
		noneUnitsButton.align(Align.left);
		getButtonTable().add(allUnitsButton).right().pad(0, 20, 20, 20);
		getButtonTable().add(noneUnitsButton).left().pad(0, 20, 20, 20);
		
		allUnitsButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				hideWithoutFade();
				moveController.disembarkUnitsToLocation(moveContext.unit, moveContext.unit.getUnitContainer().getUnits().entities(), moveContext.destTile);
			}
		});
		noneUnitsButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				hideWithFade();
			}
		});
	}
	
}

