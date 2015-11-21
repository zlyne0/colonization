package promitech.colonization.actors.colony;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Tile;
import promitech.colonization.ApplicationScreen;
import promitech.colonization.ApplicationScreenType;
import promitech.colonization.GameResources;
import promitech.colonization.actors.map.MapViewApplicationScreen;
import promitech.colonization.gdx.Frame;
import promitech.colonization.ui.hud.ButtonActor;

public class ColonyApplicationScreen extends ApplicationScreen {

    private DragAndDrop unitsDragAndDrop;
    private DragAndDrop goodsDragAndDrop;
	private Stage stage;
	private BuildingsPanelActor buildingsPanelActor;
	private WarehousePanel warehousePanel;
	private TerrainPanel terrainPanel;
	private OutsideUnitsPanel outsideUnitsPanel;
	private CarrierUnitsPanel carrierUnitsPanel;
	private PopulationPanel populationPanel;
	private ProductionPanel productionPanel;
	
	private final ChangeColonyStateListener changeColonyStateListener = new ChangeColonyStateListener() {
        @Override
        public void changeUnitAllocation(Colony colony) {
            colony.updateColonyPopulation();
            colony.updateModelOnWorkerAllocationOrGoodsTransfer();
            populationPanel.update(colony);
            productionPanel.init(colony, colonyTile);
            buildingsPanelActor.updateProductionDesc();
            terrainPanel.updateProduction();
            warehousePanel.updateGoodsQuantity(colony);
        }

        @Override
        public void transfereGoods() {
            colony.updateModelOnWorkerAllocationOrGoodsTransfer();
            productionPanel.init(colony, colonyTile);
            buildingsPanelActor.updateProductionDesc();
        }
    };
	
    private static boolean shiftPressed = false;  
    static boolean isShiftPressed() {
    	return shiftPressed || Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
    }
    
    private Colony colony;
    private Tile colonyTile;
    
	@Override
	public void create() {
		//stage = new Stage(new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        stage = new Stage();
        unitsDragAndDrop = new DragAndDrop();
        unitsDragAndDrop.setDragActorPosition(0, 0);
        unitsDragAndDrop.setTapSquareSize(3);
        
        goodsDragAndDrop = new DragAndDrop();
        goodsDragAndDrop.setDragActorPosition(0, 0);
        goodsDragAndDrop.setTapSquareSize(3);
        
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
        
        buildingsPanelActor = new BuildingsPanelActor(changeColonyStateListener);
        warehousePanel = new WarehousePanel(changeColonyStateListener);
        terrainPanel = new TerrainPanel(changeColonyStateListener);
		outsideUnitsPanel = new OutsideUnitsPanel(this.shape, changeColonyStateListener);
        carrierUnitsPanel = new CarrierUnitsPanel(this.shape, goodsDragAndDrop, changeColonyStateListener);
        populationPanel = new PopulationPanel();
        productionPanel = new ProductionPanel();
        
        Frame paperBackground = gameResources.getFrame("Paper");
        
        Table tableLayout = new Table(null);
        tableLayout.setBackground(new TiledDrawable(paperBackground.texture));
        
        HorizontalGroup rowGroup1 = new HorizontalGroup();
        rowGroup1.addActor(carrierUnitsPanel);
        rowGroup1.addActor(outsideUnitsPanel);
        
        tableLayout.setFillParent(true);
        tableLayout.add(productionPanel).colspan(2).fillX();
        tableLayout.row();
        tableLayout.add(terrainPanel);
        tableLayout.add(buildingsPanelActor);
        tableLayout.row();
        tableLayout.add(populationPanel);
        tableLayout.row();
        tableLayout.add(rowGroup1).colspan(2);
        tableLayout.row();
        tableLayout.add(warehousePanel).colspan(2);
        tableLayout.row();
		tableLayout.add(createShiftButton()).colspan(2).fillX();
		
        stage.addActor(tableLayout);
        stage.setDebugAll(true);
	}

	private TextButton createShiftButton() {
		TextButton textButton = new TextButton("shift", GameResources.instance.getUiSkin());
//		textButton.setRotation(90);
//		textButton.setTransform(true);
		textButton.pad(10);
		
		textButton.addListener(new DragListener() {
			{
				setTapSquareSize(3);
			}
			@Override
			public void dragStart(InputEvent event, float x, float y, int pointer) {
				shiftPressed = true;
			}
			@Override
			public void dragStop(InputEvent event, float x, float y, int pointer) {
				shiftPressed = false;
			}
		});
		return textButton;
	}
	
    public void initColony(Colony colony, Tile colonyTile) {
    	this.colony = colony;
    	this.colonyTile = colonyTile;
    	unitsDragAndDrop.clear();
    	goodsDragAndDrop.clear();
        MapViewApplicationScreen mapScreen = screenManager.getApplicationScreen(ApplicationScreenType.MAP_VIEW);
    	
        productionPanel.init(colony, colonyTile);
        buildingsPanelActor.initBuildings(colony, unitsDragAndDrop);
        warehousePanel.initGoods(colony, goodsDragAndDrop);
        terrainPanel.initTerrains(mapScreen.getMapActor().mapDrawModel(), colonyTile, unitsDragAndDrop);
        outsideUnitsPanel.initUnits(colonyTile, unitsDragAndDrop);
        carrierUnitsPanel.initUnits(colonyTile);
        populationPanel.update(colony);
    }
	
	@Override
	public void onShow() {
		Gdx.input.setInputProcessor(stage);
	}

	@Override
	public void onLeave() {
		Gdx.input.setInputProcessor(null);
		
		unitsDragAndDrop.clear();
		goodsDragAndDrop.clear();
		this.colony = null;
		this.colonyTile = null;
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
