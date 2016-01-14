package promitech.colonization.actors.colony;

import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
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
import net.sf.freecol.common.model.specification.RequiredGoods;
import promitech.colonization.GameResources;
import promitech.colonization.gdx.Frame;
import promitech.colonization.infrastructure.FontResource;
import promitech.colonization.ui.ClosableDialogAdapter;
import promitech.colonization.ui.CloseableDialog;
import promitech.colonization.ui.DoubleClickedListener;
import promitech.colonization.ui.SelectableRowTable;
import promitech.colonization.ui.SelectableTableItem;
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

class BuildingQueueDialog extends Dialog implements CloseableDialog {

	private final ClosableDialogAdapter closeableDialogAdapter = new ClosableDialogAdapter();
	private ShapeRenderer shape;
	private final Colony colony;
	
	private SelectableRowTable buildableItemsLayout;
	private SelectableRowTable buildQueueLayout;
	private final ActualBuildableItemActor actualBuildableItemActor = new ActualBuildableItemActor();
	private final Table dialogLayout = new Table();
	
	BuildingQueueDialog(ShapeRenderer shape, Colony colony) {
		super("", GameResources.instance.getUiSkin());
		this.colony = colony;
		this.shape = shape;
		
		createComponents();
        
		updateBuildingQueueItems();
        updateBuildableItems();
        actualBuildableItemActor.updateBuildItem(colony);
	}

	private void createComponents() {
		buildableItemsLayout = new SelectableRowTable(shape);
		buildableItemsLayout.setDoubleClickedListener(new DoubleClickedListener() {
			@Override
			public void doubleClicked(InputEvent event, float x, float y) {
				if (event.getListenerActor() instanceof SelectableTableItem) {
					SelectableTableItem<ColonyBuildingQueueItem> tableItem = (SelectableTableItem)event.getListenerActor();
					addToBuildingQueue(tableItem.object);
				}
			}
		});
		ScrollPane buildableItemsScrollPane = new ScrollPane(buildableItemsLayout, GameResources.instance.getUiSkin()) {
			@Override
			public float getMaxHeight() {
				return 500;
			}
		};
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
		ScrollPane buildQueueScrollPane = new ScrollPane(buildQueueLayout, GameResources.instance.getUiSkin()) {
			@Override
			public float getMaxHeight() {
				return 500;
			}
			
			@Override
			public float getMinWidth() {
				return 300;
			}
		};
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
        
        addListener(new InputListener() {
            public boolean keyDown (InputEvent event, int keycode2) {
                if (Keys.ENTER == keycode2) {
                	hideWithFade();
                }
                if (Keys.ESCAPE == keycode2) {
                	hideWithFade();
                }
                return false;
            }
        });
	}
	
	private Actor buttonsPanel() {
		String buyMsg = Messages.msg("colonyPanel.buyBuilding");
		TextButton okButton = new TextButton("OK", GameResources.instance.getUiSkin());
		TextButton buyButton = new TextButton(buyMsg, GameResources.instance.getUiSkin());
		
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
				buyItem();
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
	}
	
	private void addToBuildingQueue(ColonyBuildingQueueItem queueItem) {
		colony.addItemToBuildingQueue(queueItem);
		updateBuildableItems();
		addBuildableItemToTableLayout(queueItem, buildQueueLayout);
		
		actualBuildableItemActor.updateBuildItem(colony);
	}

	private void buyItem() {
		System.out.println("buy building");
		ColonyBuildingQueueItem firstBuildableItem = colony.getFirstBuildableItem();
		if (firstBuildableItem != null) {
			System.out.println("buy = " + firstBuildableItem);
			System.out.println("price = " + colony.getPriceForBuilding(firstBuildableItem));
		}
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

	@Override
	public float getMaxHeight() {
		return 500;// this.getStage().getHeight() * 0.75f;
	}
	
    void hideWithFade() {
    	hide();
    	closeableDialogAdapter.executeCloseListener();
    }
    
    void hideWithoutFade() {
    	hide(null);
    	closeableDialogAdapter.executeCloseListener();
    }

	@Override
	public void clickOnDialogStage(InputEvent event, float x, float y) {
		if (closeableDialogAdapter.canCloseBecauseClickOutsideDialog(this, x, y)) {
			hideWithFade();
		}
	}

	@Override
	public void addOnCloseEvent(EventListener dialogOnCloseListener) {
		closeableDialogAdapter.addCloseListener(dialogOnCloseListener);
	}
}
