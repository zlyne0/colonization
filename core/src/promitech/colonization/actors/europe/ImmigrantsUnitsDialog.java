package promitech.colonization.actors.europe;

import java.util.List;

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
import promitech.colonization.GameResources;
import promitech.colonization.ui.ClosableDialog;
import promitech.colonization.ui.STable;
import promitech.colonization.ui.resources.Messages;
import promitech.colonization.ui.resources.StringTemplate;

public class ImmigrantsUnitsDialog extends ClosableDialog {

	private final ShapeRenderer shape;
	private final List<UnitType> unitsTypes;
	
	public ImmigrantsUnitsDialog(float maxHeight, ShapeRenderer shape, List<UnitType> unitsTypes) {
		super("", GameResources.instance.getUiSkin());
		this.shape = shape;
		this.unitsTypes = unitsTypes;
		
		createComponents();
		
		withHidingOnEsc();
	}

	private void createComponents() {
		int[] labelAlign = new int[] { Align.left, Align.left };
		
		STable unitsTable = new STable(shape);
		unitsTable.defaults().space(10, 0, 10, 0);
		
		for (UnitType unitType : unitsTypes) {
			TextureRegion texture = GameResources.instance.getFrame(unitType.resourceImageKey()).texture;
			Image image = new Image(new TextureRegionDrawable(texture), Scaling.none, Align.center);
			
			StringTemplate labelSt = StringTemplate.template(Messages.nameKey(unitType.getId()))
				.addAmount("%number%", 1);
			Label label = new Label(Messages.message(labelSt), GameResources.instance.getUiSkin());

			unitsTable.addRow(unitType, labelAlign, image, label);
		}

		// TODO: freecol, RecruitPanel 
		/*
		Player player;
		Europe europe;
        int production = player.getTotalImmigrationProduction();
        int turns = 100;
        if (production > 0) {
            int immigrationRequired = player.getImmigrationRequired() - player.getImmigration();
            turns = immigrationRequired / production;
            if (immigrationRequired % production > 0) turns++;
        }
        europe.getRecruitPrice();
		*/
		
		// imigration production tylko dla if (!isColonial()) return 0;
		// player
		// immigration="0"
		// immigrationRequired="107"
		// europe
		// recruitPrice="710" 
		// recruitLowerCap="80"
		
		// biore wszystkie goodsType ktore maja modyfikator model.modifier.immigration czyli crosses
		// zliaczam calkowita produkcje crosses ze wszystkich colony
		// jesli nation jest krajem Europe europe = getEurope(); dodatek do produkcji
		// liczenie dodatku
		// wszystkie unit.isPerson w europe
		// poddanie mnoznikom
//        n *= spec.getInteger(GameOptions.EUROPEAN_UNIT_IMMIGRATION_PENALTY);
//        n += spec.getInteger(GameOptions.PLAYER_IMMIGRATION_BONUS);
		// dodaje dodatek
		// calkowita produkcja crosses nie moze byc ujemna, jesli jest ujemne wyjdzie zero
		
		// wyliczenie turns ilosc tur do nastepnej immigration
		
        String header = Messages.message(StringTemplate.template("recruitPanel.clickOn")
            .addAmount("%money%", 123)
            .addAmount("%number%", 234));
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
