package promitech.colonization.actors.colony;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Tile;
import promitech.colonization.ApplicationScreen;
import promitech.colonization.ApplicationScreenType;
import promitech.colonization.actors.map.MapViewApplicationScreen;
import promitech.colonization.gdx.Frame;
import promitech.colonization.ui.hud.ButtonActor;

public class ColonyApplicationScreen extends ApplicationScreen {

    private DragAndDrop dragAndDrop;
	private Stage stage;
	private BuildingsPanelActor buildingsPanelActor;
	private WarehousePanel resourcesPanel;
	private TerrainPanel terrainPanel;
	
	@Override
	public void create() {
		//stage = new Stage(new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        stage = new Stage();
        dragAndDrop = new DragAndDrop();
        dragAndDrop.setDragActorPosition(0, 0);
        
		int bw = (int) (stage.getHeight() * 0.33) / 3;
		
		ButtonActor closeButton = new ButtonActor(this.shape);
		closeButton.setWidth(bw);
		closeButton.setHeight(bw);
		closeButton.setX(stage.getWidth() - bw - 10);
		closeButton.setY(stage.getHeight() - bw - 10);
		closeButton.addListener(new InputListener() {
        	@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        		screenManager.setScreen(ApplicationScreenType.MAP_VIEW);
        		return true;
        	}
        });
        stage.addActor(closeButton);
        
        buildingsPanelActor = new BuildingsPanelActor();
        resourcesPanel = new WarehousePanel();
        terrainPanel = new TerrainPanel();
        
        Frame paperBackground = gameResources.getFrame("Paper");
        
        Table tableLayout = new Table(null);
        tableLayout.setBackground(new TextureRegionDrawable(paperBackground.texture));
        tableLayout.setFillParent(true);
        tableLayout.row();
        tableLayout.add(buildingsPanelActor);
        tableLayout.row();
        tableLayout.add(resourcesPanel);
        tableLayout.row();
        tableLayout.add(terrainPanel);
        stage.addActor(tableLayout);
	}

    public void initColony(Colony colony, Tile colonyTile) {
        MapViewApplicationScreen mapScreen = screenManager.getApplicationScreen(ApplicationScreenType.MAP_VIEW);
    	
        buildingsPanelActor.initBuildings(colony, dragAndDrop);
        resourcesPanel.initGoods(gameController.getSpecification(), colony);
        terrainPanel.initTerrains(mapScreen.getMapActor().mapDrawModel(), colonyTile);
    }
	
	@Override
	public void onShow() {
		Gdx.input.setInputProcessor(stage);
	}

	@Override
	public void onLeave() {
		Gdx.input.setInputProcessor(null);
		
		dragAndDrop.clear();
	}
	
	@Override
	public void render() {
        stage.act();
        stage.draw();
	}
	
	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height, true);		
	}
}
