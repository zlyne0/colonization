package promitech.colonization.ui.hud;

import promitech.colonization.Direction;
import promitech.colonization.GameController;
import promitech.colonization.GameResources;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.Viewport;

public class HudStage extends Stage {
    private static Direction[][] BUTTON_DIRECTIONS = new Direction[][] { 
        {Direction.SW, Direction.S, Direction.SE}, 
        {Direction.W, null, Direction.E},
        {Direction.NW, Direction.N, Direction.NE},
    };

    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    
    public final HudInfoPanel hudInfoPanel; 
    
    private final GameController gameController;

    public HudStage(Viewport viewport, final GameController gameController, GameResources gameResources) {
        super(viewport);
        this.gameController = gameController;

        int bw = (int) (getHeight() * 0.33) / 3;
        for (int y = 0; y < BUTTON_DIRECTIONS.length; y++) {
            for (int x = 0; x < BUTTON_DIRECTIONS[y].length; x++) {
                Direction direction = BUTTON_DIRECTIONS[y][x];
                if (direction == null) {
                    continue;
                }
                ButtonActor button = createButton(direction, x * bw, y * bw, bw);
                addActor(button);
            }
        }

        hudInfoPanel = new HudInfoPanel(gameController, gameResources);
        addActor(hudInfoPanel);
        
        createNextUnitButton(bw);
        
        final RadioButtonActor viewButton = new RadioButtonActor(shapeRenderer);
        viewButton.setWidth(bw);
        viewButton.setHeight(bw);
        viewButton.setX(getWidth() / 2);
        viewButton.setY(getHeight() - bw - 10);
        viewButton.addListener(new InputListener() {
        	@Override
        	public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        		viewButton.setChecked(!viewButton.isChecked());
        		gameController.setViewMode(viewButton.isChecked());
        		return true;
        	}
        });
        addActor(viewButton);
        
        addListener(new InputListener() {
        	@Override
        	public boolean keyDown(InputEvent event, int keycode) {
        		if (keycode == Input.Keys.N) {
        			gameController.nextActiveUnit();
        			return true;
        		}
        		return super.keyDown(event, keycode);
        	}
        });
    }

	private void createNextUnitButton(int bw) {
		ButtonActor nextUnit = new ButtonActor(shapeRenderer);
        nextUnit.setWidth(bw);
        nextUnit.setHeight(bw);
        nextUnit.setX(getWidth() - bw - 10);
        nextUnit.setY(getHeight() - bw - 10);
        nextUnit.addListener(new InputListener() {
        	@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        		gameController.nextActiveUnit();
        		return true;
        	}
        	
        });
        addActor(nextUnit);
	}

    @Override
    public void act() {
        super.act();
        shapeRenderer.setProjectionMatrix(getBatch().getProjectionMatrix());
        shapeRenderer.setTransformMatrix(getBatch().getTransformMatrix());
        shapeRenderer.translate(getViewport().getScreenX(), getViewport().getScreenY(), 0);
    }

    private ButtonActor createButton(final Direction direction, int x, int y, int width) {
        ButtonActor button = new ButtonActor(shapeRenderer);
        button.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                System.out.println("move active unit in " + direction + " direction");
                return true;
            }
        });
        button.setX(x);
        button.setY(y);
        button.setWidth(width);
        button.setHeight(width);
        return button;
    }
}
