package promitech.colonization.screen.colony;

import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.badlogic.gdx.utils.Scaling;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.GoodsContainer;
import net.sf.freecol.common.model.ProductionSummary;
import net.sf.freecol.common.model.specification.BuildableType;
import net.sf.freecol.common.model.specification.RequiredGoods;
import promitech.colonization.GameResources;
import promitech.colonization.gdx.Frame;
import promitech.colonization.infrastructure.FontResource;
import promitech.colonization.ui.resources.Messages;
import promitech.colonization.ui.resources.StringTemplate;

class ActualBuildableItemActor extends Table {
	private final Image itemImage;
	private final Label buildItemNameLabel;
	private final Label turnsToCompleteLabel;
	
	private final Table descriptionTableLayout = new Table();
	
	ActualBuildableItemActor() {
		buildItemNameLabel = new Label("", labelStyle());
		turnsToCompleteLabel = new Label("", labelStyle());

		itemImage = new Image();
		itemImage.setScaling(Scaling.none);
		itemImage.setAlign(Align.center);
		itemImage.scaleBy(-0.2f);
		
		add(itemImage).right();
		add(descriptionTableLayout);
	}

	void updateBuildItem(Colony colony) {
	    BuildableType item = colony.getFirstItemInBuildingQueue();
		if (item == null) {
			cleanItem();
			return;
		}
		
		itemImage.setDrawable(textureById(item.getId() + ".image"));

		
		ProductionSummary production = colony.productionSummary();
		GoodsContainer warehouse = colony.getGoodsContainer();
		
		descriptionTableLayout.clear();
		descriptionTableLayout.add(buildItemNameLabel).left().row();
		descriptionTableLayout.add(turnsToCompleteLabel).left().row();
		
		ObjectIntMap<String> requiredTurnsForGoods = new ObjectIntMap<String>(2);
		int turnsToComplete = colony.getTurnsToComplete(item, requiredTurnsForGoods);
		
		for (RequiredGoods requiredGood : item.requiredGoods()) {
			int warehouseAmount = warehouse.goodsAmount(requiredGood.getId());
			int productionAmount = production.getQuantity(requiredGood.getId());
			int goodRequiredTurn = requiredTurnsForGoods.get(requiredGood.getId(), Colony.NEVER_COMPLETE_BUILD);
			
			Image goodImage = new Image(textureById(requiredGood.getId() + ".image"), Scaling.none, Align.center);
			goodImage.setAlign(Align.topLeft);

			//label: 24 + 6/120 (Turns: 12)
			String label = goodRequiredTurnsStr(requiredGood.amount, warehouseAmount, productionAmount, goodRequiredTurn);
			
			Label goodLabel = new Label(label, labelStyle());
			goodLabel.setAlignment(Align.topLeft);
			
			HorizontalGroup resourcesLayout = new HorizontalGroup();
			resourcesLayout.addActor(goodImage);
			resourcesLayout.addActor(goodLabel);
			descriptionTableLayout.add(resourcesLayout).left().row();
		}

		StringTemplate stringTemplate = StringTemplate.template(item.getId() + ".name");
		buildItemNameLabel.setText(Messages.message(stringTemplate));
		
		updateTurnsToCompleteLabel(turnsToComplete);
	}

	private void cleanItem() {
		itemImage.setDrawable(null);
		buildItemNameLabel.setText("");
		turnsToCompleteLabel.setText("");
		descriptionTableLayout.clear();
	}

	private void updateTurnsToCompleteLabel(int requiredTurn) {
		String trunsToCompleteStr;
		if (requiredTurn == Colony.NEVER_COMPLETE_BUILD) {
			trunsToCompleteStr = "> -1";
		} else {
			trunsToCompleteStr = Integer.toString(requiredTurn);
		}
		StringTemplate stringTemplate = StringTemplate.template("turnsToComplete.long")
				.add("%number%", trunsToCompleteStr);
		turnsToCompleteLabel.setText(Messages.message(stringTemplate));
	}

	private String goodRequiredTurnsStr(int requiredGood, int warehouseAmount, int productionAmount, int requiredTurn) {
		String label = "" + warehouseAmount + " + " + productionAmount + "/" + requiredGood;
		if (requiredTurn == Colony.NEVER_COMPLETE_BUILD) {
			StringTemplate stringTemplate = StringTemplate.template("turnsToComplete.short");
			stringTemplate.addKey("%number%", "notApplicable.short");
			label += " " + Messages.message(stringTemplate); 
		} else {
			StringTemplate stringTemplate = StringTemplate.template("turnsToComplete.short")
					.addAmount("%number%", requiredTurn);
			label += " " + Messages.message(stringTemplate);
		}
		return label;
	}
	
    private LabelStyle labelStyle() {
        LabelStyle labelStyle = GameResources.instance.getUiSkin().get(LabelStyle.class);
        labelStyle.font = FontResource.getGoodsQuantityFont();
        return labelStyle;
    }	
	
	private TextureRegionDrawable textureById(String imageId) {
		Frame frame = GameResources.instance.getFrame(imageId);
		return new TextureRegionDrawable(frame.texture);
	}
} 

