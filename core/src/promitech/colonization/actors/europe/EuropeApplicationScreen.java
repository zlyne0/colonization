package promitech.colonization.actors.europe;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;

import net.sf.freecol.common.model.player.Player;
import promitech.colonization.ApplicationScreen;
import promitech.colonization.ApplicationScreenType;
import promitech.colonization.actors.CarrierUnitsPanel;
import promitech.colonization.actors.ChangeColonyStateListener;
import promitech.colonization.actors.OutsideUnitsPanel;
import promitech.colonization.actors.UnitActor;
import promitech.colonization.gdx.Frame;
import promitech.colonization.ui.DoubleClickedListener;
import promitech.colonization.ui.hud.ButtonActor;

public class EuropeApplicationScreen extends ApplicationScreen {
	private DragAndDrop goodsDragAndDrop;
	private DragAndDrop unitsDragAndDrop;
	
	private Stage stage;
	private MarketPanel marketPanel; 
	private CarrierUnitsPanel carrierUnitsPanel;
	private OutsideUnitsPanel outsideUnitsPanel;
	
	private final ChangeColonyStateListener changeColonyStateListener = new ChangeColonyStateListener() {
		@Override
		public void changeUnitAllocation() {
		}

		@Override
		public void transfereGoods() {
		}

		@Override
		public void changeBuildingQueue() {
		}
	};
	
    private final DoubleClickedListener unitActorDoubleClickListener = new DoubleClickedListener() {
        public void doubleClicked(InputEvent event, float x, float y) {
            UnitActor unitActor = (UnitActor)event.getListenerActor();
            //showUnitOrders(unitActor);
            System.out.println("XXX unit double click");
        }
    };
	
	@Override
	public void create() {
        unitsDragAndDrop = new DragAndDrop();
        unitsDragAndDrop.setDragActorPosition(0, 0);
        unitsDragAndDrop.setTapSquareSize(3);
        
        goodsDragAndDrop = new DragAndDrop();
        goodsDragAndDrop.setDragActorPosition(0, 0);
        goodsDragAndDrop.setTapSquareSize(3);
		
		
		stage = new Stage();		
		int bw = (int) (stage.getHeight() * 0.33) / 3;
		
		ButtonActor closeButton = new ButtonActor(this.shape, "ESC");
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
		
        marketPanel = new MarketPanel();
        carrierUnitsPanel = new CarrierUnitsPanel(shape, goodsDragAndDrop, changeColonyStateListener, unitActorDoubleClickListener);
        outsideUnitsPanel = new OutsideUnitsPanel(shape, unitsDragAndDrop, changeColonyStateListener, unitActorDoubleClickListener);
        
        Frame paperBackground = gameResources.getFrame("Paper");
        Table tableLayout = new Table();
        tableLayout.setBackground(new TiledDrawable(paperBackground.texture));            
        tableLayout.setFillParent(true);
        
        HorizontalGroup rowGroup1 = new HorizontalGroup();
        rowGroup1.addActor(carrierUnitsPanel);
        rowGroup1.addActor(outsideUnitsPanel);
        
        
        tableLayout.add(rowGroup1).row();
        tableLayout.add(marketPanel);
        
        stage.addActor(tableLayout);
        stage.setDebugAll(true);
	}
	
	public void init(Player player) {
		marketPanel.init(player);
		carrierUnitsPanel.initUnits(player.getEurope());
		outsideUnitsPanel.initUnits(player.getEurope());
	}
	
	@Override
	public void onShow() {
		Gdx.input.setInputProcessor(stage);
	}
	
	@Override
	public void onLeave() {
		Gdx.input.setInputProcessor(null);
	}
	
	@Override
	public void render() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
        stage.act();
        stage.draw();
	}
	
	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height, true);		
	}
}
