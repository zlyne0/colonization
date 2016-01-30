package promitech.colonization.actors.colony;

import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ObjectIntMap.Entry;
import com.badlogic.gdx.utils.Scaling;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.GoodMaxProductionLocation;
import net.sf.freecol.common.model.ObjectWithId;
import net.sf.freecol.common.model.ProductionSummary;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.Unit.UnitState;
import net.sf.freecol.common.model.UnitRole;
import net.sf.freecol.common.model.specification.Ability;
import promitech.colonization.GUIGameController;
import promitech.colonization.GameResources;
import promitech.colonization.gdx.Frame;
import promitech.colonization.infrastructure.FontResource;
import promitech.colonization.ui.DoubleClickedListener;
import promitech.colonization.ui.ClosableDialog;
import promitech.colonization.ui.SelectableRowTable;
import promitech.colonization.ui.SelectableTableItem;
import promitech.colonization.ui.resources.Messages;
import promitech.colonization.ui.resources.StringTemplate;

class UnitActionOrdersDialog extends ClosableDialog {
	
    enum ActionTypes {
        LIST_PRODUCTIONS,
        LIST_CHANGE_PRODUCTIONS,
        ASSIGN_TO_PRODUCTION,
        CHANGE_TERRAIN_PRODUCTION,
        LEAVE_TOWN,
        EQUIPPED,
        ACTIVATE,
        FORTIFY,
        CLEAR_ORDERS,
        SENTRY
    }
    
    class SeparatorMenuItem extends HorizontalGroup {
        @Override
        public float getPrefHeight() {
            return 20f;
        }
    }
    
    class UnitActionOrderItem extends SelectableTableItem<ActionTypes> {
        final ActionTypes actionType;
        
        UnitRole newRole; 
        GoodMaxProductionLocation prodLocation;

        private UnitActionOrderItem(ActionTypes actionType) {
            super(actionType);
            this.actionType = actionType;
        }
        
		public UnitActionOrderItem(GoodMaxProductionLocation prodLocation, ActionTypes actionType) {
            this(actionType);
            this.prodLocation = prodLocation;
            
            addImage(goodsImage(prodLocation.getGoodsType().getId()));
            
            StringTemplate t = StringTemplate.template(prodLocation.getGoodsType().getId() + ".workAs")
                    .addAmount("%amount%", prodLocation.getProduction())
                    .addName("%claim%", "");            
            addLabel(Messages.message(t));
        }
        
        public UnitActionOrderItem(Unit unit, UnitRole toRole, ProductionSummary required, ActionTypes actionType) {
            this(actionType);
            newRole = toRole;
            
            for (Entry<String> goodEntry : required.entries()) {
                if (goodEntry.value <= 0) {
                    continue;
                }
                addImage(goodsImage(goodEntry.key));
            }
            UnitRole fromRole = unit.getUnitRole();
            
            String msgKey = "model.role.change." + toRole.getRoleSuffixWithDefault();
            if (!Messages.containsKey(msgKey)) {
                // Fall back to the full "from"."to" key
                msgKey = "model.role.change." + fromRole.getRoleSuffixWithDefault() + "." + toRole.getRoleSuffixWithDefault();
            }
            addLabel(Messages.msg(msgKey));
        }
        
        public UnitActionOrderItem(String labelKey, ActionTypes actionType) {
            this(actionType);
            addLabel(Messages.msg(labelKey));
        }
        
        private void addImage(Image image) {
            this.add(image).left();
        }
        
        private void addLabel(String msg) {
            Label label = new Label(msg, labelStyle());
            label.setAlignment(Align.left);
            label.setFillParent(true);
            this.add(label).expand().fill().left();
        }
        
        private Image goodsImage(String goodsTypeId) {
            Frame resGoodsImage = GameResources.instance.goodsImage(goodsTypeId);
            Image image = new Image(new TextureRegionDrawable(resGoodsImage.texture), Scaling.none, Align.left);
            return image;
        }
        
        private LabelStyle labelStyle() {
            LabelStyle labelStyle = GameResources.instance.getUiSkin().get(LabelStyle.class);
            labelStyle.font = FontResource.getGoodsQuantityFont();
            return labelStyle;
        }
    }
    
    private final GUIGameController gameController;
    private final ShapeRenderer shape;
    private SelectableRowTable actionsTable;
    private ScrollPane verticalListScrollPane;
    private final Colony colony;
    private final Unit unit;
    private final UnitActor unitActor;
    private final OutsideUnitsPanel outsideUnitsPanel;
	private final TerrainPanel terrainPanel;
	private final BuildingsPanelActor buildingsPanelActor; 
	
    private DoubleClickedListener unitActionOrderItemClickedListener = new DoubleClickedListener() {
    	public void doubleClicked(InputEvent event, float x, float y) {
    	    if (event.getListenerActor() instanceof UnitActionOrderItem) {
    	        UnitActionOrderItem item = (UnitActionOrderItem)event.getListenerActor();
    	        item.setUnselected();
    	        executeCommand(item);
    	    }
    	};
    };
    
    UnitActionOrdersDialog(
    		ShapeRenderer shape, Colony colony, UnitActor unitActor, 
    		OutsideUnitsPanel outsideUnitsPanel, 
    		TerrainPanel terrainPanel, 
    		BuildingsPanelActor buildingsPanelActor, 
    		GUIGameController gameController
    ) {
        super("", GameResources.instance.getUiSkin());
        this.gameController = gameController;
        this.colony = colony;
        this.unit = unitActor.unit;
        this.unitActor = unitActor;
        this.terrainPanel = terrainPanel;
        this.buildingsPanelActor = buildingsPanelActor;
        
        this.shape = shape;
        this.outsideUnitsPanel = outsideUnitsPanel;
        
        createComponents();

        if (unit.isPerson()) {
        	addCommandItem(new UnitActionOrderItem("model.unit.workingAs", ActionTypes.LIST_PRODUCTIONS));
        }
        if (colony.isUnitInColony(unit)) {
        	if (colony.isUnitOnTerrain(unit)) {
        		addCommandItem(new UnitActionOrderItem("model.unit.changeWork", ActionTypes.LIST_CHANGE_PRODUCTIONS));
        	}
            if (colony.canReducePopulation()) {
                actionsTable.addSeparator();
                addEquippedRoles();
                actionsTable.addSeparator();
            	addCommandItem(new UnitActionOrderItem("leaveTown", ActionTypes.LEAVE_TOWN));
            }
        } else {
            actionsTable.addSeparator();
            addEquippedRoles();
            actionsTable.addSeparator();
            addCommands();
        }
        verticalListScrollPane.setScrollPercentY(100);
    }

    protected void executeCommand(UnitActionOrderItem item) {
    	System.out.println("execute action type: " + item.actionType);
    	if (ActionTypes.LIST_PRODUCTIONS.equals(item.actionType)) {
    	    actionsTable.clear();
    		productionOrders();
    		this.pack();
    		this.resetPositionToCenter();
    		return;
    	}
    	if (ActionTypes.LIST_CHANGE_PRODUCTIONS.equals(item.actionType)) {
    	    actionsTable.clear();
    		terrainProductionOrders();
    		this.pack();
    		this.resetPositionToCenter();
    		return;
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
    	}
    	if (ActionTypes.CLEAR_ORDERS.equals(item.actionType)) {
    		unit.setState(UnitState.ACTIVE);
    	}
    	if (ActionTypes.SENTRY.equals(item.actionType)) {
    		unit.setState(UnitState.SENTRY);
    	}
    	if (ActionTypes.ACTIVATE.equals(item.actionType)) {
    		gameController.closeColonyViewAndActiveUnit(colony, unit);
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
    	hideWithoutFade();
	}
    
	private void createComponents() {
	    actionsTable = new SelectableRowTable(shape);
	    actionsTable.setDoubleClickedListener(unitActionOrderItemClickedListener);
    	
        verticalListScrollPane = new ScrollPane(actionsTable, GameResources.instance.getUiSkin());
        verticalListScrollPane.setFlickScroll(false);
        
        verticalListScrollPane.setScrollingDisabled(true, false);
        verticalListScrollPane.setForceScroll(false, false);
        verticalListScrollPane.setFadeScrollBars(false);
        verticalListScrollPane.setOverscroll(true, true);
        verticalListScrollPane.setScrollBarPositions(false, true);

        getContentTable().add(verticalListScrollPane);
        
        TextButton cancelButton = new TextButton(Messages.msg("cancel"), GameResources.instance.getUiSkin());
        cancelButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                hideWithFade();
            }
        });
        getButtonTable().add(cancelButton);
        withHidingOnEsc();
    }

    private void addCommands() {
        addCommandItem(new UnitActionOrderItem("activateUnit", ActionTypes.ACTIVATE));
        if (unit.canChangeState(UnitState.FORTIFYING)) {
        	addCommandItem(new UnitActionOrderItem("fortifyUnit", ActionTypes.FORTIFY));
        }
		addCommandItem(new UnitActionOrderItem("clearUnitOrders", ActionTypes.CLEAR_ORDERS));
		
		if (unit.canChangeState(UnitState.SENTRY)) {
			addCommandItem(new UnitActionOrderItem("sentryUnit", ActionTypes.SENTRY));
		}
    }
    
    private void addEquippedRoles() {
        if (unit.hasAbility(Ability.CAN_BE_EQUIPPED)) {
            List<UnitRole> avaliableRoles = unit.avaliableRoles();
            Collections.sort(avaliableRoles, ObjectWithId.INSERT_ORDER_ASC_COMPARATOR);
            
            for (UnitRole aRole : avaliableRoles) {
                if (unit.getUnitRole().equalsId(aRole)) {
                    continue;
                }
                ProductionSummary required = unit.getUnitRole().requiredGoodsToChangeRoleTo(aRole);
                if (colony.getGoodsContainer().hasGoodsQuantity(required)) {
                	addCommandItem(new UnitActionOrderItem(unit, aRole, required, ActionTypes.EQUIPPED));
                }
            }
            
            System.out.println("avaliable roles size " + avaliableRoles.size());
            for (UnitRole ur : avaliableRoles) {
                System.out.println("ur " + ur);
            }
        }
    }
    
    private void terrainProductionOrders() {
    	List<GoodMaxProductionLocation> potentialTerrainProductions = colony.determinePotentialTerrainProductions(unit);
    	Collections.sort(potentialTerrainProductions, GoodMaxProductionLocation.GOODS_INSERT_ORDER_ASC_COMPARATOR);
    	
    	System.out.println("PotentialTerrainProduction.size = " + potentialTerrainProductions.size());
        for (GoodMaxProductionLocation g : potentialTerrainProductions) {
            System.out.println("prod: " + g);
        }
        for (GoodMaxProductionLocation g : potentialTerrainProductions) {
        	addCommandItem(new UnitActionOrderItem(g, ActionTypes.CHANGE_TERRAIN_PRODUCTION));
        }
    }
    
    private void productionOrders() {
        java.util.List<GoodMaxProductionLocation> maxProductionForGoods = colony.determinePotentialMaxGoodsProduction(unit);
        Collections.sort(maxProductionForGoods, GoodMaxProductionLocation.GOODS_INSERT_ORDER_ASC_COMPARATOR);
        
        System.out.println("PotentialMaxGoodsProduction.size = " + maxProductionForGoods.size());
        for (GoodMaxProductionLocation g : maxProductionForGoods) {
            System.out.println("max: " + g);
        }
        for (GoodMaxProductionLocation g : maxProductionForGoods) {
        	addCommandItem(new UnitActionOrderItem(g, ActionTypes.ASSIGN_TO_PRODUCTION));
        }
    }
    
    private void addCommandItem(UnitActionOrderItem item) {
        actionsTable.addCell(item)
            .expand()
            .fill()
            .align(Align.left)
            .spaceTop(5);
        actionsTable.nextRow();
    }
}
