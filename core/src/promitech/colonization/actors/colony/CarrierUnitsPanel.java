package promitech.colonization.actors.colony;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.Align;

import net.sf.freecol.common.model.GoodsContainer;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.specification.AbstractGoods;
import promitech.colonization.GameResources;
import promitech.colonization.actors.map.MapRenderer;

class CarrierUnitsPanel extends HorizontalGroup {
    class UnitsPanel extends ScrollPane {
        private final HorizontalGroup widgets = new HorizontalGroup();
        
        UnitsPanel() {
            super(null, GameResources.instance.getUiSkin());
            setWidget(widgets);
            
            setForceScroll(false, false);
            setFadeScrollBars(false);
            setOverscroll(true, true);
            
            widgets.align(Align.center);
            widgets.space(15);
        }
    
        public void clear() {
            widgets.clear();
        }
        
        void disableAllUnitActors() {
            for (Actor a : widgets.getChildren()) {
                if (a instanceof UnitActor) {
                    ((UnitActor)a).disableFocus();
                }
            }
        }
        
        void addUnit(UnitActor unitActor) {
            widgets.addActor(unitActor);
        }
        
        @Override
        public float getPrefWidth() {
            return MapRenderer.TILE_WIDTH*2;
        }
        @Override
        public float getPrefHeight() {
            return MapRenderer.TILE_HEIGHT*2;
        }
    }

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
        		GoodActor goodActor = new GoodActor(carrierGood.getTypeId(), carrierGood.getQuantity());
        		goodActor.dragAndDropSourceContainer = this;
        		goodsDragAndDrop.addSource(new GoodActor.GoodsDragAndDropSource(goodActor));
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
            
            unitActor.unit.getGoodsContainer().increaseGoodsQuantity(anAbstractGood);
            updateCargoPanelData();
            changeColonyStateListener.transfereGoods();
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
    
	public CarrierUnitsPanel(ShapeRenderer shapeRenderer, DragAndDrop goodsDragAndDrop, ChangeColonyStateListener changeColonyStateListener) {
	    this.changeColonyStateListener = changeColonyStateListener;
		this.shapeRenderer = shapeRenderer;
		this.goodsDragAndDrop = goodsDragAndDrop;
		
		carrierUnitsPanel = new UnitsPanel();
		cargoPanel = new CargoPanel(goodsDragAndDrop);
		
		addActor(carrierUnitsPanel);
		addActor(cargoPanel);
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
	
	void initUnits(Tile colonyTile) {
	    carrierUnitsPanel.clear();
	    cargoPanel.clear();
	    
	    goodsDragAndDrop.addTarget(new GoodActor.GoodsDragAndDropTarget(cargoPanel, cargoPanel));
	    
		boolean firstUnit = true;
		for (Unit unit : colonyTile.units.entities()) {
		    if (!unit.isCarrier()) {
		        continue;
		    }
		    UnitActor unitActor = new UnitActor(unit);
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
