package promitech.colonization.screen.europe;

import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.ObjectWithId;
import net.sf.freecol.common.model.ProductionSummary;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.Unit.UnitState;
import net.sf.freecol.common.model.UnitRole;
import net.sf.freecol.common.model.UnitRoleLogic;
import net.sf.freecol.common.model.player.Notification;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.Ability;
import promitech.colonization.gdx.Frame;
import promitech.colonization.screen.ApplicationScreen;
import promitech.colonization.screen.ApplicationScreenType;
import promitech.colonization.screen.map.hud.ButtonActor;
import promitech.colonization.screen.map.hud.GUIGameController;
import promitech.colonization.screen.map.hud.NotificationDialog;
import promitech.colonization.screen.ui.ChangeColonyStateListener;
import promitech.colonization.screen.ui.GoodTransferActorBridge;
import promitech.colonization.screen.ui.PlayerGoldTaxYearLabel;
import promitech.colonization.screen.ui.UnitActionOrdersDialog;
import promitech.colonization.screen.ui.UnitActor;
import promitech.colonization.screen.ui.UnitsPanel;
import promitech.colonization.screen.ui.UnitActionOrdersDialog.ActionTypes;
import promitech.colonization.screen.ui.UnitActionOrdersDialog.UnitActionOrderItem;
import promitech.colonization.ui.DoubleClickedListener;
import promitech.colonization.ui.resources.Messages;

public class EuropeApplicationScreen extends ApplicationScreen {
	
	private class EuropeUnitOrders implements UnitActionOrdersDialog.UnitOrderExecutor {

		@Override
		public boolean executeCommand(UnitActor unitActor, UnitActionOrderItem item, UnitActionOrdersDialog dialog) {
			switch (item.actionType) {
			case SAIL_TO_NEW_WORLD:
				unitActor.unit.embarkUnitsFromLocation(unitActor.unit.getOwner().getEurope());
				unitActor.unit.sailUnitToNewWorld();
				if (player.getEurope().isNoNavyInPort()) {
					guiGameController.showMapScreenAndActiveNextUnit();
				} else {
					changeColonyStateListener.changeUnitAllocation();
				}
				break;
			case EQUIPPED:
				player.getEurope().changeUnitRole(game, unitActor.unit, item.newRole, marketLog);
				unitActor.updateTexture();
				break;

			case CLEAR_ORDERS:
				unitActor.unit.setState(UnitState.ACTIVE);
				break;
			case SENTRY: 
				unitActor.unit.setState(UnitState.SENTRY);
				break;
			}
			return true;
		}
		
		private void createOrders(Unit unit, UnitActionOrdersDialog dialog) {
			if (unit.isNaval()) {
				dialog.addCommandItem(new UnitActionOrderItem("NewWorld", ActionTypes.SAIL_TO_NEW_WORLD));
			}
			if (unit.isPerson()) {
				addEquippedRoles(unit, dialog);
			}
			
	        dialog.addCommandItemSeparator();
	        dialog.addCommandItem(new UnitActionOrderItem("clearUnitOrders", ActionTypes.CLEAR_ORDERS));
	        if (unit.canChangeState(UnitState.SENTRY)) {
	        	dialog.addCommandItem(new UnitActionOrderItem("sentryUnit", ActionTypes.SENTRY));
	        }
		}
		
	    private void addEquippedRoles(Unit unit, UnitActionOrdersDialog dialog) {
	        if (unit.hasAbility(Ability.CAN_BE_EQUIPPED)) {
	            List<UnitRole> avaliableRoles = unit.avaliableRoles(player.getEurope());
	            
	            System.out.println("avaliable roles size " + avaliableRoles.size());
	            for (UnitRole aRole : avaliableRoles) {
	                System.out.println("ur " + aRole);
	                if (unit.getUnitRole().equalsId(aRole)) {
	                    continue;
	                }
	                if (aRole.hasAbility(Ability.DRESS_MISSIONARY)) {
	                	dialog.addCommandItem(new UnitActionOrderItem(unit, aRole, ProductionSummary.EMPTY, ActionTypes.EQUIPPED));
	                	continue;
	                }
	                ProductionSummary required = UnitRoleLogic.requiredGoodsToChangeRole(unit, aRole);
	                if (player.market().canAffordFor(player, required)) {
	                	dialog.addCommandItem(new UnitActionOrderItem(unit, aRole, required, ActionTypes.EQUIPPED));
	                }
	            }
	        }
	    }
	}
	
	private DragAndDrop goodsDragAndDrop;
	private DragAndDrop unitsDragAndDrop;
	
	private Stage stage;
	private PlayerGoldTaxYearLabel playerGoldTaxYearLabel; 
	private MarketPanel marketPanel; 
	private UnitsPanel carrierUnitsPanel;
	private UnitsPanel outsideUnitsPanel;
	private MarketLog marketLog;
	private HighSeasUnitsPanel highSeasUnitsPanel;
	private final EuropeUnitOrders europeUnitOrders = new EuropeUnitOrders();
	
	private Game game;
	private Player player;
	
	private GUIGameController guiGameController;
	
	private final ChangeColonyStateListener changeColonyStateListener = new ChangeColonyStateListener() {
		@Override
		public void changeUnitAllocation() {
			marketPanel.init(player, game);
			carrierUnitsPanel.initUnits(player.getEurope(), Unit.CARRIER_UNIT_PREDICATE);
			outsideUnitsPanel.initUnits(player.getEurope(), Unit.NOT_CARRIER_UNIT_PREDICATE);
			highSeasUnitsPanel.initUnits(player);
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
		guiGameController = di.guiGameController;
		
        unitsDragAndDrop = new DragAndDrop();
        unitsDragAndDrop.setDragActorPosition(0, 0);
        unitsDragAndDrop.setTapSquareSize(3);
        
        goodsDragAndDrop = new DragAndDrop();
        goodsDragAndDrop.setDragActorPosition(0, 0);
        goodsDragAndDrop.setTapSquareSize(3);
		
		
        GoodTransferActorBridge goodTransferActorBridge = new GoodTransferActorBridge();
        
        stage = new Stage(new FitViewport(ApplicationScreen.PREFERED_SCREEN_WIDTH, ApplicationScreen.PREFERED_SCREEN_HEIGHT));
        
		marketLog = new MarketLog();
        marketPanel = new MarketPanel(shape, goodsDragAndDrop, changeColonyStateListener, marketLog, goodTransferActorBridge);
        carrierUnitsPanel = new UnitsPanel(Messages.msg("inPort"))
        		.withUnitChips(shape)
        		.withUnitDoubleClick(unitActorDoubleClickListener)
        		.withUnitFocus(shape, goodsDragAndDrop, changeColonyStateListener);
        
        outsideUnitsPanel = new UnitsPanel(Messages.msg("docks"))
        		.withUnitChips(shape)
        		.withUnitDoubleClick(unitActorDoubleClickListener);
        highSeasUnitsPanel = new HighSeasUnitsPanel();
        
		goodTransferActorBridge.set(marketPanel);
		goodTransferActorBridge.set(carrierUnitsPanel);
        
        Frame paperBackground = gameResources.getFrame("Paper");
        Table tableLayout = new Table();
        tableLayout.setBackground(new TiledDrawable(paperBackground.texture));            
        tableLayout.setFillParent(true);
        
        playerGoldTaxYearLabel = new PlayerGoldTaxYearLabel();
        tableLayout.add(playerGoldTaxYearLabel).row();
        
        Table buttonsLayout = createButtonsLayout();
        
        Table rowGroup2 = new Table();
        rowGroup2.add(highSeasUnitsPanel).expandX().fillX();
        rowGroup2.add(marketLog).expandX().fillX().top();
        rowGroup2.add(buttonsLayout).expandY().fillY();
        tableLayout.add(rowGroup2).expandX().fill().row();
        
        Table unitsPanel = new Table();
        unitsPanel.add(carrierUnitsPanel).fillX().expandX().row();
        unitsPanel.add(outsideUnitsPanel).expandX().fillX();
        
        tableLayout.add(unitsPanel).fillX().expandX().row();
        tableLayout.add(marketPanel).fillY().expandY();

        stage.addActor(tableLayout);
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
		buttonsLayout.top();
		
		buttonsLayout.add(closeButton).row();
		
		Table buyButtons = new Table();
		buyButtons.bottom();
		buyButtons.add(recruitButton).row();
		buyButtons.add(purchaseButton).row();
		buyButtons.add(trainButton).row();
		buttonsLayout.add(buyButtons)
			.fillY()
			.expandY()
			.row();
		return buttonsLayout;
	}
	
	public void init(Player player, Game game) {
		this.player = player;
		this.game = game;
		
		marketPanel.init(player, game);
		carrierUnitsPanel.initUnits(player.getEurope(), Unit.CARRIER_UNIT_PREDICATE);
		outsideUnitsPanel.initUnits(player.getEurope(), Unit.NOT_CARRIER_UNIT_PREDICATE);
		highSeasUnitsPanel.initUnits(player);
		
		playerGoldTaxYearLabel.init(player, game.getTurn());
	}
	
	@Override
	public void onShow() {
		resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		
		marketLog.clearLog();
		Gdx.input.setInputProcessor(stage);
	}
	
	@Override
	public void onLeave() {
		Gdx.input.setInputProcessor(null);
		super.onLeave();
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
