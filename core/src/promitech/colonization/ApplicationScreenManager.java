package promitech.colonization;

import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.BufferUtils;

import promitech.colonization.actors.map.MapViewApplicationScreen;
import promitech.colonization.actors.settlement.SettlementApplicationScreen;
import promitech.colonization.infrastructure.FontResource;
import promitech.colonization.ui.resources.Messages;

public class ApplicationScreenManager extends ApplicationAdapter {
	private Map<ApplicationScreenType, ApplicationScreen> applicationScreensByType = new HashMap<ApplicationScreenType, ApplicationScreen>();

	private ApplicationScreen currentApplicationScreen;
	
    private OrthographicCamera cameraYdown;
    private OrthographicCamera cameraYup;
	private SpriteBatch batch;
	private ShapeRenderer shape;
	private GameResources gameResources;
	
	public void setScreen(ApplicationScreenType type) {
		if (currentApplicationScreen != null) {
			currentApplicationScreen.onLeave();
		}
		currentApplicationScreen = applicationScreensByType.get(type);
		currentApplicationScreen.onShow();
	}
	
	@Override
	public void create() {
		Gdx.app.setLogLevel(Application.LOG_DEBUG);
		
		cameraYdown = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cameraYdown.setToOrtho(true, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cameraYdown.update();

        cameraYup = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cameraYup.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cameraYup.update();
		
		batch = new SpriteBatch();
		shape = new ShapeRenderer();
		
		GUIGameController gameController = new GUIGameController();
		gameResources = new GameResources();
		try {
            Messages.instance().load();
            FontResource.load();
            gameResources.load();
            
            gameController.initGameFromSavegame();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		applicationScreensByType.put(ApplicationScreenType.MAP_VIEW, new MapViewApplicationScreen());
		applicationScreensByType.put(ApplicationScreenType.SETTLEMENT, new SettlementApplicationScreen());
		for (ApplicationScreen screen : applicationScreensByType.values()) {
			screen.batch = batch;
			screen.shape = shape;
			screen.gameResources = gameResources;
			screen.screenManager = this;
			screen.gameController = gameController;
			screen.create();
		}
		setScreen(ApplicationScreenType.MAP_VIEW);
	}
	
	@Override
	public void render() {
	    frameRenderMillis = System.currentTimeMillis();
	    
	    Gdx.gl.glEnable(GL20.GL_BLEND);
	    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
//        cameraYdown.update();
//        batch.setProjectionMatrix(cameraYdown.combined);
//        shape.setProjectionMatrix(cameraYdown.combined);
		
		currentApplicationScreen.render();
		
		frameRenderMillis = System.currentTimeMillis() - frameRenderMillis;
		drawFPS();
	}
	
    private BitmapFont fpsFont;    
    private long frameRenderMillis;
	
	private void drawFPS() {
        if (fpsFont == null) {
            fpsFont = new BitmapFont(true);
        }
        IntBuffer intBuffer = BufferUtils.newIntBuffer(16);
        Gdx.gl20.glGetIntegerv(GL20.GL_MAX_TEXTURE_SIZE, intBuffer);
	    
        batch.setProjectionMatrix(cameraYdown.combined);
        batch.begin();
        fpsFont.setColor(Color.GREEN);
        fpsFont.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond() + ", time: " + frameRenderMillis, 20, 20);
        fpsFont.draw(batch, "TextureSize: " + intBuffer.get(), 20, 40);
        fpsFont.draw(batch, "Density: " + Gdx.graphics.getDensity(), 20, 60);
        batch.end();
	}
	
	@Override
	public void dispose() {
	    Messages.instance().dispose();
	    FontResource.dispose();
	    
		batch.dispose();
		shape.dispose();
	}
	
	@Override
	public void resize(int width, int height) {
        currentApplicationScreen.resize(width, height);
	}
}
