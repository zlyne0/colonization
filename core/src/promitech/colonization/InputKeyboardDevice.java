package promitech.colonization;

import promitech.colonization.math.Directions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class InputKeyboardDevice {
    private boolean debug;

    private Directions moveDirection;
    
    public InputKeyboardDevice() {
    }
    
    public void recognizeInput() {
    	debug = Gdx.input.isKeyPressed(Input.Keys.G);
    	
    	moveDirection = null;
    	if (Gdx.input.isKeyPressed(Input.Keys.A)) {
        	moveDirection = Directions.WEST;
    	}
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
        	moveDirection = Directions.EAST;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
        	moveDirection = Directions.SOUTH;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
        	moveDirection = Directions.NORTH;
        }
    }

	public Directions getMoveDirection() {
		return moveDirection;
	}

	public boolean isDebug() {
		return debug;
	}
}
