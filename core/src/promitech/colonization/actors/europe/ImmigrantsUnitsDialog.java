package promitech.colonization.actors.europe;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.GameResources;
import promitech.colonization.actors.ChangeColonyStateListener;
import promitech.colonization.ui.ClosableDialog;
import promitech.colonization.ui.STable;
import promitech.colonization.ui.STableSelectListener;
import promitech.colonization.ui.resources.Messages;
import promitech.colonization.ui.resources.StringTemplate;

public class ImmigrantsUnitsDialog extends ClosableDialog implements STableSelectListener {

	private final ShapeRenderer shape;
	private final Player player;
	private final ChangeColonyStateListener changeColonyStateListener;
	
	public ImmigrantsUnitsDialog(float maxHeight, ShapeRenderer shape, Player player, ChangeColonyStateListener changeColonyStateListener) {
		super("", GameResources.instance.getUiSkin());
		this.shape = shape;
		this.player = player;
		this.changeColonyStateListener = changeColonyStateListener;
		
		createComponents();
		
		withHidingOnEsc();
	}

	@Override
	public void onSelect(Object payload) {
		UnitType unitType = (UnitType)payload;
		int price = player.getEurope().getRecruitImmigrantPrice();
		if (player.hasGold(price)) {
			System.out.println("recruit immigrant " + unitType + " for gold " + price);
			
			player.getEurope().buyImmigrant(unitType, price);
			changeColonyStateListener.changeUnitAllocation();
			
			hide();
		} else {
			System.out.println("do not have gold to recruit immigrant " + unitType + " for gold " + price);
		}
	}

	private void createComponents() {
		int[] labelAlign = new int[] { Align.left, Align.left };
		
		STable unitsTable = new STable(shape);
		unitsTable.defaults().space(10, 0, 10, 0);
		unitsTable.addSelectListener(this);
		
		for (UnitType unitType : player.getEurope().getRecruitables()) {
			TextureRegion texture = GameResources.instance.getFrame(unitType.resourceImageKey()).texture;
			Image image = new Image(new TextureRegionDrawable(texture), Scaling.none, Align.center);
			
			StringTemplate labelSt = StringTemplate.template(Messages.nameKey(unitType.getId()))
				.addAmount("%number%", 1);
			Label label = new Label(Messages.message(labelSt), GameResources.instance.getUiSkin());

			unitsTable.addRow(unitType, labelAlign, image, label);
		}
		
        String header = Messages.message(StringTemplate.template("recruitPanel.clickOn")
            .addAmount("%money%", player.getEurope().getRecruitImmigrantPrice())
            .addAmount("%number%", player.getEurope().getNextImmigrantTurns()));
        Label lable = new Label(header, GameResources.instance.getUiSkin());
        lable.setWrap(true);
		
		Table dialogLayout = new Table();
		dialogLayout.add(lable).pad(20).space(10).width(300).row();
		dialogLayout.add(unitsTable).row();
		
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
