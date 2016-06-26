package promitech.colonization.actors.europe;

import java.util.List;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

import net.sf.freecol.common.model.UnitType;
import promitech.colonization.GameResources;
import promitech.colonization.ui.ClosableDialog;
import promitech.colonization.ui.STable;
import promitech.colonization.ui.resources.Messages;
import promitech.colonization.ui.resources.StringTemplate;

class BuyableUnitsDialog extends ClosableDialog {

	private final ShapeRenderer shape;
	private final List<UnitType> unitsTypes;
	
	public BuyableUnitsDialog(float maxHeight, ShapeRenderer shape, List<UnitType> unitsTypes) {
		super("", GameResources.instance.getUiSkin(), maxHeight);
		this.shape = shape;
		this.unitsTypes = unitsTypes;
		
		createComponents();
		
		withHidingOnEsc();
	}

	private void createComponents() {
		STable unitsTable = new STable(shape);
		unitsTable.defaults().space(10, 0, 10, 0);
		
		int[] labelAlign = new int[] { Align.left, Align.left, Align.right };
		
		for (UnitType unitType : unitsTypes) {
			TextureRegion texture = GameResources.instance.getFrame(unitType.resourceImageKey()).texture;
			Image image = new Image(new TextureRegionDrawable(texture), Scaling.none, Align.center);
			
			StringTemplate labelSt = StringTemplate.template(Messages.nameKey(unitType.getId()))
				.addAmount("%number%", 1);
			
			Label label = new Label(Messages.message(labelSt), GameResources.instance.getUiSkin());
			
			StringTemplate priceSt = StringTemplate.template("goldAmount").addAmount("%amount%", unitType.getPrice());
			Label priceLabel = new Label(Messages.message(priceSt), GameResources.instance.getUiSkin());
			
			unitsTable.addRow(unitType, labelAlign, image, label, priceLabel);
		}
		
		ScrollPane unitsScrollPane = new ScrollPane(unitsTable, GameResources.instance.getUiSkin());
		unitsScrollPane.setFlickScroll(false);
		unitsScrollPane.setScrollingDisabled(true, false);
		unitsScrollPane.setForceScroll(false, false);
		unitsScrollPane.setFadeScrollBars(false);
		unitsScrollPane.setOverscroll(true, true);
		unitsScrollPane.setScrollBarPositions(false, true);
		
		Table dialogLayout = new Table();
		dialogLayout.add(unitsScrollPane).pad(20);
		
		getContentTable().add(dialogLayout);
		getButtonTable().add(buttonsPanel()).expandX();
	}

	private Actor buttonsPanel() {
		TextButton okButton = new TextButton("cancel", GameResources.instance.getUiSkin());
		okButton.align(Align.right);
		okButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				hideWithFade();
			}
		});
		
		Table panel = new Table();
		panel.setFillParent(true);
		panel.add(okButton).right().pad(0, 20, 20, 20);
		return panel;
	}

}
