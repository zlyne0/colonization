package promitech.colonization.screen.colony;

import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SplitPane;
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
import net.sf.freecol.common.model.specification.BuildableType;
import net.sf.freecol.common.model.specification.GameOptions;
import net.sf.freecol.common.model.specification.RequiredGoods;
import promitech.colonization.GameResources;
import promitech.colonization.gdx.Frame;
import promitech.colonization.infrastructure.FontResource;
import promitech.colonization.screen.ui.ChangeColonyStateListener;
import promitech.colonization.ui.ClosableDialog;
import promitech.colonization.ui.ModalDialogSize;
import promitech.colonization.ui.STable;
import promitech.colonization.ui.STableSelectListener;
import promitech.colonization.ui.SimpleMessageDialog;
import promitech.colonization.ui.resources.Messages;
import promitech.colonization.ui.resources.StringTemplate;

class BuildItemDescActor extends Table {
	BuildItemDescActor(ColonyBuildingQueueItem item) {
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
		for (RequiredGoods requiredGood : item.getType().requiredGoods()) {
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

class BuildingQueueDialog extends ClosableDialog<BuildingQueueDialog> {

	private ShapeRenderer shape;
	private final Colony colony;
	private final Game game;
	private final ChangeColonyStateListener changeColonyStateListener;
	
	private STable buildableItemsLayout;
    private STable buildQueueLayout;
	private final ActualBuildableItemActor actualBuildableItemActor = new ActualBuildableItemActor();
	private TextButton buyButton;
    private int[] buildQueueItemsAligment = new int[] { Align.center, Align.left };
	
	BuildingQueueDialog(ShapeRenderer shape, Game game, Colony colony, ChangeColonyStateListener changeColonyStateListener) {
		super(ModalDialogSize.width75(), ModalDialogSize.height75());
		
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
	    buildableItemsLayout = new STable(shape);
	    buildableItemsLayout.addSelectListener(new STableSelectListener() {
            @Override
            public void onSelect(Object payload) {
                if (payload instanceof ColonyBuildingQueueItem) {
                    addToBuildingQueue((ColonyBuildingQueueItem)payload);
                } else {
                    throw new IllegalStateException("payload should be ColonyBuildingQueueItem but it is " + payload);
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

		buildQueueLayout = new STable(shape);
		buildQueueLayout.addSelectListener(new STableSelectListener() {
            @Override
            public void onSelect(Object payload) {
                if (payload instanceof ColonyBuildingQueueItem) {
                    removeFromBuildingQueue((ColonyBuildingQueueItem)payload);
                } else {
                    throw new IllegalStateException("payload should be ColonyBuildingQueueItem but it is " + payload);
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
		
		// wrap scrollpane in table for better layout look 
		SplitPane lists = new SplitPane(
			fillXTableWrapper(buildQueueScrollPane),
			fillXTableWrapper(buildableItemsScrollPane), 
			false, 
			GameResources.instance.getUiSkin()
		);
		
		buttonTableLayoutExtendX();
		getContentTable().add(actualBuildableItemActor).row();
		getContentTable().add(lists).expand().fill().pad(10);
		getButtonTable().add(buttonsPanel()).expandX().fillX();
        
		withHidingOnEsc();
	}
	
	private Table fillXTableWrapper(ScrollPane widget) {
		Table t = new Table();
		t.add(widget).align(Align.top)
			.fillX()
			.expand();
		return t;
	}
	
	private Actor buttonsPanel() {
		String buyMsg = Messages.msg("colonyPanel.buyBuilding");
		TextButton okButton = new TextButton("OK", GameResources.instance.getUiSkin());
		buyButton = new TextButton(buyMsg, GameResources.instance.getUiSkin());
		
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
		panel.add(buyButton).pad(0, 10, 10, 10).expandX().fillX();
		panel.add(okButton).pad(0, 10, 10, 10).expandX().fillX();
		return panel;
	}

	private void addBuildableItemToTableLayout(ColonyBuildingQueueItem item, STable sTable) {
        Image buildingImage = buildingImage(item);
        BuildItemDescActor desc = new BuildItemDescActor(item);
        sTable.addRow(item, buildQueueItemsAligment, buildingImage, desc);
	}

	private void removeFromBuildingQueue(ColonyBuildingQueueItem queueItem) {
		buildQueueLayout.removeSelectedItems();
		colony.removeItemFromBuildingQueue(queueItem);
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
		BuildableType firstBuildableItem = colony.getFirstItemInBuildingQueue();
		if (firstBuildableItem == null) {
			System.out.println("Nothing to buy");
			return;
		}
		int price = colony.getPriceForBuilding(firstBuildableItem);
		
		System.out.println("buy " + firstBuildableItem + " for price " + price);
		
		StringTemplate st = StringTemplate.template("payForBuilding.text")
				.addAmount("%amount%", price);
		
		SimpleMessageDialog confirmationDialog = new SimpleMessageDialog();
		confirmationDialog.withContent(st)
			.withButton("payForBuilding.no")
			.withButton("payForBuilding.yes", new SimpleMessageDialog.ButtonActionListener() {
				@Override
				public void buttonPressed(SimpleMessageDialog dialog) {
					buyItem();
					dialog.hide();
				}
			});

		showDialog(confirmationDialog);
	}
	
	private void buyItem() {
	    BuildableType firstBuildableItem = colony.getFirstItemInBuildingQueue();
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

	private Image buildingImage(ColonyBuildingQueueItem item) {
        Frame frame = GameResources.instance.getFrame(item.getId() + ".image");
        TextureRegionDrawable textureRegionDrawable = new TextureRegionDrawable(frame.texture);
        Image image = new Image(textureRegionDrawable, Scaling.none, Align.center);
        image.scaleBy(-0.2f);
        return image;
	}
	
	private void updateBuildingQueueItems() {
		buildQueueLayout.clear();
		for (ColonyBuildingQueueItem item : colony.buildingQueue) {
			addBuildableItemToTableLayout(item, buildQueueLayout);
		}
	}

	private void updateBuyButtonAvailability() {
		if (!Specification.options.getBoolean(GameOptions.PAY_FOR_BUILDING)) {
			System.out.println("Colony[" + colony.getId() + "] buy disabled because PAY_FOR_BUILDING option");
			buyButton.setDisabled(true);
			return;
		}
		BuildableType firstBuildableItem = colony.getFirstItemInBuildingQueue();
		if (firstBuildableItem == null) {
			System.out.println("Colony[" + colony.getId() + "] buy disabled because no item to build");
			buyButton.setDisabled(true);
			return;
		}
		int price = colony.getPriceForBuilding(firstBuildableItem);
		if (price == 0 || colony.getOwner().hasNotGold(price)) {
			System.out.println("Colony[" + colony.getId() + "] buy disabled because no gold " + price);
			buyButton.setDisabled(true);
		} else {
			buyButton.setDisabled(false);
		}
	}
	
	@Override
	public void show(Stage stage) {
		super.show(stage);
		resetPositionToCenter();
	}
	
}
