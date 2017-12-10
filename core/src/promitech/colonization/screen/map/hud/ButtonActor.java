package promitech.colonization.screen.map.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;

import promitech.colonization.infrastructure.FontResource;

public class ButtonActor extends Widget {
    private static final Color DEFAULT_BUTTON_COLOR = new Color(0.75f, 0.75f, 0.75f, 0.50f);  
    
    private final ShapeRenderer shapeRenderer;
    private final String label;
    protected Color buttonColor = DEFAULT_BUTTON_COLOR;
    
    public ButtonActor(ShapeRenderer shapeRenderer) {
        this.shapeRenderer = shapeRenderer;
        this.label = null;
    }

    public ButtonActor(ShapeRenderer shapeRenderer, String label) {
        this.shapeRenderer = shapeRenderer;
        this.label = label;
    }
    
    @Override
    public float getPrefWidth() {
    	return getWidth();
    }
    
    @Override
    public float getPrefHeight() {
    	return getHeight();
    }
    
    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.end();
        
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        
    	shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
		shapeRenderer.setTransformMatrix(batch.getTransformMatrix());
        
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(buttonColor);
        shapeRenderer.rect(getX() + 10, getY() + 10, getWidth()-20, getHeight()-20);
        shapeRenderer.end();

        batch.begin();
        if (label != null) {
        	BitmapFont font = FontResource.getHudButtonsFont();
        	font.draw(batch, 
        		label, 
    			getX() + getWidth()/2 - FontResource.strWidth(font, label) / 2, 
    			getY() + getHeight()/2 + FontResource.fontHeight(font) / 2
        	);
        }
    }
    
}
