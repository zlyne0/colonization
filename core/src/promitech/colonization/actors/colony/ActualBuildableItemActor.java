package promitech.colonization.actors.colony;

import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.ColonyBuildingQueueItem;
import net.sf.freecol.common.model.GoodsContainer;
import net.sf.freecol.common.model.ProductionSummary;
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
		ColonyBuildingQueueItem item = colony.getFirstBuildableItem();
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
		
		boolean potentialNeverFinish = false; // because no required good production
		int requiredTurn = -1;
		for (RequiredGoods requiredGood : item.requiredGoods()) {
			int warehouseAmount = warehouse.goodsAmount(requiredGood.getId());
			int productionAmount = production.getQuantity(requiredGood.getId());
			int goodRequiredTurn = -1;
			
			if (warehouseAmount < requiredGood.amount) {
				if (productionAmount > 0) {
					goodRequiredTurn = (requiredGood.amount - warehouseAmount) / productionAmount;
				} else {
					goodRequiredTurn = -1;
				}
			} else {
				goodRequiredTurn = 0;
			}
			
			if (goodRequiredTurn >= 0) {
				if (goodRequiredTurn > requiredTurn) {
					requiredTurn = goodRequiredTurn;
				}
			} else {
				potentialNeverFinish = true;
			}
			
			Image goodImage = new Image(textureById(requiredGood.getId() + ".image"), Scaling.none, Align.center);
			goodImage.setAlign(Align.topLeft);

			//24 + 6/120 (Turns: 12)
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
		
		updateTurnsToCompleteLabel(potentialNeverFinish, requiredTurn);
	}

	private void cleanItem() {
		itemImage.setDrawable(null);
		buildItemNameLabel.setText("");
		turnsToCompleteLabel.setText("");
		descriptionTableLayout.clear();
	}

	private void updateTurnsToCompleteLabel(boolean potentialNeverFinish, int requiredTurn) {
		String trunsToCompleteStr = Integer.toString(requiredTurn);
		if (potentialNeverFinish) {
			trunsToCompleteStr = ">" + trunsToCompleteStr;
		}
		StringTemplate stringTemplate = StringTemplate.template("turnsToComplete.long")
				.addName("%number%", trunsToCompleteStr);
		turnsToCompleteLabel.setText(Messages.message(stringTemplate));
	}

	private String goodRequiredTurnsStr(int requiredGood, int warehouseAmount, int productionAmount, int requiredTurn) {
		String label = "" + warehouseAmount + " + " + productionAmount + "/" + requiredGood;
		if (requiredTurn >= 0) {
			StringTemplate stringTemplate = StringTemplate.template("turnsToComplete.short")
					.addAmount("%number%", requiredTurn);
			label += " " + Messages.message(stringTemplate);
		} else {
			StringTemplate stringTemplate = StringTemplate.template("turnsToComplete.short");
			stringTemplate.add("%number%", "notApplicable.short");
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

