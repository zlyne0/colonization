package promitech.colonization.ui.hud;

import java.util.EnumMap;
import java.util.Map.Entry;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.viewport.Viewport;

import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.specification.Ability;
import promitech.colonization.Direction;
import promitech.colonization.GUIGameController;
import promitech.colonization.GUIGameModel;
import promitech.colonization.GUIGameModel.ChangeStateListener;
import promitech.colonization.GameResources;

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
    
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    public final HudInfoPanel hudInfoPanel; 
    private final GUIGameController gameController;
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
    
    private ButtonActor nextUnitButton;
    private RadioButtonActor viewButton;
    private ButtonActor endTurnButton;

    private ButtonActor acceptActionButton;
    private ButtonActor cancelActionButton;
    
    private final Group buttonsGroup = new Group();
    
    private GUIGameModel.ChangeStateListener guiGameModelChangeListener = new ChangeStateListener() {
		@Override
		public void change(GUIGameModel model) {
			resetButtonVisibility(model);
		}
	};
    
    public HudStage(Viewport viewport, final GUIGameController gameController, GameResources gameResources) {
        super(viewport);
        this.gameController = gameController;

        int bw = (int) (getHeight() * 0.33) / 3;
        hudInfoPanel = new HudInfoPanel(gameResources);
        
        addActor(buttonsGroup);
        addActor(hudInfoPanel);
        
        createActionButtons(bw);
		addListener(keysInputListener);
        
        gameController.addGUIGameModelChangeListener(guiGameModelChangeListener);
        
		endOfTurnActor = new EndOfTurnActor(shapeRenderer, gameController);
		endOfTurnActor.setWidth(getWidth());
		endOfTurnActor.setHeight(getHeight());
    }

    private final InputListener keysInputListener = new InputListener() {
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
    		if (keycode == Input.Keys.N && nextUnitButton.getParent() != null) {
    			gameController.nextActiveUnit();
    			return true;
    		}
    		
    		if (keycode == Input.Keys.G && gotoTileButton.getParent() != null) {
    			gameController.enterIntoCreateGotoPathMode();
    			return true;
    		}
    		
    		if (keycode == Input.Keys.H && gotoLocationButton.getParent() != null) {
    			showGotoLocationDialog();
    			return true;
    		}
    		
    		if (keycode == Input.Keys.ENTER) {
    			gameController.acceptAction();
    			return true;
    		}
    		
    		if (keycode == Input.Keys.SPACE && unitWaitButton.getParent() != null) {
    			gameController.skipUnit();
    		}
    		
    		if (keycode == Input.Keys.ESCAPE) {
    			gameController.cancelAction();
    			return true;
    		}
    		if (keycode == Input.Keys.R) {
    			gameController.buildRoad();
    			return true;
    		}
    		if (keycode == Input.Keys.P) {
    			gameController.plowOrClearForestImprovement();
    			return true;
    		}
    		
    		Direction direction = directionByKeyCode.get(keycode);
    		if (direction != null) {
    			ButtonActor button = directionButtons.get(direction);
    			if (button.getParent() != null) {
    				gameController.pressDirectionKey(direction);
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
    				gameController.pressDirectionKey(directionButton.getKey());
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
    				gameController.leaveCreateGotoPathMode();
    			} else {
    				gameController.enterIntoCreateGotoPathMode();
    			}
    			return true;
    		}
    		
    		if (event.getListenerActor() == gotoLocationButton) {
    			showGotoLocationDialog();
    			return true;
    		}
    		
    		if (event.getListenerActor() == acceptActionButton) {
    			gameController.acceptAction();
    			return true;
    		}
    		
    		if (event.getListenerActor() == cancelActionButton) {
    			gameController.cancelAction();
    			return true;
    		}
    		
    		if (event.getListenerActor() == endTurnButton) {
    			endOfTurnActor.start(gameController);
    			HudStage.this.addActor(endOfTurnActor);
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
    		
    		return false;
    	}
    };	

	private void createActionButtons(int bw) {
        createDirectionButtons(bw);
		
		centerOnUnitButton = new ButtonActor(shapeRenderer, "C");
		centerOnUnitButton.setSize(bw, bw);
		centerOnUnitButton.setPosition(bw, bw);
		centerOnUnitButton.addListener(buttonsInputListener);

		unitWaitButton = new ButtonActor(shapeRenderer, "wait");		
		unitWaitButton.setSize(bw, bw);
		unitWaitButton.setPosition(3*bw, 0);
		unitWaitButton.addListener(buttonsInputListener);
		
		sentryButton = new ButtonActor(shapeRenderer, "S");
		sentryButton.setSize(bw, bw);
		sentryButton.setPosition(4*bw, 0);
		sentryButton.addListener(buttonsInputListener);
		
		fortifyButton = new ButtonActor(shapeRenderer, "F");
		fortifyButton.setSize(bw, bw);
		fortifyButton.setPosition(5*bw, 0);
		fortifyButton.addListener(buttonsInputListener);
		
		buildColonyButton = new ButtonActor(shapeRenderer, "B");
		buildColonyButton.setSize(bw, bw);
		buildColonyButton.setPosition(0, bw*4);
		buildColonyButton.addListener(buttonsInputListener);
		plowButton = new ButtonActor(shapeRenderer, "P");
		plowButton.setSize(bw, bw);
		plowButton.setPosition(0, bw*5);
		plowButton.addListener(buttonsInputListener);
		roadButton = new ButtonActor(shapeRenderer, "R");
		roadButton.setSize(bw, bw);
		roadButton.setPosition(0, bw*6);
		roadButton.addListener(buttonsInputListener);
		
		gotoTileButton = new RadioButtonActor(shapeRenderer, "goto");
		gotoTileButton.setSize(bw, bw);
		gotoTileButton.setPosition(6*bw, 0);
		gotoTileButton.addListener(buttonsInputListener);
		gotoLocationButton = new ButtonActor(shapeRenderer, "goto l.");
		gotoLocationButton.setSize(bw, bw);
		gotoLocationButton.setPosition(7*bw, 0);
		gotoLocationButton.addListener(buttonsInputListener);
		
		nextUnitButton = new ButtonActor(shapeRenderer, "next");
        nextUnitButton.setSize(bw, bw);
        nextUnitButton.setPosition(getWidth() - bw, getHeight() - 3*bw);
        nextUnitButton.addListener(buttonsInputListener);
	
		viewButton = new RadioButtonActor(shapeRenderer, "V");
        viewButton.setSize(bw, bw);
        viewButton.setPosition(getWidth() / 2, getHeight() - bw - 10);
        viewButton.addListener(buttonsInputListener);

        endTurnButton = new ButtonActor(shapeRenderer, "end turn");
        endTurnButton.setSize(bw, bw);
        endTurnButton.setPosition(getWidth() - bw, getHeight() - bw);
        endTurnButton.addListener(buttonsInputListener);
        
        acceptActionButton = new ButtonActor(shapeRenderer, "accept");
        acceptActionButton.setSize(bw, bw);
        acceptActionButton.setPosition(0, getHeight() / 2);
        acceptActionButton.addListener(buttonsInputListener);

        cancelActionButton = new ButtonActor(shapeRenderer, "cancel");
        cancelActionButton.setSize(bw, bw);
        cancelActionButton.setPosition(getWidth() - bw, getHeight() / 2);
        cancelActionButton.addListener(buttonsInputListener);
	}

	private void createDirectionButtons(int bw) {
		for (int y = 0; y < BUTTON_DIRECTIONS.length; y++) {
            for (int x = 0; x < BUTTON_DIRECTIONS[y].length; x++) {
                Direction direction = BUTTON_DIRECTIONS[y][x];
                if (direction == null) {
                    continue;
                }
                ButtonActor button = createDirectionButton(direction, x * bw, y * bw, bw);
                directionButtons.put(direction, button);
            }
        }
	}
	
    private ButtonActor createDirectionButton(final Direction direction, int x, int y, int width) {
        ButtonActor button = new ButtonActor(shapeRenderer);
        button.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
    			gameController.pressDirectionKey(direction);
                return true;
            }
        });
        button.setX(x);
        button.setY(y);
        button.setWidth(width);
        button.setHeight(width);
        return button;
    }
	
    @Override
    public void act() {
    	shapeRenderer.setProjectionMatrix(getBatch().getProjectionMatrix());
    	shapeRenderer.setTransformMatrix(getBatch().getTransformMatrix());
    	shapeRenderer.translate(getViewport().getScreenX(), getViewport().getScreenY(), 0);
    	
        super.act();
    }

	private void resetButtonVisibility(GUIGameModel model) {
		// remove all actors
		buttonsGroup.clearChildren();
		if (model.isAiMove()) {
			return;
		}
		boolean hasUnitsToMove = model.hasUnitsToMove();
		if (!hasUnitsToMove) {
			buttonsGroup.addActor(endTurnButton);
		}
        
        viewButton.setChecked(model.isViewMode());
        gotoTileButton.setChecked(model.isCreateGotoPathMode());
		
        if (!model.isCreateGotoPathMode()) {
        	
			if (model.isActiveUnitSet()) {
				for (Entry<Direction, ButtonActor> entry : directionButtons.entrySet()) {
					ButtonActor directionButton = entry.getValue();
					buttonsGroup.addActor(directionButton);
				}
				
				Unit unit = model.getActiveUnit();
				buttonsGroup.addActor(centerOnUnitButton);
				buttonsGroup.addActor(unitWaitButton);
				buttonsGroup.addActor(sentryButton);
		        
		        if (unit.unitType.isNaval() || unit.unitType.isWagonTrain()) {
		        } else {
		        	buttonsGroup.addActor(fortifyButton);
		        	
		        	buttonsGroup.addActor(buildColonyButton);
		        	if (unit.hasAbility(Ability.IMPROVE_TERRAIN)) {
		        		buttonsGroup.addActor(plowButton);
		        		buttonsGroup.addActor(roadButton);
		        	}
		        }
			}
			
			if (!model.isViewMode() && hasUnitsToMove) {
				buttonsGroup.addActor(nextUnitButton);
			}
			buttonsGroup.addActor(viewButton);
        }
		
        if (model.isCreateGotoPathMode()) {
        	buttonsGroup.addActor(acceptActionButton);
        	buttonsGroup.addActor(cancelActionButton);
        }
        
		if (model.isActiveUnitSet()) {
			buttonsGroup.addActor(gotoLocationButton);
			buttonsGroup.addActor(gotoTileButton);
		}
	}
	
    private void showGotoLocationDialog() {
		HudStage.this.removeListener(keysInputListener);
		GoToCityDialogList gotoCityDialogList = new GoToCityDialogList(
				gameController,
				shapeRenderer,
				HudStage.this.getHeight() * 0.75f
		);
		gotoCityDialogList.addOnCloseListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				HudStage.this.addListener(keysInputListener);
				return true;
			}
		});
		gotoCityDialogList.show(HudStage.this);
    }
	
}
