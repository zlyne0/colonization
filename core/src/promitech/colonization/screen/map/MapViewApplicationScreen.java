package promitech.colonization.screen.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import promitech.colonization.infrastructure.ManyStageInputProcessor;
import promitech.colonization.screen.ApplicationScreen;
import promitech.colonization.screen.map.hud.GUIGameController;
import promitech.colonization.screen.map.hud.HudStage;

public class MapViewApplicationScreen extends ApplicationScreen {

    private Stage mapStage;
    private HudStage hudStage;
    private MapActor mapActor;
    private ManyStageInputProcessor manyStageInputProcessor;
    
    private GUIGameController guiGameController;
    
    @Override
    public void create() {
    	guiGameController = di.guiGameController;
    	
        mapActor = new MapActor(guiGameController, gameResources, di.guiGameModel);
        di.guiGameController.setMapActor(mapActor);
        di.guiGameController.setApplicationScreenManager(this.screenManager);
        di.moveController.setMapActor(mapActor);
        di.moveView.setMapActor(mapActor);
        
        hudStage = new HudStage(new ScreenViewport(), di, mapActor, this.screenManager);
        
        guiGameController.setMapHudStage(hudStage);
        
		mapStage = new Stage(new ScreenViewport(new OrthographicCamera()));
        mapStage.addListener(new InputListener() {
        	public boolean scrolled(InputEvent event, float x, float y, int amount) {
        		OrthographicCamera stageCamera = (OrthographicCamera)mapStage.getCamera();
        		if (amount > 0) {
        			stageCamera.zoom *= 1.25;
        		} else {
        			stageCamera.zoom *= 0.75;
        		}
        		if (stageCamera.zoom < 1f) {
        			stageCamera.zoom = 1f;
        			return true;
        		}
        		if (stageCamera.zoom >= 6f) {
        			stageCamera.zoom = 6f;
        			return true;
        		}
        		mapActor.centerCameraOnScreenCords(x, y);
        				
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
        guiGameController.onShowGUI();
        hudStage.hudInfoPanel.updateSelectedUnitDescription();
    }
    
    @Override
    public void onLeave() {
    	Gdx.input.setInputProcessor(null);
    	super.onLeave();
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
    	guiGameController.setMapHudStage(null);
    }

	public MapActor getMapActor() {
		return mapActor;
	}    
}
