package promitech.colonization.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;

public class ClosableDialogAdapter {
	
	private static final long CREATE_CLOSE_TIMEOUT = 1000;
	
    private final long createTime;
	private EventListener closeListener;
    
    public ClosableDialogAdapter() {
    	this.createTime = System.currentTimeMillis();
    }
    
	public boolean containsCords(Actor actor, float x, float y) {
		return 
				actor.getX() <= x && 
				actor.getY() <= y && 
				x <= actor.getX() + actor.getWidth() && 
				y <= actor.getY() + actor.getHeight();
	}
	
	public boolean canCloseBecauseClickOutsideDialog(Actor actor, float x, float y) {
		if (!containsCords(actor, x, y)) {
			if (System.currentTimeMillis() - createTime > CREATE_CLOSE_TIMEOUT) {
				return true;
			}
		}
		return false;
	}

	public void addCloseListener(EventListener closeListener) {
		this.closeListener = closeListener;
	}

	public void executeCloseListener() {
		if (closeListener != null) {
			closeListener.handle(null);
		}
	}	
	
}
