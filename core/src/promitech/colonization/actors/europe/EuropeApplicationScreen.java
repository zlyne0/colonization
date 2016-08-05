package promitech.colonization.actors.europe;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.badlogic.gdx.utils.Align;

import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.player.Notification;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.ApplicationScreen;
import promitech.colonization.ApplicationScreenType;
import promitech.colonization.actors.CarrierUnitsPanel;
import promitech.colonization.actors.ChangeColonyStateListener;
import promitech.colonization.actors.OutsideUnitsPanel;
import promitech.colonization.actors.UnitActor;
import promitech.colonization.gdx.Frame;
import promitech.colonization.ui.DoubleClickedListener;
import promitech.colonization.ui.UnitActionOrdersDialog;
import promitech.colonization.ui.UnitActionOrdersDialog.ActionTypes;
import promitech.colonization.ui.UnitActionOrdersDialog.UnitActionOrderItem;
import promitech.colonization.ui.hud.ButtonActor;
import promitech.colonization.ui.hud.NotificationDialog;
import promitech.colonization.ui.resources.Messages;

public class EuropeApplicationScreen extends ApplicationScreen {
	
	private class EuropeUnitOrders implements UnitActionOrdersDialog.UnitOrderExecutor {

		@Override
		public boolean executeCommand(UnitActor unitActor, UnitActionOrderItem item, UnitActionOrdersDialog dialog) {
			switch (item.actionType) {
			case SAIL_TO_NEW_WORLD:
				unitActor.unit.sailUnitToNewWorld();
				if (player.getEurope().isNoNavyInPort()) {
	        		gameController.showMapScreenAndActiveNextUnit();
				} else {
					changeColonyStateListener.changeUnitAllocation();
				}
				break;
			}
			return true;
		}
		
		private void createOrders(Unit unit, UnitActionOrdersDialog dialog) {
			if (unit.isNaval()) {
				dialog.addCommandItem(new UnitActionOrderItem("NewWorld", ActionTypes.SAIL_TO_NEW_WORLD));
			}
		}
	}
	
	private DragAndDrop goodsDragAndDrop;
	private DragAndDrop unitsDragAndDrop;
	
	private Stage stage;
	private MarketPanel marketPanel; 
	private CarrierUnitsPanel carrierUnitsPanel;
	private OutsideUnitsPanel outsideUnitsPanel;
	private MarketLog marketLog;
	private HighSeasUnitsPanel highSeasUnitsPanel;
	private final EuropeUnitOrders europeUnitOrders = new EuropeUnitOrders();
	
	private Player player;
	
	private final ChangeColonyStateListener changeColonyStateListener = new ChangeColonyStateListener() {
		@Override
		public void changeUnitAllocation() {
			marketPanel.init(player);
			carrierUnitsPanel.initUnits(player.getEurope());
			outsideUnitsPanel.initUnits(player.getEurope());
			highSeasUnitsPanel.initUnits(player.getHighSeas());
		}

		@Override
		public void transfereGoods() {
		}

		@Override
		public void changeBuildingQueue() {
		}

		@Override
		public void addNotification(Notification notification) {
			NotificationDialog dialog = new NotificationDialog(notification);
			dialog.show(EuropeApplicationScreen.this.stage);
		}
	};
	
    private final DoubleClickedListener unitActorDoubleClickListener = new DoubleClickedListener() {
        public void doubleClicked(InputEvent event, float x, float y) {
            UnitActor unitActor = (UnitActor)event.getListenerActor();
            showUnitOrders(unitActor);
        }
    };
	
	@Override
	public void create() {
        unitsDragAndDrop = new DragAndDrop();
        unitsDragAndDrop.setDragActorPosition(0, 0);
        unitsDragAndDrop.setTapSquareSize(3);
        
        goodsDragAndDrop = new DragAndDrop();
        goodsDragAndDrop.setDragActorPosition(0, 0);
        goodsDragAndDrop.setTapSquareSize(3);
		
		
		stage = new Stage();		
		marketLog = new MarketLog();
        marketPanel = new MarketPanel(gameController.getGame(), shape, goodsDragAndDrop, changeColonyStateListener, marketLog);
        carrierUnitsPanel = new CarrierUnitsPanel(shape, goodsDragAndDrop, changeColonyStateListener, unitActorDoubleClickListener);
        outsideUnitsPanel = new OutsideUnitsPanel(shape, unitsDragAndDrop, changeColonyStateListener, unitActorDoubleClickListener);
        highSeasUnitsPanel = new HighSeasUnitsPanel();
        
        Frame paperBackground = gameResources.getFrame("Paper");
        Table tableLayout = new Table();
        tableLayout.setBackground(new TiledDrawable(paperBackground.texture));            
        tableLayout.setFillParent(true);
        
        HorizontalGroup rowGroup2 = new HorizontalGroup();
        rowGroup2.addActor(highSeasUnitsPanel);
        rowGroup2.addActor(marketLog);
        tableLayout.add(rowGroup2).row();
        HorizontalGroup rowGroup1 = new HorizontalGroup();
        rowGroup1.addActor(carrierUnitsPanel);
        rowGroup1.addActor(outsideUnitsPanel);
        
        tableLayout.add(rowGroup1).row();
        tableLayout.add(marketPanel);

		Table buttonsLayout = createButtonsLayout();
        
        stage.addActor(tableLayout);
		stage.addActor(buttonsLayout);
        
        stage.setDebugAll(true);
	}

	protected void showUnitOrders(UnitActor unitActor) {
    	UnitActionOrdersDialog dialog = new UnitActionOrdersDialog(shape, unitActor, europeUnitOrders);
    	europeUnitOrders.createOrders(unitActor.unit, dialog);
    	dialog.show(stage);
	}

	private Table createButtonsLayout() {
		int bw = (int) (stage.getHeight() * 0.33) / 3;
		bw -= 10;
		
		ButtonActor closeButton = new ButtonActor(this.shape, "ESC");
		closeButton.setWidth(bw);
		closeButton.setHeight(bw);
		closeButton.addListener(new InputListener() {
        	@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        		screenManager.setScreen(ApplicationScreenType.MAP_VIEW);
        		return true;
        	}
        });
		
		ButtonActor recruitButton = new ButtonActor(this.shape, Messages.msg("recruit"));
		recruitButton.setWidth(bw);
		recruitButton.setHeight(bw);
		recruitButton.addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				ImmigrantsUnitsDialog buyableUnitsDialog = new ImmigrantsUnitsDialog(
					stage.getHeight() * 0.75f, 
					shape,
					player,
					changeColonyStateListener
				);
				buyableUnitsDialog.show(stage);
				
				System.out.println("event " + event);
				return true;
			}
		});

		ButtonActor purchaseButton = new ButtonActor(this.shape, Messages.msg("purchase"));
		purchaseButton.setWidth(bw);
		purchaseButton.setHeight(bw);
		purchaseButton.addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				BuyableUnitsDialog buyableUnitsDialog = new BuyableUnitsDialog(
						stage.getHeight() * 0.75f, 
						shape,
						Specification.instance.unitTypesPurchasedInEurope.sortedEntities(),
						player,
						changeColonyStateListener
					);
				buyableUnitsDialog.show(stage);
				return true;
			}
		});

		ButtonActor trainButton = new ButtonActor(this.shape, Messages.msg("train"));
		trainButton.setWidth(bw);
		trainButton.setHeight(bw);
		trainButton.addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				BuyableUnitsDialog buyableUnitsDialog = new BuyableUnitsDialog(
					stage.getHeight() * 0.75f, 
					shape,
					Specification.instance.unitTypesTrainedInEurope.sortedEntities(),
					player,
					changeColonyStateListener
				);
				buyableUnitsDialog.show(stage);
				return true;
			}
		});

		Table buttonsLayout = new Table();
		buttonsLayout.setFillParent(true);
		buttonsLayout.align(Align.top | Align.right);
		buttonsLayout.defaults().space(0).pad(10, 10, 0, 10);
		
		buttonsLayout.add(closeButton).row();
		buttonsLayout.add(recruitButton).row();
		buttonsLayout.add(purchaseButton).row();
		buttonsLayout.add(trainButton).row();
		return buttonsLayout;
	}
	
	public void init(Player player) {
		this.player = player;
		
		marketPanel.init(player);
		carrierUnitsPanel.initUnits(player.getEurope());
		outsideUnitsPanel.initUnits(player.getEurope());
		highSeasUnitsPanel.initUnits(player.getHighSeas());
	}
	
	@Override
	public void onShow() {
		Gdx.input.setInputProcessor(stage);
	}
	
	@Override
	public void onLeave() {
		Gdx.input.setInputProcessor(null);
	}
	
	@Override
	public void render() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
        stage.act();
        stage.draw();
	}
	
	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height, true);		
	}
}
