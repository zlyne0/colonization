package promitech.colonization.screen;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import promitech.colonization.DI;
import promitech.colonization.GameResources;

public abstract class ApplicationScreen {

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
	}

    public void resize(int width, int height) {
    }
	
}
