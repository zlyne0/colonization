package promitech.colonization;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public abstract class ApplicationScreen {

	protected ApplicationScreenManager screenManager;
	protected SpriteBatch batch;
	protected ShapeRenderer shape;	
	protected GameResources gameResources;
	protected GUIGameController gameController;
	
	public void create() {
	}
	
	public void render() {
	}
	
	public void dispose() {
	}
	
	public void onShow() {
	}
	
	public void onLeave() {
	}

    public void resize(int width, int height) {
    }
	
}
