package promitech.colonization.actors.colony;

import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.GoodMaxProductionLocation;
import net.sf.freecol.common.model.ObjectWithId;
import net.sf.freecol.common.model.ProductionSummary;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.Unit.UnitState;
import net.sf.freecol.common.model.UnitRole;
import net.sf.freecol.common.model.UnitRoleLogic;
import net.sf.freecol.common.model.player.Notification;
import net.sf.freecol.common.model.specification.Ability;
import promitech.colonization.ApplicationScreen;
import promitech.colonization.ApplicationScreenType;
import promitech.colonization.GUIGameController;
import promitech.colonization.GUIGameModel;
import promitech.colonization.GameResources;
import promitech.colonization.actors.ChangeColonyStateListener;
import promitech.colonization.actors.GoodTransferActorBridge;
import promitech.colonization.actors.UnitActor;
import promitech.colonization.actors.UnitsPanel;
import promitech.colonization.actors.map.MapViewApplicationScreen;
import promitech.colonization.gdx.Frame;
import promitech.colonization.ui.DoubleClickedListener;
import promitech.colonization.ui.UnitActionOrdersDialog;
import promitech.colonization.ui.UnitActionOrdersDialog.ActionTypes;
import promitech.colonization.ui.UnitActionOrdersDialog.UnitActionOrderItem;
import promitech.colonization.ui.resources.Messages;

public class ColonyApplicationScreen extends ApplicationScreen {

	private class ColonyUnitOrders implements UnitActionOrdersDialog.UnitOrderExecutor {
		
		@Override
	    public boolean executeCommand(UnitActor unitActor, UnitActionOrderItem item, UnitActionOrdersDialog dialog) {
	    	Unit unit = unitActor.unit;
	    	
	    	System.out.println("execute action type: " + item.actionType);
	    	if (ActionTypes.LIST_PRODUCTIONS.equals(item.actionType)) {
	    		dialog.clearItem();
	    		productionOrders(unit, dialog);
	    		dialog.pack();
	    		dialog.resetPositionToCenter();
	    		return false;
	    	}
	    	if (ActionTypes.LIST_CHANGE_PRODUCTIONS.equals(item.actionType)) {
	    		dialog.clearItem();
	    		terrainProductionOrders(unit, dialog);
	    		dialog.pack();
	    		dialog.resetPositionToCenter();
	    		return false;
	    	}
			
	    	if (ActionTypes.LEAVE_TOWN.equals(item.actionType)) {
	    		DragAndDropSourceContainer<UnitActor> source = (DragAndDropSourceContainer<UnitActor>)unitActor.dragAndDropSourceContainer;
	    		source.takePayload(unitActor, -1, -1);
	    		outsideUnitsPanel.putPayload(unitActor, -1, -1);
	    	}
	    	if (ActionTypes.EQUIPPED.equals(item.actionType)) {
	    		DragAndDropSourceContainer<UnitActor> source = (DragAndDropSourceContainer<UnitActor>)unitActor.dragAndDropSourceContainer;
	    		source.takePayload(unitActor, -1, -1);
	    		colony.changeUnitRole(unit, item.newRole);
	    		unitActor.updateTexture();
	    		outsideUnitsPanel.putPayload(unitActor, -1, -1);
	    	}
	    	if (ActionTypes.FORTIFY.equals(item.actionType)) {
	    		unit.setState(UnitState.FORTIFYING);
	    		guiGameController.nextActiveUnitWhenActive(unit);
	    	}
	    	if (ActionTypes.CLEAR_ORDERS.equals(item.actionType)) {
	    		unit.setState(UnitState.ACTIVE);
	    	}
	    	if (ActionTypes.SENTRY.equals(item.actionType)) {
	    		unit.setState(UnitState.SENTRY);
	    		guiGameController.nextActiveUnitWhenActive(unit);
	    	}
	    	if (ActionTypes.ACTIVATE.equals(item.actionType)) {
	    		guiGameController.closeColonyViewAndActiveUnit(colony, unit);
	    	}
	    	if (ActionTypes.ASSIGN_TO_PRODUCTION.equals(item.actionType)) {
	    		DragAndDropSourceContainer<UnitActor> source = (DragAndDropSourceContainer<UnitActor>)unitActor.dragAndDropSourceContainer;
	    		source.takePayload(unitActor, -1, -1);
	    		
	    		// from any location to building
	    		if (item.prodLocation.getBuilding() != null) {
	    			buildingsPanelActor.putWorkerOnBuilding(unitActor, item.prodLocation.getBuilding());
	    		}
	    		if (item.prodLocation.getColonyTile() != null) {
	    			terrainPanel.putWorkerOnTerrain(unitActor, item.prodLocation.getColonyTile());
	    		}
	    	}
	    	if (ActionTypes.CHANGE_TERRAIN_PRODUCTION.equals(item.actionType)) {
	    		// from terrain location to terrain location but diffrent goods type
	    		terrainPanel.changeWorkerProduction(unitActor, item.prodLocation);
	    	}
	    	return true;
		}
	    
	    private void createOrders(Unit unit, UnitActionOrdersDialog dialog) {
	        if (unit.isPerson()) {
	        	dialog.addCommandItem(new UnitActionOrderItem("model.unit.workingAs", ActionTypes.LIST_PRODUCTIONS));
	        }
	        if (colony.isUnitInColony(unit)) {
	        	if (colony.isUnitOnTerrain(unit)) {
	        		dialog.addCommandItem(new UnitActionOrderItem("model.unit.changeWork", ActionTypes.LIST_CHANGE_PRODUCTIONS));
	        	}
	            if (colony.canReducePopulation()) {
	            	dialog.addCommandItemSeparator();
	                addEquippedRoles(unit, dialog);
	            	dialog.addCommandItemSeparator();
	            	dialog.addCommandItem(new UnitActionOrderItem("leaveTown", ActionTypes.LEAVE_TOWN));
	            }
	        } else {
            	dialog.addCommandItemSeparator();
	            addEquippedRoles(unit, dialog);
            	dialog.addCommandItemSeparator();
	            addCommands(unit, dialog);
	        }
	    }
	    
	    private void addCommands(Unit unit, UnitActionOrdersDialog dialog) {
	        dialog.addCommandItem(new UnitActionOrderItem("activateUnit", ActionTypes.ACTIVATE));
	        if (unit.canChangeState(UnitState.FORTIFYING)) {
	        	dialog.addCommandItem(new UnitActionOrderItem("fortifyUnit", ActionTypes.FORTIFY));
	        }
	        dialog.addCommandItem(new UnitActionOrderItem("clearUnitOrders", ActionTypes.CLEAR_ORDERS));
			
			if (unit.canChangeState(UnitState.SENTRY)) {
				dialog.addCommandItem(new UnitActionOrderItem("sentryUnit", ActionTypes.SENTRY));
			}
	    }
	    
	    private void addEquippedRoles(Unit unit, UnitActionOrdersDialog dialog) {
	        if (unit.hasAbility(Ability.CAN_BE_EQUIPPED)) {
	            List<UnitRole> avaliableRoles = unit.avaliableRoles(colony.colonyUpdatableFeatures);
	            Collections.sort(avaliableRoles, ObjectWithId.INSERT_ORDER_ASC_COMPARATOR);
	            
	            System.out.println("avaliable roles size " + avaliableRoles.size());
	            for (UnitRole aRole : avaliableRoles) {
	                System.out.println("ur " + aRole);
	                if (unit.getUnitRole().equalsId(aRole)) {
	                    continue;
	                }
	                ProductionSummary required = UnitRoleLogic.minimumRequiredGoods(unit.getUnitRole(), aRole);
	                if (colony.getGoodsContainer().hasGoodsQuantity(required)) {
	                	dialog.addCommandItem(new UnitActionOrderItem(unit, aRole, required, ActionTypes.EQUIPPED));
	                }
	            }
	        }
	    }
	    
	    private void terrainProductionOrders(Unit unit, UnitActionOrdersDialog dialog) {
	    	List<GoodMaxProductionLocation> potentialTerrainProductions = colony.determinePotentialTerrainProductions(unit);
	    	Collections.sort(potentialTerrainProductions, GoodMaxProductionLocation.GOODS_INSERT_ORDER_ASC_COMPARATOR);
	    	
	    	System.out.println("PotentialTerrainProduction.size = " + potentialTerrainProductions.size());
	        for (GoodMaxProductionLocation g : potentialTerrainProductions) {
	            System.out.println("prod: " + g);
	        	dialog.addCommandItem(new UnitActionOrderItem(g, ActionTypes.CHANGE_TERRAIN_PRODUCTION));
	        }
	    }
		
		private void productionOrders(Unit unit, UnitActionOrdersDialog dialog) {
			java.util.List<GoodMaxProductionLocation> maxProductionForGoods = colony.determinePotentialMaxGoodsProduction(unit);
			Collections.sort(maxProductionForGoods, GoodMaxProductionLocation.GOODS_INSERT_ORDER_ASC_COMPARATOR);
			
			System.out.println("PotentialMaxGoodsProduction.size = " + maxProductionForGoods.size());
			for (GoodMaxProductionLocation g : maxProductionForGoods) {
				System.out.println("max: " + g);
				dialog.addCommandItem(new UnitActionOrderItem(g, ActionTypes.ASSIGN_TO_PRODUCTION));
			}
		}
	}
	
    private DragAndDrop unitsDragAndDrop;
    private DragAndDrop goodsDragAndDrop;
	private Stage stage;
	private BuildingsPanelActor buildingsPanelActor;
	private WarehousePanel warehousePanel;
	private TerrainPanel terrainPanel;
	private ActualBuildableItemActor actualBuildableItemActor; 
	private UnitsPanel outsideUnitsPanel;
	private UnitsPanel carrierUnitsPanel;
	private PopulationPanel populationPanel;
	private ProductionPanel productionPanel;

    private Colony colony;
    private Tile colonyTile;
    private GUIGameController guiGameController;
    private GUIGameModel guiGameModel;
    
    private final ColonyUnitOrders colonyUnitOrders = new ColonyUnitOrders();
	
	private final ChangeColonyStateListener changeColonyStateListener = new ChangeColonyStateListener() {
        @Override
        public void changeUnitAllocation() {
            colony.updateColonyPopulation();
            colony.updateModelOnWorkerAllocationOrGoodsTransfer();
            populationPanel.update(colony);
            productionPanel.init(colony, colonyTile);
            buildingsPanelActor.updateProductionDesc();
            terrainPanel.updateProduction();
            warehousePanel.updateGoodsQuantity(colony);
            actualBuildableItemActor.updateBuildItem(colony);
        }

        @Override
        public void transfereGoods() {
            colony.updateModelOnWorkerAllocationOrGoodsTransfer();
            productionPanel.init(colony, colonyTile);
            buildingsPanelActor.updateProductionDesc();
            warehousePanel.updateGoodsQuantity(colony);
			actualBuildableItemActor.updateBuildItem(colony);
        }

		@Override
		public void changeBuildingQueue() {
			actualBuildableItemActor.updateBuildItem(colony);
		}

		@Override
		public void addNotification(Notification notification) {
		}
    };

    private final DoubleClickedListener unitActorDoubleClickListener = new DoubleClickedListener() {
        public void doubleClicked(InputEvent event, float x, float y) {
            UnitActor unitActor = (UnitActor)event.getListenerActor();
            showUnitOrders(unitActor);
        }
    };

	@Override
	public void create() {
		guiGameController = di.guiGameController;
		guiGameModel = di.guiGameModel;
		
		//stage = new Stage(new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        stage = new Stage();
        
        unitsDragAndDrop = new DragAndDrop();
        unitsDragAndDrop.setDragActorPosition(0, 0);
        unitsDragAndDrop.setTapSquareSize(3);
        
        goodsDragAndDrop = new DragAndDrop();
        goodsDragAndDrop.setDragActorPosition(0, 0);
        goodsDragAndDrop.setTapSquareSize(3);
        
        GoodTransferActorBridge goodTransferActorBridge = new GoodTransferActorBridge();
        buildingsPanelActor = new BuildingsPanelActor(changeColonyStateListener, unitActorDoubleClickListener);
        warehousePanel = new WarehousePanel(changeColonyStateListener, goodTransferActorBridge);
        terrainPanel = new TerrainPanel(changeColonyStateListener, unitActorDoubleClickListener);
        outsideUnitsPanel = new UnitsPanel()
        		.withUnitChips(shape)
        		.withDragAndDrop(unitsDragAndDrop, changeColonyStateListener)
        		.withUnitDoubleClick(unitActorDoubleClickListener)
        		.withLabel(Messages.msg("outsideColony"));
		
        carrierUnitsPanel = new UnitsPanel()
        		.withUnitChips(shape)
        		.withUnitDoubleClick(unitActorDoubleClickListener)
        		.withUnitFocus(shape, goodsDragAndDrop, changeColonyStateListener)
        		.withLabel(Messages.msg("inPort"));
        
        populationPanel = new PopulationPanel();
        productionPanel = new ProductionPanel();
        actualBuildableItemActor = new ActualBuildableItemActor();
        
        goodTransferActorBridge.set(carrierUnitsPanel);
        goodTransferActorBridge.set(warehousePanel);
        
        Frame paperBackground = gameResources.getFrame("Paper");
        
        Table tableLayout = new Table();
        tableLayout.setBackground(new TiledDrawable(paperBackground.texture));
        
        
        
        
        VerticalGroup colGroup1 = new VerticalGroup();
        colGroup1.addActor(terrainPanel);
        colGroup1.addActor(populationPanel);
        colGroup1.addActor(actualBuildableItemActor);
        
        HorizontalGroup unitsGroup = new HorizontalGroup();
        unitsGroup.addActor(carrierUnitsPanel);
        unitsGroup.addActor(outsideUnitsPanel);
        
        
        Table spComponents = new Table();
        spComponents.add(colGroup1);
        spComponents.add(buildingsPanelActor);
        ScrollPane centerComponents = new ScrollPane(spComponents, GameResources.instance.getUiSkin());
        centerComponents.setForceScroll(false, false);
        centerComponents.setFadeScrollBars(false);
        centerComponents.setOverscroll(true, true);
        centerComponents.setScrollBarPositions(true, true);
        centerComponents.setScrollingDisabled(false, false);
        
        Table buttons = new Table();
        buttons.add(createBuildQueueButton()).expandX().fillX();
        buttons.add(createCloseButton()).expandX().fillX();
        
        tableLayout.setFillParent(true);
        tableLayout.add(buttons).fillX().row();
        tableLayout.add(productionPanel).fillX();
        tableLayout.row();
        
        tableLayout.add(centerComponents).fill().expand();
        tableLayout.row();
        tableLayout.add(unitsGroup).fillX();
        tableLayout.row();
        tableLayout.add(warehousePanel);
		
        stage.addActor(tableLayout);
        stage.setDebugAll(true);
	}

	private TextButton createBuildQueueButton() {
		String msg = Messages.msg("colonyPanel.buildQueue");
		TextButton textButton = new TextButton(msg, GameResources.instance.getUiSkin());
		textButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				BuildingQueueDialog dialog = new BuildingQueueDialog(
						stage.getHeight() * 0.75f,
						shape, guiGameModel.game, 
						colony, changeColonyStateListener
				);
				dialog.show(stage);
			}
		});
		return textButton;
	}
	
	private TextButton createCloseButton() {
		String msg = Messages.msg("close");
		TextButton closeButton = new TextButton(msg, GameResources.instance.getUiSkin());
		closeButton.addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				guiGameController.closeColonyView(colony);
				return true;
			}
		});
		return closeButton;
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
        outsideUnitsPanel.initUnits(colonyTile, Unit.NOT_CARRIER_UNIT_PREDICATE);
        carrierUnitsPanel.initUnits(colonyTile, Unit.CARRIER_UNIT_PREDICATE);
        
        populationPanel.update(colony);
        actualBuildableItemActor.updateBuildItem(colony);
    }
	
    private void showUnitOrders(UnitActor unitActor) {
    	UnitActionOrdersDialog dialog = new UnitActionOrdersDialog(shape, unitActor, colonyUnitOrders);
    	colonyUnitOrders.createOrders(unitActor.unit, dialog);
    	dialog.show(stage);
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
