package promitech.colonization.actors.colony;

import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
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
import promitech.colonization.actors.OutsideUnitsPanel;
import promitech.colonization.actors.UnitActor;
import promitech.colonization.gdx.Frame;
import promitech.colonization.infrastructure.FontResource;
import promitech.colonization.ui.ClosableDialog;
import promitech.colonization.ui.STable;
import promitech.colonization.ui.STableSelectListener;
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
    
    class UnitActionOrderItem {
        final ActionTypes actionType;
        
        UnitRole newRole; 
        GoodMaxProductionLocation prodLocation;

        private String label;
        private String goodsIds[];
        
        private UnitActionOrderItem(ActionTypes actionType) {
            this.actionType = actionType;
        }
        
		public UnitActionOrderItem(GoodMaxProductionLocation prodLocation, ActionTypes actionType) {
            this(actionType);
            this.prodLocation = prodLocation;
            goodsIds = new String[] {prodLocation.getGoodsType().getId()};
            
            StringTemplate t = StringTemplate.template(prodLocation.getGoodsType().getId() + ".workAs")
                    .addAmount("%amount%", prodLocation.getProduction())
                    .add("%claim%", "");
            label = Messages.message(t);
        }
        
        public UnitActionOrderItem(Unit unit, UnitRole toRole, ProductionSummary required, ActionTypes actionType) {
            this(actionType);
            newRole = toRole;
            
            goodsIds = new String[required.size()];
            int i = 0;
            for (Entry<String> goodEntry : required.entries()) {
                if (goodEntry.value <= 0) {
                    continue;
                }
                goodsIds[i] = goodEntry.key;
                i++;
            }
            UnitRole fromRole = unit.getUnitRole();
            
            String msgKey = "model.role.change." + toRole.getRoleSuffixWithDefault();
            if (!Messages.containsKey(msgKey)) {
                // Fall back to the full "from"."to" key
                msgKey = "model.role.change." + fromRole.getRoleSuffixWithDefault() + "." + toRole.getRoleSuffixWithDefault();
            }
            label = Messages.msg(msgKey);
        }
        
        public UnitActionOrderItem(String labelKey, ActionTypes actionType) {
            this(actionType);
            label = Messages.msg(labelKey);
        }
        
        public String getLabel() {
            return label;
        }

        public String[] getGoodsImageIds() {
            return goodsIds;
        }
    }
    private final int[] actionTableAligment = new int[] { Align.right, Align.left };
    
    private final GUIGameController gameController;
    private final ShapeRenderer shape;
    private STable actionsTable;
    private ScrollPane verticalListScrollPane;
    private final Colony colony;
    private final Unit unit;
    private final UnitActor unitActor;
    private final OutsideUnitsPanel outsideUnitsPanel;
	private final TerrainPanel terrainPanel;
	private final BuildingsPanelActor buildingsPanelActor; 
	
    private STableSelectListener onSelectListener = new STableSelectListener() {
        @Override
        public void onSelect(Object payload) {
            if (payload instanceof UnitActionOrderItem) {
                executeCommand((UnitActionOrderItem)payload);
            } else {
                throw new IllegalStateException("can not recognize payload: " + payload);
            }
        }
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
    		gameController.nextActiveUnitWhenActive(unit);
    	}
    	if (ActionTypes.CLEAR_ORDERS.equals(item.actionType)) {
    		unit.setState(UnitState.ACTIVE);
    	}
    	if (ActionTypes.SENTRY.equals(item.actionType)) {
    		unit.setState(UnitState.SENTRY);
    		gameController.nextActiveUnitWhenActive(unit);
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
	    actionsTable = new STable(shape);
	    actionsTable.addSelectListener(onSelectListener);
	    actionsTable.rowsPad(5, 10, 5, 10);
    	
        verticalListScrollPane = new ScrollPane(actionsTable, GameResources.instance.getUiSkin());
        verticalListScrollPane.setFlickScroll(false);
        verticalListScrollPane.setScrollingDisabled(true, false);
        verticalListScrollPane.setForceScroll(false, false);
        verticalListScrollPane.setFadeScrollBars(false);
        verticalListScrollPane.setOverscroll(true, true);
        verticalListScrollPane.setScrollBarPositions(false, true);

        getContentTable().add(verticalListScrollPane).pad(5);
        
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
            
            System.out.println("avaliable roles size " + avaliableRoles.size());
            for (UnitRole aRole : avaliableRoles) {
                System.out.println("ur " + aRole);
                if (unit.getUnitRole().equalsId(aRole)) {
                    continue;
                }
                ProductionSummary required = unit.getUnitRole().requiredGoodsToChangeRoleTo(aRole);
                if (colony.getGoodsContainer().hasGoodsQuantity(required)) {
                	addCommandItem(new UnitActionOrderItem(unit, aRole, required, ActionTypes.EQUIPPED));
                }
            }
        }
    }
    
    private void terrainProductionOrders() {
    	List<GoodMaxProductionLocation> potentialTerrainProductions = colony.determinePotentialTerrainProductions(unit);
    	Collections.sort(potentialTerrainProductions, GoodMaxProductionLocation.GOODS_INSERT_ORDER_ASC_COMPARATOR);
    	
    	System.out.println("PotentialTerrainProduction.size = " + potentialTerrainProductions.size());
        for (GoodMaxProductionLocation g : potentialTerrainProductions) {
            System.out.println("prod: " + g);
        	addCommandItem(new UnitActionOrderItem(g, ActionTypes.CHANGE_TERRAIN_PRODUCTION));
        }
    }
    
    private void productionOrders() {
        java.util.List<GoodMaxProductionLocation> maxProductionForGoods = colony.determinePotentialMaxGoodsProduction(unit);
        Collections.sort(maxProductionForGoods, GoodMaxProductionLocation.GOODS_INSERT_ORDER_ASC_COMPARATOR);
        
        System.out.println("PotentialMaxGoodsProduction.size = " + maxProductionForGoods.size());
        for (GoodMaxProductionLocation g : maxProductionForGoods) {
            System.out.println("max: " + g);
        	addCommandItem(new UnitActionOrderItem(g, ActionTypes.ASSIGN_TO_PRODUCTION));
        }
    }

    private void addCommandItem(UnitActionOrderItem item) {
        Label label = new Label(item.getLabel(), labelStyle());
        label.setAlignment(Align.left);
                
        HorizontalGroup imgs = new HorizontalGroup();
        if (item.getGoodsImageIds() != null) {
            for (String goodsId : item.getGoodsImageIds()) {
                if (goodsId != null) {
                    imgs.addActor(goodsImage(goodsId));
                }
            }
        }
        actionsTable.addRow(item, actionTableAligment, imgs, label);
    }
    
    private LabelStyle labelStyle() {
        LabelStyle labelStyle = GameResources.instance.getUiSkin().get(LabelStyle.class);
        labelStyle.font = FontResource.getGoodsQuantityFont();
        return labelStyle;
    }

    private Image goodsImage(String goodsTypeId) {
        Frame resGoodsImage = GameResources.instance.goodsImage(goodsTypeId);
        return new Image(new TextureRegionDrawable(resGoodsImage.texture), Scaling.none, Align.left);
    }
}
