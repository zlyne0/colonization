package promitech.colonization.ui.hud;

import promitech.colonization.Direction;
import promitech.colonization.GUIGameController;
import promitech.colonization.GameResources;

import java.util.EnumMap;
import java.util.Map.Entry;
import java.util.Set;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.viewport.Viewport;

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
    
    private final EnumMap<Direction, ButtonActor> directionButtons = new EnumMap<Direction, ButtonActor>(Direction.class);
    private ButtonActor nextUnit;
    private ButtonActor gotoButton;
    private RadioButtonActor viewButton;

    public HudStage(Viewport viewport, final GUIGameController gameController, GameResources gameResources) {
        super(viewport);
        this.gameController = gameController;

        int bw = (int) (getHeight() * 0.33) / 3;
        createDirectionButtons(bw);

        hudInfoPanel = new HudInfoPanel(gameResources);
        addActor(hudInfoPanel);
        
        createNextUnitButton(bw);
        createGotoUnitbutton(bw);
        createViewModeButton(bw);
        
        addListener(new InputListener() {
        	@Override
        	public boolean keyDown(InputEvent event, int keycode) {
        		if (keycode == Input.Keys.V) {
        			viewButton.setChecked(!viewButton.isChecked());
        			gameController.setViewMode(viewButton.isChecked());
        			resetButtonVisibility();
        			return true;
        		}
        		if (keycode == Input.Keys.N) {
        			gameController.nextActiveUnit();
        			resetButtonVisibility();
        			return true;
        		}
        		if (keycode == Input.Keys.G) {
        			gameController.gotoButtonPressed();
        			resetButtonVisibility();
        			return true;
        		}
        		Direction direction = directionByKeyCode.get(keycode);
        		if (direction != null) {
        			gameController.pressDirectionKey(direction);
        			return true;
        		}
        		return super.keyDown(event, keycode);
        	}
        });
    }

	private void createViewModeButton(int bw) {
		viewButton = new RadioButtonActor(shapeRenderer);
        viewButton.setWidth(bw);
        viewButton.setHeight(bw);
        viewButton.setX(getWidth() / 2);
        viewButton.setY(getHeight() - bw - 10);
        viewButton.addListener(new InputListener() {
        	@Override
        	public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        		viewButton.setChecked(!viewButton.isChecked());
        		gameController.setViewMode(viewButton.isChecked());
        		resetButtonVisibility();
        		return true;
        	}
        });
        addActor(viewButton);
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
                addActor(button);
            }
        }
	}

	private void createNextUnitButton(int bw) {
		nextUnit = new ButtonActor(shapeRenderer);
        nextUnit.setWidth(bw);
        nextUnit.setHeight(bw);
        nextUnit.setX(getWidth() - bw - 10);
        nextUnit.setY(getHeight() - bw - 10);
        nextUnit.addListener(new InputListener() {
        	@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        		gameController.nextActiveUnit();
        		resetButtonVisibility();
        		return true;
        	}
        	
        });
        addActor(nextUnit);
	}

	private void createGotoUnitbutton(int bw) {
		gotoButton = new ButtonActor(shapeRenderer);
		gotoButton.setWidth(bw);
		gotoButton.setHeight(bw);
		gotoButton.setX(getWidth() - bw - bw - 10);
		gotoButton.setY(getHeight() - bw - 10);
		gotoButton.addListener(new InputListener() {
        	@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        		gameController.gotoButtonPressed();
        		resetButtonVisibility();
        		return true;
        	}
        	
        });
		addActor(gotoButton);
	}
	
    @Override
    public void act() {
        super.act();
        shapeRenderer.setProjectionMatrix(getBatch().getProjectionMatrix());
        shapeRenderer.setTransformMatrix(getBatch().getTransformMatrix());
        shapeRenderer.translate(getViewport().getScreenX(), getViewport().getScreenY(), 0);
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

	public void resetButtonVisibility() {
		boolean showDirectionButtons = gameController.isShowDirectionButtons();
		for (Entry<Direction, ButtonActor> entrySet : directionButtons.entrySet()) {
			entrySet.getValue().setEnabled(showDirectionButtons);
		}
		
		gotoButton.setEnabled(gameController.isShowGotoButton());
		viewButton.setEnabled(gameController.isShowViewModeButton());
	}
}
