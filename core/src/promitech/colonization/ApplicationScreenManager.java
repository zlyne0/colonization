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

import net.sf.freecol.common.model.Building;
import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import promitech.colonization.actors.colony.ColonyApplicationScreen;
import promitech.colonization.actors.europe.EuropeApplicationScreen;
import promitech.colonization.actors.map.MapViewApplicationScreen;
import promitech.colonization.infrastructure.FontResource;
import promitech.colonization.infrastructure.ThreadsResources;
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
		GameResources.instance = gameResources;
		try {
		    long s = System.currentTimeMillis();
            Messages.instance().load();
            FontResource.load();
            gameResources.load();
            
            gameController.initGameFromSavegame();
            long length = System.currentTimeMillis() - s;
            System.out.println("loading timeout: " + length);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		applicationScreensByType.put(ApplicationScreenType.MAP_VIEW, new MapViewApplicationScreen());
		applicationScreensByType.put(ApplicationScreenType.COLONY, new ColonyApplicationScreen());
		applicationScreensByType.put(ApplicationScreenType.EUROPE, new EuropeApplicationScreen());
		for (ApplicationScreen screen : applicationScreensByType.values()) {
			screen.batch = batch;
			screen.shape = shape;
			screen.gameResources = gameResources;
			screen.screenManager = this;
			screen.gameController = gameController;
			screen.create();
		}
		
//		{
//		    // TODO: for tests only
//            Tile tile = gameController.getGame().map.getTile(24, 78);
//            Settlement settlement = tile.getSettlement();
//            if (settlement.isColony()) {
//                Colony colony = settlement.getColony();
//
//                // start additional building
//                {
//                	Building building = new Building("building:-2");
//                	building.buildingType = Specification.instance.buildingTypes.getById("model.building.shipyard");
//                	colony.buildings.add(building);
//                }
////                {
////                	Building building = new Building("building:-" + colony.buildings.size());
////                	building.buildingType = Specification.instance.buildingTypes.getById("model.building.armory");
////                	colony.buildings.add(building);
////                }
//                {
//                	Building building = new Building("building:-1");
//                	building.buildingType = Specification.instance.buildingTypes.getById("model.building.tobacconistShop");
//                	colony.buildings.add(building);
//                	colony.buildings.removeId("building:6542");
//                }
//                
//                colony.updateColonyFeatures();
//                // end additional building
//                
//                ColonyApplicationScreen colonyScreen = getApplicationScreen(ApplicationScreenType.COLONY);
//                colonyScreen.initColony(colony, tile);
//            }
//		}
		
		setScreen(ApplicationScreenType.MAP_VIEW);
		Gdx.graphics.setContinuousRendering(false);
	}
	
	@Override
	public void render() {
	    frameRenderMillis = System.currentTimeMillis();
	    
//	    Gdx.gl.glEnable(GL20.GL_BLEND);
//	    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
//		Gdx.gl.glClearColor(0, 0, 0, 1);
//		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
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
	    ThreadsResources.instance.dispose();
	    
		batch.dispose();
		shape.dispose();
	}
	
	@Override
	public void resize(int width, int height) {
        currentApplicationScreen.resize(width, height);
	}

	@SuppressWarnings("unchecked")
    public <T extends ApplicationScreen> T getApplicationScreen(ApplicationScreenType type) {
        ApplicationScreen applicationScreen = applicationScreensByType.get(type);
        if (applicationScreen == null) {
            throw new IllegalStateException("can not find application screen for type: " + type);
        }
        return (T)applicationScreen;
    }
}
