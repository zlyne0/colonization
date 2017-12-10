package promitech.colonization.screen.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class ShiftPressed {

    private static boolean shiftPressed = false;  
    public static boolean isShiftPressed() {
    	return shiftPressed || Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
    }
	
    public static void setShiftPressed(boolean pressed) {
    	shiftPressed = pressed;
    }
	
}
