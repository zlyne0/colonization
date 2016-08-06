package promitech.colonization.actors;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.Align;

import net.sf.freecol.common.model.GoodsContainer;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitLocation;
import net.sf.freecol.common.model.specification.AbstractGoods;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.GameResources;
import promitech.colonization.actors.colony.DragAndDropSourceContainer;
import promitech.colonization.actors.colony.DragAndDropTargetContainer;
import promitech.colonization.ui.DoubleClickedListener;

public class CarrierUnitsPanel extends Table {
    class CargoPanel extends ScrollPane implements DragAndDropSourceContainer<AbstractGoods>, DragAndDropTargetContainer<AbstractGoods> {
        private final HorizontalGroup widgets = new HorizontalGroup();
        private static final float GOOD_IMG_WIDTH = 32;
        private static final float ITEMS_SPACE = 20;
        private UnitActor unitActor;
        
        private final DragAndDrop goodsDragAndDrop;
        
        CargoPanel(DragAndDrop goodsDragAndDrop) {
            super(null, GameResources.instance.getUiSkin());
            setWidget(widgets);
            
            setForceScroll(false, false);
            setFadeScrollBars(false);
            setOverscroll(true, true);
            
            widgets.align(Align.center);
            widgets.space(15);
            widgets.padBottom(20);
            widgets.padTop(20);
            
            this.goodsDragAndDrop = goodsDragAndDrop;
        }
        
        public void clear() {
            widgets.clear();
            unitActor = null;
        }
        
        void initCargo(UnitActor unitActor) {
            this.unitActor = unitActor;
            updateCargoPanelData();
        }
        
        private void updateCargoPanelData() {
        	widgets.clear();
        	GoodsContainer goodsContainer = unitActor.unit.getGoodsContainer();
        	for (AbstractGoods carrierGood : goodsContainer.carrierGoods()) {
        		GoodsType goodsType = Specification.instance.goodsTypes.getById(carrierGood.getTypeId());
        		
        		QuantityGoodActor goodActor = new QuantityGoodActor(goodsType, carrierGood.getQuantity());
        		goodActor.dragAndDropSourceContainer = this;
        		goodsDragAndDrop.addSource(new QuantityGoodActor.GoodsDragAndDropSource(goodActor));
        		widgets.addActor(goodActor);
        	}
        }
        
        @Override
        public float getPrefWidth() {
            return 6 * GOOD_IMG_WIDTH + 6 * ITEMS_SPACE;
        }

        @Override
        public float getPrefHeight() {
        	float h = super.getPrefHeight();
        	if (!isForceScrollX()) {
        		float scrollbarHeight = 0;
        		if (getStyle().hScrollKnob != null) {
        			scrollbarHeight = getStyle().hScrollKnob.getMinHeight();
        		}
        		if (getStyle().hScroll != null) {
        			scrollbarHeight = Math.max(scrollbarHeight, getStyle().hScroll.getMinHeight());
        		}
				h += scrollbarHeight;
        	}
        	return h;
        }
        
        @Override
        public void putPayload(AbstractGoods anAbstractGood, float x, float y) {
            System.out.println("carrierPanel: carrierId[" + unitActor.unit.getId() + "] put goods " + anAbstractGood);
            
            if (anAbstractGood.isNotEmpty()) {
	            unitActor.unit.getGoodsContainer().increaseGoodsQuantity(anAbstractGood);
	            updateCargoPanelData();
	            changeColonyStateListener.transfereGoods();
            }
        }

        @Override
        public boolean canPutPayload(AbstractGoods anAbstractGood, float x, float y) {
        	return unitActor != null && unitActor.unit.hasSpaceForAdditionalCargo(anAbstractGood);
        }

        @Override
        public void takePayload(AbstractGoods anAbstractGood, float x, float y) {
            System.out.println("carrierPanel: carrierId[" + unitActor.unit.getId() + "] take goods " + anAbstractGood);
            
            unitActor.unit.getGoodsContainer().decreaseGoodsQuantity(anAbstractGood);
            updateCargoPanelData();
            changeColonyStateListener.transfereGoods();
        }
    }

	private final ShapeRenderer shapeRenderer;
	private final DragAndDrop goodsDragAndDrop;
	
	private final UnitsPanel carrierUnitsPanel;
	private final CargoPanel cargoPanel;
	
	private final ChangeColonyStateListener changeColonyStateListener;
	private final DoubleClickedListener unitActorDoubleClickListener;
    
	public CarrierUnitsPanel(ShapeRenderer shapeRenderer, DragAndDrop goodsDragAndDrop, 
	    ChangeColonyStateListener changeColonyStateListener, DoubleClickedListener unitActorDoubleClickListener
	) {
	    this.changeColonyStateListener = changeColonyStateListener;
	    this.unitActorDoubleClickListener = unitActorDoubleClickListener;
		this.shapeRenderer = shapeRenderer;
		this.goodsDragAndDrop = goodsDragAndDrop;
		
		carrierUnitsPanel = new UnitsPanel();
		cargoPanel = new CargoPanel(goodsDragAndDrop);
		
		add(carrierUnitsPanel).fillX().expandX();
		add(cargoPanel);
	}
	
	private ClickListener unitActorClickListener = new ClickListener() {
	    @Override
	    public void clicked(InputEvent event, float x, float y) {
	        carrierUnitsPanel.disableAllUnitActors();
	        UnitActor unitActor = (UnitActor)event.getTarget(); 
	        unitActor.enableFocus(shapeRenderer);
	        cargoPanel.initCargo(unitActor);
	    }
	};
	
	public void initUnits(UnitLocation unitLocation) {
	    carrierUnitsPanel.clear();
	    cargoPanel.clear();
	    
	    goodsDragAndDrop.addTarget(new QuantityGoodActor.GoodsDragAndDropTarget(cargoPanel, cargoPanel));
	    
		boolean firstUnit = true;
		for (Unit unit : unitLocation.getUnits().entities()) {
		    if (!unit.isCarrier()) {
		        continue;
		    }
		    UnitActor unitActor = new UnitActor(unit, unitActorDoubleClickListener);
		    unitActor.enableUnitChip(shapeRenderer);
		    if (firstUnit) {
		        firstUnit = false;
		        unitActor.enableFocus(shapeRenderer);
		        cargoPanel.initCargo(unitActor);
		    }
		    unitActor.addListener(unitActorClickListener);
		    carrierUnitsPanel.addUnit(unitActor);
		}
	}
}
