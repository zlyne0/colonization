package promitech.colonization.ui.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Actor;

class ButtonActor extends Actor {
    private static final Color BUTTON_COLOR = new Color(0.75f, 0.75f, 0.75f, 0.75f);  
    private final ShapeRenderer shapeRenderer;

    public ButtonActor(ShapeRenderer shapeRenderer) {
        this.shapeRenderer = shapeRenderer;
    }
    
    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.end();
        
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(BUTTON_COLOR);
        shapeRenderer.rect(getX() + 10, getY() + 10, getWidth()-20, getHeight()-20);
        shapeRenderer.end();
        
        batch.begin();
    }
}
