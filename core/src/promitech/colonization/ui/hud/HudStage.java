package promitech.colonization.ui.hud;

import promitech.colonization.Direction;
import promitech.colonization.GameResources;

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

    private final ShapeRenderer shapeRenderer = new ShapeRenderer();;

    public HudStage(Viewport viewport, GameResources gameResources) {
        super(viewport);

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

        addActor(new HudInfoPanel(gameResources));
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
