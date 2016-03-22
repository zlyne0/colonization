package promitech.colonization.actors.colony;

import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.ColonyBuildingQueueItem;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.specification.GameOptions;
import net.sf.freecol.common.model.specification.RequiredGoods;
import promitech.colonization.GameResources;
import promitech.colonization.gdx.Frame;
import promitech.colonization.infrastructure.FontResource;
import promitech.colonization.ui.DoubleClickedListener;
import promitech.colonization.ui.ClosableDialog;
import promitech.colonization.ui.SelectableRowTable;
import promitech.colonization.ui.SelectableTableItem;
import promitech.colonization.ui.SimpleMessageDialog;
import promitech.colonization.ui.resources.Messages;
import promitech.colonization.ui.resources.StringTemplate;

class BuildItemImgActor extends SelectableTableItem<ColonyBuildingQueueItem> {
	BuildItemImgActor(ColonyBuildingQueueItem item) {
		super(item);
		Image image = new Image(getItemTexture(item), Scaling.none, Align.center);
		image.scaleBy(-0.2f);
		add(image).expand().fill();
	}
	
	private static TextureRegionDrawable getItemTexture(ColonyBuildingQueueItem item) {
		Frame frame = GameResources.instance.getFrame(item.getId() + ".image");
		return new TextureRegionDrawable(frame.texture);
	}
}

class BuildItemActor extends SelectableTableItem<ColonyBuildingQueueItem> {
	BuildItemActor(ColonyBuildingQueueItem item) {
		super(item);
		
		Label nameLabel = nameLabel(item);
		nameLabel.setAlignment(Align.bottomLeft);
		add(nameLabel)
			.expand().fill()
			.bottom()
			.row();

		add(requiredGoods(item))
			.top().left()
			.fill().expand();
	}

	private Label nameLabel(ColonyBuildingQueueItem item) {
		StringTemplate t1 = StringTemplate.template(item.getId() + ".name")
				.addAmount("%number%", 1);
		String msg = Messages.message(t1);
		return new Label(msg, labelStyle());
	}
	
	private Table requiredGoods(ColonyBuildingQueueItem item) {
		
		Table goodsLayout = new Table();
		goodsLayout.align(Align.left);
		for (RequiredGoods requiredGood : item.requiredGoods()) {
			Image goodImage = goodsImage(requiredGood.getId());
			goodImage.setAlign(Align.topLeft);
			
			Label goodLabel = new Label(Integer.toString(requiredGood.amount), labelStyle());
			goodLabel.setAlignment(Align.topLeft);
			
			goodsLayout.add(goodImage).expandY().fillY();
			goodsLayout.add(goodLabel).expand().fill();
		}
		return goodsLayout;
	}
	
    private Image goodsImage(String goodsTypeId) {
        Frame resGoodsImage = GameResources.instance.goodsImage(goodsTypeId);
        return new Image(new TextureRegionDrawable(resGoodsImage.texture), Scaling.none, Align.center);
    }
	
    private LabelStyle labelStyle() {
        LabelStyle labelStyle = GameResources.instance.getUiSkin().get(LabelStyle.class);
        labelStyle.font = FontResource.getGoodsQuantityFont();
        return labelStyle;
    }
}

class BuildingQueueDialog extends ClosableDialog {

	private ShapeRenderer shape;
	private final Colony colony;
	private final Game game;
	private final ChangeColonyStateListener changeColonyStateListener;
	
	private SelectableRowTable buildableItemsLayout;
	private SelectableRowTable buildQueueLayout;
	private final ActualBuildableItemActor actualBuildableItemActor = new ActualBuildableItemActor();
	private final Table dialogLayout = new Table();
	private TextButton buyButton;
	
	BuildingQueueDialog(float maxHeight, ShapeRenderer shape, Game game, Colony colony, ChangeColonyStateListener changeColonyStateListener) {
		super("", GameResources.instance.getUiSkin(), maxHeight);
		this.game = game;
		this.colony = colony;
		this.shape = shape;
		this.changeColonyStateListener = changeColonyStateListener;
		
		createComponents();
        
		updateBuildingQueueItems();
        updateBuildableItems();
        actualBuildableItemActor.updateBuildItem(colony);
		updateBuyButtonAvailability();
	}

	private void createComponents() {
		buildableItemsLayout = new SelectableRowTable(shape);
		buildableItemsLayout.setDoubleClickedListener(new DoubleClickedListener() {
			@Override
			public void doubleClicked(InputEvent event, float x, float y) {
				if (event.getListenerActor() instanceof SelectableTableItem) {
					SelectableTableItem<ColonyBuildingQueueItem> tableItem = (SelectableTableItem)event.getListenerActor();
					addToBuildingQueue(tableItem.payload);
				}
			}
		});
		ScrollPane buildableItemsScrollPane = new ScrollPane(buildableItemsLayout, GameResources.instance.getUiSkin());
		buildableItemsScrollPane.setFlickScroll(false);
		buildableItemsScrollPane.setScrollingDisabled(true, false);
		buildableItemsScrollPane.setForceScroll(false, false);
		buildableItemsScrollPane.setFadeScrollBars(false);
		buildableItemsScrollPane.setOverscroll(true, true);
		buildableItemsScrollPane.setScrollBarPositions(false, true);

		
		buildQueueLayout = new SelectableRowTable(shape);
		buildQueueLayout.setDoubleClickedListener(new DoubleClickedListener() {
			@Override
			public void doubleClicked(InputEvent event, float x, float y) {
				if (event.getListenerActor() instanceof SelectableTableItem) {
					SelectableTableItem<ColonyBuildingQueueItem> tableItem = (SelectableTableItem)event.getListenerActor();
					removeFromBuildingQueue(tableItem);
				}
			}
		});
		ScrollPane buildQueueScrollPane = new ScrollPane(buildQueueLayout, GameResources.instance.getUiSkin());
		buildQueueScrollPane.setFlickScroll(false);
		buildQueueScrollPane.setScrollingDisabled(true, false);
		buildQueueScrollPane.setForceScroll(false, false);
		buildQueueScrollPane.setFadeScrollBars(false);
		buildQueueScrollPane.setOverscroll(true, true);
		buildQueueScrollPane.setScrollBarPositions(false, true);
		
		dialogLayout.add(actualBuildableItemActor)
			.expandX()
			.colspan(2)
			.row();
		
		dialogLayout.add(buildQueueScrollPane).pad(20).top();
		dialogLayout.add(buildableItemsScrollPane).pad(20).row();
		
		getContentTable().add(dialogLayout);
		getButtonTable().add(buttonsPanel()).expandX();
        
		withHidingOnEsc();
	}
	
	private Actor buttonsPanel() {
		String buyMsg = Messages.msg("colonyPanel.buyBuilding");
		TextButton okButton = new TextButton("OK", GameResources.instance.getUiSkin());
		buyButton = new TextButton(buyMsg, GameResources.instance.getUiSkin());
		
		okButton.align(Align.right);
		buyButton.align(Align.left);

		okButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				hideWithFade();
			}
		});
		buyButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				buyItemButtonAction();
			}
		});
		
		Table panel = new Table();
		panel.setFillParent(true);
		panel.add(buyButton).left().pad(0, 20, 20, 20);
		panel.add(okButton).right().pad(0, 20, 20, 20);
		return panel;
	}

	private void addBuildableItemToTableLayout(ColonyBuildingQueueItem item, SelectableRowTable table) {
		BuildItemImgActor img = new BuildItemImgActor(item);
		BuildItemActor desc = new BuildItemActor(item);
		
		table.addCell(img)
			.fill().expand();
		table.addCell(desc)
			.expand().fill()
			.align(Align.left);
		table.nextRow();
	}

	private void removeFromBuildingQueue(SelectableTableItem<ColonyBuildingQueueItem> tableItem) {
		buildQueueLayout.removeSelectableItems(tableItem);
		List<ColonyBuildingQueueItem> unitItemObjects = buildQueueLayout.getItemsObjectsFromUniqueRows();
		colony.initBuildingQueue(unitItemObjects);
		updateBuildingQueueItems();
		updateBuildableItems();
		
		actualBuildableItemActor.updateBuildItem(colony);
		updateBuyButtonAvailability();
		changeColonyStateListener.changeBuildingQueue();
	}
	
	private void addToBuildingQueue(ColonyBuildingQueueItem queueItem) {
		colony.addItemToBuildingQueue(queueItem);
		updateBuildableItems();
		addBuildableItemToTableLayout(queueItem, buildQueueLayout);
		
		actualBuildableItemActor.updateBuildItem(colony);
		updateBuyButtonAvailability();
		changeColonyStateListener.changeBuildingQueue();
	}

	private void buyItemButtonAction() {
		System.out.println("buy building button action");
		ColonyBuildingQueueItem firstBuildableItem = colony.getFirstBuildableItem();
		
		if (firstBuildableItem == null) {
			System.out.println("Nothing to buy");
			return;
		}
		int price = colony.getPriceForBuilding(firstBuildableItem);
		
		System.out.println("buy " + firstBuildableItem + " for price " + price);
		
		StringTemplate st = StringTemplate.template("payForBuilding.text")
				.addAmount("%amount%", price);
		
		final SimpleMessageDialog confirmationDialog = new SimpleMessageDialog("", GameResources.instance.getUiSkin());
		this.addChildDialog(confirmationDialog);
		confirmationDialog.withContant(st)
			.withButton("payForBuilding.no")
			.withButton("payForBuilding.yes", new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					buyItem();
					confirmationDialog.hide();
				}
			});
		confirmationDialog.show(getStage());
	}
	
	private void buyItem() {
		ColonyBuildingQueueItem firstBuildableItem = colony.getFirstBuildableItem();
		if (firstBuildableItem == null) {
			throw new IllegalArgumentException("there is not item to build");
		}
		System.out.println("confirmed buy building " + firstBuildableItem);
		colony.payForBuilding(firstBuildableItem, game);
		
		actualBuildableItemActor.updateBuildItem(colony);
		updateBuyButtonAvailability();
		
		changeColonyStateListener.transfereGoods();
	}
	
	private void updateBuildableItems() {
		buildableItemsLayout.clear();
		
		System.out.println("generate buildable units and buildings");
        List<ColonyBuildingQueueItem> buildableItems = new LinkedList<ColonyBuildingQueueItem>(); 
        colony.buildableUnits(buildableItems);
        colony.buildableBuildings(buildableItems);
        for (ColonyBuildingQueueItem item : buildableItems) {
        	System.out.println("item: " + item);
        	addBuildableItemToTableLayout(item, buildableItemsLayout);
        }
	}

	private void updateBuildingQueueItems() {
		buildQueueLayout.clear();
		for (ColonyBuildingQueueItem item : colony.buildingQueue) {
			addBuildableItemToTableLayout(item, buildQueueLayout);
		}
	}

	private void updateBuyButtonAvailability() {
		if (!Specification.options.getBoolean(GameOptions.PAY_FOR_BUILDING)) {
			buyButton.setDisabled(true);
			return;
		}
		ColonyBuildingQueueItem firstBuildableItem = colony.getFirstBuildableItem();
		if (firstBuildableItem == null) {
			buyButton.setDisabled(true);
			return;
		}
		int price = colony.getPriceForBuilding(firstBuildableItem);
		if (price == 0 || colony.getOwner().hasNotGold(price)) {
			buyButton.setDisabled(true);
		} else {
			buyButton.setDisabled(false);
		}
	}
}
