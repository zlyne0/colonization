package promitech.colonization.screen.map.hud;

import java.util.EnumMap;
import java.util.Map.Entry;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.viewport.Viewport;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.Unit.UnitState;
import net.sf.freecol.common.model.player.ChooseEmigrantToRecruitNotification;
import net.sf.freecol.common.model.player.MessageNotification;
import net.sf.freecol.common.model.player.MonarchActionNotification;
import net.sf.freecol.common.model.player.Notification;
import net.sf.freecol.common.model.player.RecruitFoundingFatherNotification;
import net.sf.freecol.common.model.specification.Ability;
import promitech.colonization.DI;
import promitech.colonization.Direction;
import promitech.colonization.GameResources;
import promitech.colonization.orders.BuildColonyOrder;
import promitech.colonization.orders.move.MoveController;
import promitech.colonization.screen.ApplicationScreenManager;
import promitech.colonization.screen.ApplicationScreenType;
import promitech.colonization.screen.TradeRouteListDialog;
import promitech.colonization.screen.ff.ChooseFoundingFatherDialog;
import promitech.colonization.screen.debug.DebugShortcutsKeys;
import promitech.colonization.screen.map.MapActor;
import promitech.colonization.screen.map.hud.GUIGameModel.ChangeStateListener;
import promitech.colonization.ui.ModalDialog;

public class HudStage extends Stage {
    private static Direction[][] BUTTON_DIRECTIONS = new Direction[][] { 
        {Direction.SW, Direction.S, Direction.SE}, 
        {Direction.W, null, Direction.E},
        {Direction.NW, Direction.N, Direction.NE},
    };

    private static IntMap<Direction> directionByKeyCode = new IntMap<Direction>(8);
    static {
    	directionByKeyCode.put(Input.Keys.Q, Direction.NW);
    	directionByKeyCode.put(Input.Keys.W, Direction.N);
    	directionByKeyCode.put(Input.Keys.E, Direction.NE);
    	directionByKeyCode.put(Input.Keys.D, Direction.E);
    	directionByKeyCode.put(Input.Keys.C, Direction.SE);
    	directionByKeyCode.put(Input.Keys.X, Direction.S);
    	directionByKeyCode.put(Input.Keys.Z, Direction.SW);
    	directionByKeyCode.put(Input.Keys.A, Direction.W);
    }
    
    private final ShapeRenderer shapeRenderer;
    public final HudInfoPanel hudInfoPanel; 
    private final GUIGameController gameController;
    private final MoveController moveController;
    private final GUIGameModel guiGameModel;
    private final ApplicationScreenManager screenManager;
    private final DebugShortcutsKeys debugShortcutsKeys;
    
    private final EndOfTurnActor endOfTurnActor; 
    
    private final EnumMap<Direction, ButtonActor> directionButtons = new EnumMap<Direction, ButtonActor>(Direction.class);
    private ButtonActor centerOnUnitButton;
    private ButtonActor unitWaitButton;
    private ButtonActor sentryButton;
    private ButtonActor fortifyButton;
    private ButtonActor plowButton;
    private ButtonActor roadButton;
    private ButtonActor buildColonyButton;
    private RadioButtonActor gotoTileButton;
    private ButtonActor gotoLocationButton;
    private ButtonActor activeButton;
    
    private ButtonActor nextUnitButton;
    private RadioButtonActor viewButton;
    private ButtonActor europeButton;
    private ButtonActor tradeRouteButton;
    private ButtonActor endTurnButton;
    private ButtonActor showNotificationButton;

    private ButtonActor acceptActionButton;
    private ButtonActor cancelActionButton;
    
    private ButtonActor exitActionButton;
    
    private final Group buttonsGroup = new Group();
    private boolean needUpdateButtonVisibility = true;
    
    private GUIGameModel.ChangeStateListener guiGameModelChangeListener = new ChangeStateListener() {
		@Override
		public void change(GUIGameModel model) {
			needUpdateButtonVisibility = true;
		}
	};
    
	private final EventListener addInputListenerToStageEvent = new EventListener() {
		@Override
		public boolean handle(Event event) {
			HudStage.this.addListener(inputListener);
			return true;
		}
	};

    public HudStage(Viewport viewport, DI di, MapActor mapActor, ApplicationScreenManager screenManager) {
        super(viewport);
        this.gameController = di.guiGameController;
        this.moveController = di.moveController;
        this.guiGameModel = di.guiGameModel;
        this.shapeRenderer = new ShapeRenderer();
        this.screenManager = screenManager;

        debugShortcutsKeys = new DebugShortcutsKeys(this, di, mapActor);
        
        hudInfoPanel = new HudInfoPanel(GameResources.instance);
        hudInfoPanel.setMapActor(mapActor);
        
        addActor(buttonsGroup);
        addActor(hudInfoPanel);
        
        createActionButtons();
        updateLayout();
        
		addListener(inputListener);
        
		guiGameModel.addChangeListener(guiGameModelChangeListener);
        
		endOfTurnActor = new EndOfTurnActor(shapeRenderer);
		endOfTurnActor.setWidth(getWidth());
		endOfTurnActor.setHeight(getHeight());
    }

    public void showDialog(final Dialog dialog) {
    	Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				dialog.show(HudStage.this);
			}
    	});
    }

    public void showDialog(final ModalDialog<?> modalDialog) {
    	Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				HudStage.this.removeListener(inputListener);    	
				modalDialog.addOnCloseListener(HudStage.this.addInputListenerToStageEvent);
				modalDialog.init(shapeRenderer);
				modalDialog.show(HudStage.this);
			}
		});
    }
    
    private final InputListener inputListener = new InputListener() {
    	
    	@Override
    	public boolean keyDown(InputEvent event, int keycode) {
    		if (keycode == Input.Keys.V && viewButton.getParent() != null) {
    			if (viewButton.isChecked()) {
    				gameController.leaveViewMode();
    			} else {
    				gameController.enterInViewMode();
    			}
    			return true;
    		}
    		if (keycode == Input.Keys.B && buildColonyButton.getParent() != null) {
    			gameController.buildColony();
    			return true;
    		}
    		if (keycode == Input.Keys.Y && europeButton.getParent() != null) {
    			gameController.showEuropeScreen();
    			return true;
    		}
    		if (keycode == Input.Keys.L && tradeRouteButton.getParent() != null) {
    			showCreateTradeRouteDialog();
    			return true;
    		}
    		if (keycode == Input.Keys.N && nextUnitButton.getParent() != null) {
    			gameController.nextActiveUnit();
    			return true;
    		}
    		
    		if (keycode == Input.Keys.G && gotoTileButton.getParent() != null) {
    			moveController.enterIntoCreateGotoPathMode();
    			return true;
    		}
    		
    		if (keycode == Input.Keys.H && gotoLocationButton.getParent() != null) {
    			showGotoLocationDialog();
    			return true;
    		}
    		
    		if (keycode == Input.Keys.ENTER) {
    			moveController.acceptAction();
    			return true;
    		}
    		
    		if (keycode == Input.Keys.SPACE && unitWaitButton.getParent() != null) {
    			gameController.skipUnit();
    		}
    		
    		if (keycode == Input.Keys.ESCAPE) {
    			gameController.cancelAction();
    			return true;
    		}
    		if (keycode == Input.Keys.R && roadButton.getParent() != null) {
    			gameController.buildRoad();
    			return true;
    		}
    		if (keycode == Input.Keys.P && plowButton.getParent() != null) {
    			gameController.plowOrClearForestImprovement();
    			return true;
    		}
    		if (keycode == Input.Keys.T && sentryButton.getParent() != null) {
    			gameController.sentryUnit();
    			return true;
    		}
    		if (keycode == Input.Keys.F && fortifyButton.getParent() != null) {
    			gameController.fortify();
    			return true;
    		}
    		if (keycode == Input.Keys.A && activeButton.getParent() != null) {
    			gameController.activeUnit();
    			return true;
    		} 
    		if (keycode == Input.Keys.S && centerOnUnitButton.getParent() != null) {
    			gameController.centerOnActiveUnit();
    			return true;
    		}
            if (debugShortcutsKeys.canHandleKey(keycode)) {
                debugShortcutsKeys.handleKey(keycode);
                return true;
            }
    		
    		Direction direction = directionByKeyCode.get(keycode);
    		if (direction != null) {
    			ButtonActor button = directionButtons.get(direction);
    			if (button.getParent() != null) {
    				moveController.pressDirectionKey(direction);
    				return true;
    			}
    		}
    		return super.keyDown(event, keycode);
    	}
    };
    
	private final InputListener buttonsInputListener = new InputListener() {
    	@Override
		public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
    		
    		for (Entry<Direction, ButtonActor> directionButton : directionButtons.entrySet()) {
    			if (directionButton.getValue() == event.getListenerActor()) {
    				moveController.pressDirectionKey(directionButton.getKey());
    				return true;
    			}
    		}
    		
    		if (event.getListenerActor() == viewButton) {
    			if (viewButton.isChecked()) {
    				gameController.leaveViewMode();
    			} else {
    				gameController.enterInViewMode();
    			}
    			return true;
    		}
    		
    		if (event.getListenerActor() == buildColonyButton) {
    			gameController.buildColony();
    			return true;
    		}
    		if (event.getListenerActor() == europeButton) {
    			gameController.showEuropeScreen();
    			return true;
    		}
    		if (event.getListenerActor() == tradeRouteButton) {
    			showCreateTradeRouteDialog();
    			return true;
    		}
    		
    		if (event.getListenerActor() == nextUnitButton) {
				gameController.nextActiveUnit();
    			return true;
    		}
    		
    		if (event.getListenerActor() == unitWaitButton) {
    			gameController.skipUnit();
    			return true;
    		}
    		
    		if (event.getListenerActor() == gotoTileButton) {
    			if (gotoTileButton.isChecked()) {
    				moveController.leaveCreateGotoPathMode();
    			} else {
    				moveController.enterIntoCreateGotoPathMode();
    			}
    			return true;
    		}
    		
    		if (event.getListenerActor() == gotoLocationButton) {
    			showGotoLocationDialog();
    			return true;
    		}
    		
    		if (event.getListenerActor() == acceptActionButton) {
    			moveController.acceptAction();
    			return true;
    		}
    		
    		if (event.getListenerActor() == cancelActionButton) {
    			gameController.cancelAction();
    			return true;
    		}
    		
    		if (event.getListenerActor() == endTurnButton) {
    			HudStage.this.addActor(endOfTurnActor);
    			endOfTurnActor.start(gameController);
    			return true;
    		}
    		
    		if (event.getListenerActor() == showNotificationButton) {
    			showNotification();
    			return true;
    		}
    		
    		if (event.getListenerActor() == roadButton) {
    			gameController.buildRoad();
    			return true;
    		}
    		if (event.getListenerActor() == plowButton) {
    			gameController.plowOrClearForestImprovement();
    			return true;
    		}
    		if (event.getListenerActor() == fortifyButton) {
    			gameController.fortify();
    			return true;
    		}
    		if (event.getListenerActor() == sentryButton) {
    			gameController.sentryUnit();
    			return true;
    		}
    		if (event.getListenerActor() == activeButton) {
    			gameController.activeUnit();
    			return true;
    		}
    		if (event.getListenerActor() == centerOnUnitButton) {
    			gameController.centerOnActiveUnit();
    			return true;
    		}
    		if (event.getListenerActor() == exitActionButton) {
    			screenManager.setScreen(ApplicationScreenType.MAIN_MENU);
    			return true;
    		}
    		return false;
    	}
    };	

	private void createActionButtons() {
        createDirectionButtons();
		
		centerOnUnitButton = new ButtonActor(shapeRenderer, "C");
		centerOnUnitButton.addListener(buttonsInputListener);

		unitWaitButton = new ButtonActor(shapeRenderer, "wait");		
		unitWaitButton.addListener(buttonsInputListener);
		
		sentryButton = new ButtonActor(shapeRenderer, "S");
		sentryButton.addListener(buttonsInputListener);
		
		fortifyButton = new ButtonActor(shapeRenderer, "F");
		fortifyButton.addListener(buttonsInputListener);
		
		activeButton = new ButtonActor(shapeRenderer, "A");
		activeButton.addListener(buttonsInputListener);
		
		buildColonyButton = new ButtonActor(shapeRenderer, "B");
		buildColonyButton.addListener(buttonsInputListener);
		plowButton = new ButtonActor(shapeRenderer, "P");
		plowButton.addListener(buttonsInputListener);
		roadButton = new ButtonActor(shapeRenderer, "R");
		roadButton.addListener(buttonsInputListener);
		
		gotoTileButton = new RadioButtonActor(shapeRenderer, "goto");
		gotoTileButton.addListener(buttonsInputListener);
		gotoLocationButton = new ButtonActor(shapeRenderer, "goto l.");
		gotoLocationButton.addListener(buttonsInputListener);
		
		nextUnitButton = new ButtonActor(shapeRenderer, "next");
        nextUnitButton.addListener(buttonsInputListener);
	
		viewButton = new RadioButtonActor(shapeRenderer, "V");
        viewButton.addListener(buttonsInputListener);

        europeButton = new ButtonActor(shapeRenderer, "Y");
        europeButton.addListener(buttonsInputListener);
        
        tradeRouteButton = new ButtonActor(shapeRenderer, "L");
        tradeRouteButton.addListener(buttonsInputListener);
        
        endTurnButton = new ButtonActor(shapeRenderer, "end turn");
        endTurnButton.addListener(buttonsInputListener);
        
        showNotificationButton = new ButtonActor(shapeRenderer, "msg");
        showNotificationButton.addListener(buttonsInputListener);
        
        acceptActionButton = new ButtonActor(shapeRenderer, "accept");
        acceptActionButton.addListener(buttonsInputListener);

        cancelActionButton = new ButtonActor(shapeRenderer, "cancel");
        cancelActionButton.addListener(buttonsInputListener);
    
        exitActionButton = new ButtonActor(shapeRenderer, "ESC");
        exitActionButton.addListener(buttonsInputListener);
	}

	public void updateLayout() {
		int bw = (int) (getHeight() * 0.33) / 3;
		
		layoutForDirectionButtons(bw);
		
		centerOnUnitButton.setSize(bw, bw);
		centerOnUnitButton.setPosition(bw, bw);
		
		unitWaitButton.setSize(bw, bw);
		unitWaitButton.setPosition(3*bw, 0);
		
		sentryButton.setSize(bw, bw);
		sentryButton.setPosition(4*bw, 0);
		fortifyButton.setSize(bw, bw);
		fortifyButton.setPosition(5*bw, 0);
		activeButton.setSize(bw, bw);
		activeButton.setPosition(5*bw, 0);
		buildColonyButton.setSize(bw, bw);
		buildColonyButton.setPosition(0, bw*4);
		plowButton.setSize(bw, bw);
		plowButton.setPosition(0, bw*5);
		roadButton.setSize(bw, bw);
		roadButton.setPosition(0, bw*6);
		gotoTileButton.setSize(bw, bw);
		gotoTileButton.setPosition(6*bw, 0);
		gotoLocationButton.setSize(bw, bw);
		gotoLocationButton.setPosition(7*bw, 0);
		nextUnitButton.setSize(bw, bw);
		nextUnitButton.setPosition(getWidth() - bw, getHeight() - 3*bw);
		viewButton.setSize(bw, bw);
		viewButton.setPosition(getWidth() / 2, getHeight() - bw - 10);
		europeButton.setSize(bw, bw);
		europeButton.setPosition(getWidth() / 2 - bw, getHeight() - bw - 10);
		tradeRouteButton.setSize(bw, bw);
		tradeRouteButton.setPosition(viewButton.getX() + bw, viewButton.getY());
		
		endTurnButton.setSize(bw, bw);
		endTurnButton.setPosition(getWidth() - bw, getHeight() - bw);
		showNotificationButton.setSize(bw, bw);
		showNotificationButton.setPosition(getWidth() - bw, getHeight() - bw);
		acceptActionButton.setSize(bw, bw);
		acceptActionButton.setPosition(0, getHeight() / 2);
		cancelActionButton.setSize(bw, bw);
		cancelActionButton.setPosition(getWidth() - bw, getHeight() / 2);

		exitActionButton.setSize(bw, bw);
		exitActionButton.setPosition(0, getHeight() - bw);
		
		hudInfoPanel.layout();
	}
	
	private void createDirectionButtons() {
		for (int y = 0; y < BUTTON_DIRECTIONS.length; y++) {
            for (int x = 0; x < BUTTON_DIRECTIONS[y].length; x++) {
                final Direction direction = BUTTON_DIRECTIONS[y][x];
                if (direction == null) {
                    continue;
                }
                ButtonActor button = new ButtonActor(shapeRenderer);
                button.addListener(new InputListener() {
                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    	moveController.pressDirectionKey(direction);
                        return true;
                    }
                });
                directionButtons.put(direction, button);
            }
        }
	}

	private void layoutForDirectionButtons(int bw) {
		for (int y = 0; y < BUTTON_DIRECTIONS.length; y++) {
            for (int x = 0; x < BUTTON_DIRECTIONS[y].length; x++) {
                final Direction direction = BUTTON_DIRECTIONS[y][x];
                if (direction == null) {
                    continue;
                }
                ButtonActor button = directionButtons.get(direction);
                if (button != null) {
                    button.setX(x * bw);
                    button.setY(y * bw);
                    button.setWidth(bw);
                    button.setHeight(bw);
                }
                directionButtons.put(direction, button);
            }
		}
	}
	
    @Override
    public void act() {
    	shapeRenderer.setProjectionMatrix(getBatch().getProjectionMatrix());
    	shapeRenderer.setTransformMatrix(getBatch().getTransformMatrix());
    	shapeRenderer.translate(getViewport().getScreenX(), getViewport().getScreenY(), 0);
    	
    	if (needUpdateButtonVisibility) {
    	    resetButtonVisibility(guiGameModel);
    	}
        super.act();
    }
    
	private void resetButtonVisibility(GUIGameModel model) {
	    needUpdateButtonVisibility = false;
	    
		// remove all actors
		buttonsGroup.clearChildren();
		if (model.isAiMove()) {
			return;
		}
		boolean hasUnitsToMove = model.hasUnitsToMove();
		boolean hasNotifications = model.hasNotifications();
		if (!hasUnitsToMove && !hasNotifications) {
			buttonsGroup.addActor(endTurnButton);
		}
        if (hasNotifications) {
        	buttonsGroup.addActor(showNotificationButton);
        }
		
        viewButton.setChecked(model.isViewMode());
        gotoTileButton.setChecked(model.isCreateGotoPathMode());
		
        if (!model.isCreateGotoPathMode()) {
        	
			if (model.isActiveUnitSet()) {
				Unit unit = model.getActiveUnit();
				
				buttonsGroup.addActor(centerOnUnitButton);
				
				if (model.getActiveUnit().getState() != UnitState.FORTIFIED) {
					for (Entry<Direction, ButtonActor> entry : directionButtons.entrySet()) {
						ButtonActor directionButton = entry.getValue();
						buttonsGroup.addActor(directionButton);
					}
					buttonsGroup.addActor(unitWaitButton);
					buttonsGroup.addActor(sentryButton);
					
					if (unit.unitType.isNaval() || unit.unitType.isWagonTrain()) {
					} else {
						if (unit.canChangeState(UnitState.FORTIFYING)) {
							buttonsGroup.addActor(fortifyButton);
						}

						if (unit.isAtTileLocation()) {
							if (new BuildColonyOrder(model.game.map).check(unit, unit.getTile()) == BuildColonyOrder.OrderStatus.OK) {
								buttonsGroup.addActor(buildColonyButton);
							}
							if (unit.hasAbility(Ability.IMPROVE_TERRAIN)) {
								buttonsGroup.addActor(plowButton);
								buttonsGroup.addActor(roadButton);
							}
						}
					}
					buttonsGroup.addActor(gotoLocationButton);
					buttonsGroup.addActor(gotoTileButton);
					
				} else {
					buttonsGroup.addActor(activeButton);
				}
				
			}
			
			if (!model.isViewMode() && hasUnitsToMove) {
				buttonsGroup.addActor(nextUnitButton);
			}
			buttonsGroup.addActor(viewButton);
			buttonsGroup.addActor(europeButton);
			buttonsGroup.addActor(tradeRouteButton);
			
			buttonsGroup.addActor(exitActionButton);
        }
		
        if (model.isCreateGotoPathMode()) {
        	buttonsGroup.addActor(acceptActionButton);
        	buttonsGroup.addActor(cancelActionButton);
        }
	}
	
    private void showGotoLocationDialog() {
		GoToCityDialogList gotoCityDialogList = new GoToCityDialogList(
			moveController,
			shapeRenderer,
			guiGameModel
		);
		HudStage.this.showDialog(gotoCityDialogList);
    }
	
    private void showCreateTradeRouteDialog() {
    	TradeRouteListDialog dialog = new TradeRouteListDialog(
			shapeRenderer, 
			guiGameModel.game.playingPlayer, 
			Game.idGenerator, 
			gameController,
			moveController
		);
    	HudStage.this.showDialog(dialog);
    }
    
	private void showNotification() {
		Notification notification = gameController.getFirstNotification();
		
		if (notification instanceof MessageNotification) {
			NotificationDialog dialog = new NotificationDialog(notification);
			HudStage.this.showDialog(dialog);
		}
		
		if (notification instanceof MonarchActionNotification) {
			MonarchActionNotificationDialog dialog = new MonarchActionNotificationDialog(
				guiGameModel.game,
				(MonarchActionNotification)notification
			);
			dialog.show(HudStage.this);
		}
		
		if (notification instanceof RecruitFoundingFatherNotification) {
			ChooseFoundingFatherDialog dialog = new ChooseFoundingFatherDialog(
				guiGameModel.game.playingPlayer, 
				guiGameModel.game.getTurn()
			);
			HudStage.this.showDialog(dialog);
		}
		
		if (notification instanceof ChooseEmigrantToRecruitNotification) {
			ChooseEmigrantToRecruitDialog dialog = new ChooseEmigrantToRecruitDialog(guiGameModel.game.playingPlayer);
			HudStage.this.showDialog(dialog);
		}
	}

}
