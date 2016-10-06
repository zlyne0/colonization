package promitech.colonization.actors.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import promitech.colonization.ApplicationScreen;
import promitech.colonization.infrastructure.ManyStageInputProcessor;
import promitech.colonization.ui.hud.HudStage;

public class MapViewApplicationScreen extends ApplicationScreen {

    private Stage mapStage;
    private HudStage hudStage;
    private MapActor mapActor;
    private ManyStageInputProcessor manyStageInputProcessor;
    
    @Override
    public void create() {
        mapActor = new MapActor(gameController, gameResources);
        gameController.setMapActor(mapActor);
        
        gameController.setApplicationScreenManager(this.screenManager);
        
        hudStage = new HudStage(new ScreenViewport(), gameController, gameResources);
        hudStage.hudInfoPanel.setMapActor(mapActor);
        
        gameController.setMapHudStage(hudStage);
        
		mapStage = new Stage(new ScreenViewport(new OrthographicCamera()));
        mapStage.addListener(new InputListener() {
        	public boolean scrolled(InputEvent event, float x, float y, int amount) {
        		OrthographicCamera stageCamera = (OrthographicCamera)mapStage.getCamera();
        		if (amount > 0) {
        			stageCamera.zoom *= 1.25;
        		} else {
        			stageCamera.zoom *= 0.75;
        		}
        		stageCamera.zoom = MathUtils.clamp(stageCamera.zoom, 1f, 6f);
        		System.out.println("zoom = " + stageCamera.zoom);
        				
        		resizeMapStage();        		
        		return true;
        	};
        });
        
        Table wl = new Table();
        wl.setFillParent(true);
        wl.defaults().fillX();
        wl.defaults().fillY();
        wl.defaults().expandX();
        wl.defaults().expandY();
        wl.add(mapActor);
        wl.pack();
        mapStage.addActor(wl);
        
        manyStageInputProcessor = new ManyStageInputProcessor(hudStage, mapStage);
    }
    
    @Override
    public void onShow() {
        Gdx.input.setInputProcessor(manyStageInputProcessor);
        gameController.onShowGUI();
        hudStage.hudInfoPanel.updateSelectedUnitDescription();
    }
    
    @Override
    public void onLeave() {
    	Gdx.input.setInputProcessor(null);
    }
    
    @Override
    public void render() {
    	Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        mapStage.getViewport().apply();
        mapStage.act();
        mapStage.draw();
        
        hudStage.getViewport().apply();
        hudStage.act();
        hudStage.draw();
    }
    
    @Override
    public void resize(int width, int height) {
    	resizeMapStage();        		
    	
        hudStage.getViewport().update(width, height, true);
        hudStage.updateLayout();
    }

	private void resizeMapStage() {
		OrthographicCamera stageCamera = (OrthographicCamera)mapStage.getViewport().getCamera();

    	int w = (int)(Gdx.graphics.getWidth() * stageCamera.zoom);
		int h = (int)(Gdx.graphics.getHeight() * stageCamera.zoom);
		
		mapStage.getViewport().update(w, h, true);
		
		Vector2 v = mapStage.screenToStageCoordinates(new Vector2(0, 0));
		stageCamera.position.add(-v.x, stageCamera.viewportHeight - v.y, 0);
	}
    
    @Override
    public void dispose() {
    	gameController.setMapHudStage(null);
    }

	public MapActor getMapActor() {
		return mapActor;
	}    
}
