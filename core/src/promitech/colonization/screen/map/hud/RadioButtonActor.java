package promitech.colonization.screen.map.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class RadioButtonActor extends ButtonActor {
    private static final Color UNCHECKED_BUTTON_COLOR = new Color(0.75f, 0.75f, 0.75f, 0.50f);  
    private static final Color CHECKED_BUTTON_COLOR = new Color(0.50f, 0.50f, 0.50f, 0.50f);  

    private boolean checked = false;
    
	public RadioButtonActor(ShapeRenderer shapeRenderer, String label) {
		super(shapeRenderer, label);
    }
    
    @Override
    public void draw(Batch batch, float parentAlpha) {
    	if (checked) {
    		buttonColor = CHECKED_BUTTON_COLOR;
    	} else {
    		buttonColor = UNCHECKED_BUTTON_COLOR;
    	}
    	super.draw(batch, parentAlpha);
    }

    public void setChecked(boolean checked) {
    	this.checked = checked;
    }
    
	public boolean isChecked() {
		return checked;
	}

	public void recheck() {
		this.checked = !checked;
	}
}
