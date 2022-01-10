package promitech.colonization.screen.ui;

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

import net.sf.freecol.common.model.GoodMaxProductionLocation;
import net.sf.freecol.common.model.ProductionSummary;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitRole;
import net.sf.freecol.common.model.colonyproduction.MaxGoodsProductionLocation;

import promitech.colonization.GameResources;
import promitech.colonization.gdx.Frame;
import promitech.colonization.infrastructure.FontResource;
import promitech.colonization.ui.ClosableDialog;
import promitech.colonization.ui.STable;
import promitech.colonization.ui.STableSelectListener;
import promitech.colonization.ui.resources.Messages;
import promitech.colonization.ui.resources.StringTemplate;

public class UnitActionOrdersDialog extends ClosableDialog<UnitActionOrdersDialog> {
	public static interface UnitOrderExecutor {
		/**
		 * should return true to close dialog 
		 */
		public boolean executeCommand(UnitActor unitActor, UnitActionOrderItem item, UnitActionOrdersDialog dialog);
	}
	
    public static enum ActionTypes {
        LIST_PRODUCTIONS,
        LIST_CHANGE_PRODUCTIONS,
        ASSIGN_TO_PRODUCTION,
        CHANGE_TERRAIN_PRODUCTION,
        LEAVE_TOWN,
        EQUIPPED,
        ACTIVATE,
        FORTIFY,
        CLEAR_ORDERS,
        SENTRY,
        SAIL_TO_NEW_WORLD
    }
    
    public static class UnitActionOrderItem {
        public final ActionTypes actionType;
        
        public final UnitRole newRole; 
        public final MaxGoodsProductionLocation prodLocation;

        private String label;
        private String goodsIds[];
        
        private UnitActionOrderItem(ActionTypes actionType) {
            this.actionType = actionType;
            this.prodLocation = null;
            this.newRole = null;
        }
        
		public UnitActionOrderItem(MaxGoodsProductionLocation prodLocation, ActionTypes actionType) {
			this.actionType = actionType;
            this.prodLocation = prodLocation;
            this.newRole = null;
            goodsIds = new String[] {prodLocation.getGoodsType().getId()};
            
            StringTemplate t = StringTemplate.template(prodLocation.getGoodsType().getId() + ".workAs")
                    .addAmount("%amount%", prodLocation.getProduction())
                    .add("%claim%", "");
            label = Messages.message(t);
        }
        
        public UnitActionOrderItem(Unit unit, UnitRole toRole, ProductionSummary required, ActionTypes actionType) {
            this.actionType = actionType;
            this.prodLocation = null;
            this.newRole = toRole;
            
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
    
    private final ShapeRenderer shape;
    private STable actionsTable;
    private ScrollPane verticalListScrollPane;
    private final UnitActor unitActor;
    private final UnitOrderExecutor unitOrderExecutor;
	
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
    
	public UnitActionOrdersDialog(ShapeRenderer shape, UnitActor unitActor, UnitOrderExecutor unitOrderExecutor) {
        this.unitActor = unitActor;
        this.shape = shape;
        this.unitOrderExecutor = unitOrderExecutor;
        
        createComponents();

        verticalListScrollPane.setScrollPercentY(100);
    }

    protected void executeCommand(UnitActionOrderItem item) {
    	if (unitOrderExecutor.executeCommand(unitActor, item, this)) {
    		hideWithoutFade();
    	}
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

	public void clearItem() {
		actionsTable.clear();
	}
	
	public void addCommandItemSeparator() {
		actionsTable.addSeparator();		
	}
	
    public void addCommandItem(UnitActionOrderItem item) {
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
