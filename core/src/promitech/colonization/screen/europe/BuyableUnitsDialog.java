package promitech.colonization.screen.europe;

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
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.GameResources;
import promitech.colonization.screen.ui.ChangeColonyStateListener;
import promitech.colonization.ui.ClosableDialog;
import promitech.colonization.ui.ClosableDialogSize;
import promitech.colonization.ui.STable;
import promitech.colonization.ui.STableSelectListener;
import promitech.colonization.ui.resources.Messages;
import promitech.colonization.ui.resources.StringTemplate;

class BuyableUnitsDialog extends ClosableDialog<BuyableUnitsDialog> implements STableSelectListener {

	private final ShapeRenderer shape;
	private final List<UnitType> unitsTypes;
	private final Player player;
	private final ChangeColonyStateListener changeColonyStateListener;
	
	public BuyableUnitsDialog(ShapeRenderer shape, List<UnitType> unitsTypes, Player player, ChangeColonyStateListener changeColonyStateListener) {
		super(ClosableDialogSize.def(), ClosableDialogSize.height75());
		this.shape = shape;
		this.unitsTypes = unitsTypes;
		this.player = player;
		this.changeColonyStateListener = changeColonyStateListener;
		
		createComponents();
		
		withHidingOnEsc();
	}

	@Override
	public void onSelect(Object payload) {
		UnitType unitType = (UnitType)payload;
		
		if (player.hasGold(unitType.getPrice())) {
			System.out.println("buy unit in europe " + unitType);
			player.getEurope().buyUnit(unitType, unitType.getPrice());
			
			changeColonyStateListener.changeUnitAllocation();
			
			hide();
		} else {
			System.out.println("do not have gold to by unit " + unitType);
		}
	}
	
	private void createComponents() {
		STable unitsTable = new STable(shape);
		unitsTable.defaults().space(10, 0, 10, 0);
		unitsTable.addSelectListener(this);
		
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
		
		buttonTableLayoutExtendX();
		getContentTable().add(dialogLayout);
		getButtonTable().add(buttonsPanel()).expandX().fillX();
	}

	private Actor buttonsPanel() {
		TextButton okButton = new TextButton("cancel", GameResources.instance.getUiSkin());
		okButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				hideWithFade();
			}
		});
		
		Table panel = new Table();
		panel.setFillParent(true);
		panel.add(okButton).right().pad(0, 10, 10, 10).fillX().expandX();
		return panel;
	}

}
