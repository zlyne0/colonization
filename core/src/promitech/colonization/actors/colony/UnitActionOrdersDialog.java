package promitech.colonization.actors.colony;

import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ObjectIntMap.Entry;

import net.sf.freecol.common.model.Ability;
import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.GoodMaxProductionLocation;
import net.sf.freecol.common.model.ObjectWithId;
import net.sf.freecol.common.model.ProductionSummary;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitRole;
import promitech.colonization.GameResources;
import promitech.colonization.gdx.Frame;
import promitech.colonization.infrastructure.FontResource;
import promitech.colonization.ui.DoubleClickedListener;
import promitech.colonization.ui.resources.Messages;
import promitech.colonization.ui.resources.StringTemplate;

class UnitActionOrdersDialog extends Dialog {
	
    enum ActionTypes {
        SHOW_ONLY_PRODUCTIONS,
        LIST_PRODUCTIONS,
        ASSIGN_TO_PRODUCTION,
        LEAVE_TOWN,
        EQUIPPED,
        ACTIVATE,
        FORTIFY,
        CLEAR_ORDERS,
        SENTRY
    }
    
    class ActionMenuItem extends HorizontalGroup {
        protected boolean selected = false;
        private ShapeRenderer shape;
        
        @Override
        public void draw(Batch batch, float parentAlpha) {
            if (selected) {
                
                if (shape == null) {
                    shape = new ShapeRenderer();
                    shape.setProjectionMatrix(batch.getProjectionMatrix());
                    shape.setTransformMatrix(batch.getTransformMatrix());
                }
                batch.end();
                
                shape.begin(ShapeType.Filled);
                shape.setColor(Color.YELLOW);
                shape.rect(getX(), getY(), getWidth(), getHeight());
                shape.end();
                
                batch.begin();
            }
            super.draw(batch, parentAlpha);
        }
        
        public void setUnselected() {
            selected = false;
        }

        public void setSelected() {
            selected = true;
        }
    }
    
    class SeparatorMenuItem extends HorizontalGroup {
        @Override
        public float getPrefHeight() {
            return 20f;
        }
    }
    
    class UnitActionOrderItem extends ActionMenuItem {
        final ActionTypes actionType;

        private UnitActionOrderItem(ActionTypes actionType) {
            pad(5);
            this.actionType = actionType;
        }
        
		public UnitActionOrderItem(GoodMaxProductionLocation g, ActionTypes actionType) {
            this(actionType);
            
            Image image = goodsImage(g.getGoodsType().getId());
            
            StringTemplate t = StringTemplate.template(g.getGoodsType().getId() + ".workAs")
                    .addAmount("%amount%", g.getProduction())
                    .addName("%claim%", "");            
            String msg = Messages.message(t);
            
            Label label = new Label(msg, labelStyle());
            label.setFillParent(true);
            this.addActor(image);
            this.addActor(label);
        }
        
        public UnitActionOrderItem(Unit unit, UnitRole toRole, ProductionSummary required, ActionTypes actionType) {
            this(actionType);
            
            for (Entry<String> goodEntry : required.entries()) {
                if (goodEntry.value <= 0) {
                    continue;
                }
                Image image = goodsImage(goodEntry.key);
                this.addActor(image);
            }
            UnitRole fromRole = unit.getUnitRole();
            
            String msgKey = "model.role.change." + toRole.getRoleSuffixWithDefault();
            if (!Messages.containsKey(msgKey)) {
                // Fall back to the full "from"."to" key
                msgKey = "model.role.change." + fromRole.getRoleSuffixWithDefault() + "." + toRole.getRoleSuffixWithDefault();
            }
            String msg = Messages.msg(msgKey);
            
            Label label = new Label(msg, labelStyle());
            label.setFillParent(true);
            this.addActor(label);
        }
        
        public UnitActionOrderItem(String labelKey, ActionTypes actionType) {
            this(actionType);
        	
            String msg = Messages.msg(labelKey);
            Label label = new Label(msg, labelStyle());
            label.setFillParent(true);
            this.addActor(label);
        }
        
        private Image goodsImage(String goodsTypeId) {
            Frame resGoodsImage = GameResources.instance.goodsImage(goodsTypeId);
            Image image = new Image(resGoodsImage.texture);
            return image;
        }
        
        private LabelStyle labelStyle() {
            LabelStyle labelStyle = GameResources.instance.getUiSkin().get(LabelStyle.class);
            labelStyle.font = FontResource.getGoodsQuantityFont();
            return labelStyle;
        }
    }
    
    private Table tableLayout;
    private ScrollPane verticalListScrollPane;
    
    private DoubleClickedListener unitActionOrderItemClickedListener = new DoubleClickedListener() {
    	public void clicked(InputEvent event, float x, float y) {
    		super.clicked(event, x, y);
    		
    		for (Actor a : tableLayout.getChildren()) {
    		    if (a instanceof ActionMenuItem) {
    		        ActionMenuItem item = (ActionMenuItem)a;
    		        item.setUnselected();
    		    }
    		}
    		
    		UnitActionOrderItem item = (UnitActionOrderItem)event.getListenerActor();
    		item.setSelected();
    	};
    	
    	public void doubleClicked(InputEvent event, float x, float y) {
    	    UnitActionOrderItem item = (UnitActionOrderItem)event.getListenerActor();
    	    
    	    System.out.println("action type = " + item.actionType);
    	    
    	};
    };
    
    UnitActionOrdersDialog(Colony colony, Unit unit) {
        super("", GameResources.instance.getUiSkin());

        createComponents();

        addCommandItem(new UnitActionOrderItem("model.unit.changeWork", ActionTypes.LIST_PRODUCTIONS));
        addSeparator();
        addCommandItem(new UnitActionOrderItem("leaveTown", ActionTypes.LEAVE_TOWN));
        
        if (colony.isUnitInColony(unit)) {
            if (colony.canReducePopulation()) {
                addEquippedRoles(colony, unit);
                addSeparator();
            	addCommandItem(new UnitActionOrderItem("leaveTown", ActionTypes.LEAVE_TOWN));
            }
        } else {
            addEquippedRoles(colony, unit);
            addSeparator();
            addCommands();
        }
        verticalListScrollPane.setScrollPercentY(100);
    }
    
    private void createComponents() {
    	tableLayout = new Table();
    	tableLayout.setFillParent(true);
    	
        verticalListScrollPane = new ScrollPane(tableLayout, GameResources.instance.getUiSkin());
        verticalListScrollPane.setFlickScroll(false);
        
        verticalListScrollPane.setScrollingDisabled(true, false);
        verticalListScrollPane.setForceScroll(false, false);
        verticalListScrollPane.setFadeScrollBars(false);
        verticalListScrollPane.setOverscroll(true, true);
        verticalListScrollPane.setScrollBarPositions(false, true);

        getContentTable().add(verticalListScrollPane).maxHeight(500);
        
        TextButton cancelButton = new TextButton(Messages.msg("cancel"), GameResources.instance.getUiSkin());
        cancelButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                hide();
            }
        });
        getButtonTable().add(cancelButton);
        addListener(new InputListener() {
            public boolean keyDown (InputEvent event, int keycode2) {
                if (Keys.ENTER == keycode2) {
                    hide();
                }
                if (Keys.ESCAPE == keycode2) {
                    hide();
                }
                return false;
            }
        });
    }

    private void addCommands() {
        addCommandItem(new UnitActionOrderItem("activateUnit", ActionTypes.ACTIVATE));
		addCommandItem(new UnitActionOrderItem("fortifyUnit", ActionTypes.FORTIFY));
		addCommandItem(new UnitActionOrderItem("clearUnitOrders", ActionTypes.CLEAR_ORDERS));
		addCommandItem(new UnitActionOrderItem("sentryUnit", ActionTypes.SENTRY));
    }
    
    private void addEquippedRoles(Colony colony, Unit unit) {
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
    
    private void addGoodProductionOrders(Colony colony, Unit unit) {
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
    
    private void addSeparator() {
        tableLayout.add(new SeparatorMenuItem()).fillX().spaceTop(5).row();
    }
    
    private void addCommandItem(UnitActionOrderItem item) {
    	item.addListener(unitActionOrderItemClickedListener);
    	tableLayout.add(item).fillX().spaceTop(5).row();
    }
}
