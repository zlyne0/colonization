package promitech.colonization.screen;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.utils.Array;

import promitech.colonization.DI;
import promitech.colonization.GameResources;

public abstract class ApplicationScreen {

	private final Array<EventListener> oneHitOnLeaveListeners = new Array<EventListener>();
	protected ApplicationScreenManager screenManager;
	protected SpriteBatch batch;
	protected ShapeRenderer shape;	
	protected GameResources gameResources;
	protected DI di;
	
	public void create() {
	}
	
	public void render() {
	}
	
	public void dispose() {
	}
	
	public void onShow() {
	}
	
	public void onLeave() {
		for (int i=0; i<oneHitOnLeaveListeners.size; i++) {
			oneHitOnLeaveListeners.get(i).handle(null);
		}
		oneHitOnLeaveListeners.clear();
	}

    public void resize(int width, int height) {
    }
	
    public void addOneHitOnLeaveListener(EventListener listener) {
    	oneHitOnLeaveListeners.add(listener);
    }
}
