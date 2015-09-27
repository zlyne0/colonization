package promitech.colonization.actors.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;

import promitech.colonization.ApplicationScreen;
import promitech.colonization.infrastructure.ManyStageInputProcessor;
import promitech.colonization.ui.hud.HudStage;

public class MapViewApplicationScreen extends ApplicationScreen {

    private Stage stage;
    private HudStage hudStage;
    private MapActor mapActor;
    
    @Override
    public void create() {
        mapActor = new MapActor(gameController, gameResources);
        gameController.setMapActor(mapActor);
        
        gameController.setApplicationScreenManager(this.screenManager);
        
        hudStage = new HudStage(new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()), gameController, gameResources);
        hudStage.hudInfoPanel.setMapActor(mapActor);
        
        
        //stage = new Stage(new CenterSizableViewport(640, 480, 640, 480));
        stage = new Stage();
        
        Table w = new Table();
        w.setFillParent(true);
        w.defaults().fillX();
        w.defaults().fillY();
        w.defaults().expandX();
        w.defaults().expandY();
        w.add(mapActor);
        w.pack();
        
        stage.addActor(w);      
    }
    
    @Override
    public void onShow() {
        Gdx.input.setInputProcessor(new ManyStageInputProcessor(hudStage, stage));
    }
    
    @Override
    public void onLeave() {
    	Gdx.input.setInputProcessor(null);
    }
    
    @Override
    public void render() {
        stage.getViewport().apply();
        stage.act();
        stage.draw();

        hudStage.getViewport().apply();
        hudStage.act();
        hudStage.draw();
    }
    
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        hudStage.getViewport().update(width, height, true);
    }

	public MapActor getMapActor() {
		return mapActor;
	}    
}
