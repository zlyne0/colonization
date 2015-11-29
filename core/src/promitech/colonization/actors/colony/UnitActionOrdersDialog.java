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
	
    public class UnitActionOrderItem extends HorizontalGroup {
        private boolean selected = false;

        private UnitActionOrderItem() {
            pad(5);
        }
        
		public UnitActionOrderItem(GoodMaxProductionLocation g) {
            this();
            
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
        
        public UnitActionOrderItem(Unit unit, UnitRole toRole, ProductionSummary required) {
            this();
            
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
        
        public UnitActionOrderItem(String labelKey) {
            this();
        	
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
    
    private Table tableLayout;
    private ScrollPane verticalListScrollPane;
    
    private DoubleClickedListener unitActionOrderItemClickedListener = new DoubleClickedListener() {
    	public void clicked(InputEvent event, float x, float y) {
    		super.clicked(event, x, y);
    		
    		for (Actor a : tableLayout.getChildren()) {
    			UnitActionOrderItem item = (UnitActionOrderItem)a;
    			item.setUnselected();
    		}
    		
    		UnitActionOrderItem item = (UnitActionOrderItem)event.getListenerActor();
    		item.setSelected();
    	};
    	
    	public void doubleClicked(InputEvent event, float x, float y) {
    	};
    };
    
    UnitActionOrdersDialog(Colony colony, Unit unit) {
        super("", GameResources.instance.getUiSkin());

        createComponents();

        addGoodProductionOrders(colony, unit);
        
        if (colony.isUnitInColony(unit)) {
            if (colony.canReducePopulation()) {
            	addCommandItem(new UnitActionOrderItem("leaveTown"));
                addEquippedRoles(colony, unit);
            }
        } else {
            addEquippedRoles(colony, unit);
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
        addCommandItem(new UnitActionOrderItem("activateUnit"));
		addCommandItem(new UnitActionOrderItem("fortifyUnit"));
		addCommandItem(new UnitActionOrderItem("clearUnitOrders"));
		addCommandItem(new UnitActionOrderItem("sentryUnit"));
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
                	addCommandItem(new UnitActionOrderItem(unit, aRole, required));
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
        	addCommandItem(new UnitActionOrderItem(g));
        }
    }
    
    private void addCommandItem(UnitActionOrderItem item) {
    	item.addListener(unitActionOrderItemClickedListener);
    	tableLayout.add(item).fillX().spaceTop(5).row();
    }
}
