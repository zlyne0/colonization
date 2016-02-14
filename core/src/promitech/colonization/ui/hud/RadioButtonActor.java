package promitech.colonization.ui.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class RadioButtonActor extends Actor {
    private static final Color BUTTON_COLOR = new Color(0.75f, 0.75f, 0.75f, 0.50f);  
    private static final Color CHECKED_BUTTON_COLOR = new Color(0.50f, 0.50f, 0.50f, 0.50f);  
    private final ShapeRenderer shapeRenderer;

    private boolean checked = false;
    private boolean enabled = true;
    
	public RadioButtonActor(ShapeRenderer shapeRenderer) {
        this.shapeRenderer = shapeRenderer;
    }
    
    @Override
    public void draw(Batch batch, float parentAlpha) {
    	if (!enabled) {
    		return;
    	}
        batch.end();
        
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        
        shapeRenderer.begin(ShapeType.Filled);
        
        if (checked) {
        	shapeRenderer.setColor(CHECKED_BUTTON_COLOR);
        } else {
        	shapeRenderer.setColor(BUTTON_COLOR);
        }
        shapeRenderer.rect(getX() + 10, getY() + 10, getWidth()-20, getHeight()-20);
        shapeRenderer.end();
        
        batch.begin();
    }

    public void setChecked(boolean checked) {
    	this.checked = checked;
    }
    
	public boolean isChecked() {
		return checked;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
