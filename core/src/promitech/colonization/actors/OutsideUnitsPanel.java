package promitech.colonization.actors;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.Align;

import net.sf.freecol.common.model.GoodsContainer;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.Unit.UnitPredicate;
import net.sf.freecol.common.model.Unit.UnitState;
import net.sf.freecol.common.model.UnitLocation;
import net.sf.freecol.common.model.specification.AbstractGoods;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.GameResources;
import promitech.colonization.actors.colony.DragAndDropSourceContainer;
import promitech.colonization.actors.colony.DragAndDropTargetContainer;
import promitech.colonization.actors.map.MapRenderer;
import promitech.colonization.infrastructure.FontResource;
import promitech.colonization.ui.DoubleClickedListener;

class CargoPanel extends Table implements DragAndDropSourceContainer<AbstractGoods>, DragAndDropTargetContainer<AbstractGoods> {
	private static final float ITEMS_SPACE = 15;
	
	private final ChangeColonyStateListener changeColonyStateListener;
	private float cargoWidthWidth;
	private final DragAndDrop goodsDragAndDrop;
	private Unit unit;
	private final float prefHeight;
	
	CargoPanel(DragAndDrop goodsDragAndDrop, ChangeColonyStateListener changeColonyStateListener) {
		setTouchable(Touchable.enabled);
		
		this.goodsDragAndDrop = goodsDragAndDrop;
		this.changeColonyStateListener = changeColonyStateListener;
		
		prefHeight = LabelGoodActor.goodsImgHeight() + ITEMS_SPACE;
		
		align(Align.left);
		defaults().align(Align.left);
	}
	
	void initCargoForUnit(Unit unit) {
		this.unit = unit;
		cargoWidthWidth = unit.unitType.getSpace() * (LabelGoodActor.goodsImgWidth() + ITEMS_SPACE);
		
		updateCargoPanelData();
	}
	
	private void updateCargoPanelData() {
		clear();
		
		GoodsContainer goodsContainer = unit.getGoodsContainer();
		for (AbstractGoods carrierGood : goodsContainer.carrierGoods()) {
			GoodsType goodsType = Specification.instance.goodsTypes.getById(carrierGood.getTypeId());
			
			QuantityGoodActor goodActor = new QuantityGoodActor(goodsType, carrierGood.getQuantity());
			goodActor.dragAndDropSourceContainer = this;
			goodsDragAndDrop.addSource(new QuantityGoodActor.GoodsDragAndDropSource(goodActor));			
			add(goodActor)
				.width(goodActor.getPrefWidth() + ITEMS_SPACE)
				.height(goodActor.getPrefHeight() + ITEMS_SPACE);
		}
	}
	
	@Override
	public float getPrefWidth() {
		return cargoWidthWidth;
	}
	
	@Override
	public float getPrefHeight() {
		return prefHeight;
	}
	
	@Override
	public void putPayload(AbstractGoods anAbstractGood, float x, float y) {
		System.out.println("carrierPanel: carrierId[" + unit.getId() + "] put goods " + anAbstractGood);
		
		if (anAbstractGood.isNotEmpty()) {
			unit.getGoodsContainer().increaseGoodsQuantity(anAbstractGood);
			updateCargoPanelData();
			changeColonyStateListener.transfereGoods();
		}
	}
	
	@Override
	public boolean canPutPayload(AbstractGoods anAbstractGood, float x, float y) {
		return unit.hasSpaceForAdditionalCargo(anAbstractGood);
	}
	
	@Override
	public void takePayload(AbstractGoods anAbstractGood, float x, float y) {
		System.out.println("carrierPanel: carrierId[" + unit.getId() + "] take goods " + anAbstractGood);
		
		unit.getGoodsContainer().decreaseGoodsQuantity(anAbstractGood);
		updateCargoPanelData();
		changeColonyStateListener.transfereGoods();
	}
}

public class OutsideUnitsPanel extends ScrollPane implements DragAndDropSourceContainer<UnitActor>, DragAndDropTargetContainer<UnitActor> {

	
	private final HorizontalGroup widgets = new HorizontalGroup();
	
    private ChangeColonyStateListener changeColonyStateListener;
    private DoubleClickedListener unitActorDoubleClickListener;
	private ShapeRenderer shapeRenderer;
	private DragAndDrop unitDragAndDrop;
	private UnitLocation unitLocation;
	
	private boolean showUnitChip = false;
	private boolean withUnitFocus = false;
    private Label label;
    private UnitActor selectedActor;
    private CargoPanel cargoPanel;
	
	private final ClickListener unitClickListener = new ClickListener() {
	    @Override
	    public void clicked(InputEvent event, float x, float y) {
	    	if (selectedActor != null) {
	    		selectedActor.disableFocus();
	    	}
	        UnitActor unitActor = (UnitActor)event.getTarget();
	        selectedActor = unitActor;
	        selectedActor.enableFocus(shapeRenderer);
	        
	        updateCargo(selectedActor);
	    }
	};
    
    
	public OutsideUnitsPanel() {
		super(null, GameResources.instance.getUiSkin());
		setWidget(widgets);
		
        setForceScroll(false, false);
        setFadeScrollBars(false);
        setOverscroll(true, true);
        setScrollBarPositions(false, true);
        
        widgets.align(Align.center);
        widgets.space(15);
	}
	
	public OutsideUnitsPanel withDragAndDrop(DragAndDrop unitDragAndDrop, ChangeColonyStateListener changeColonyStateListener) {
		this.changeColonyStateListener = changeColonyStateListener;
		this.unitDragAndDrop = unitDragAndDrop;
		return this;
	}

	public OutsideUnitsPanel withUnitChips(ShapeRenderer shapeRenderer) {
		this.shapeRenderer = shapeRenderer;
		this.showUnitChip = true;
		return this;
	}
	
	public OutsideUnitsPanel withUnitDoubleClick(DoubleClickedListener unitActorDoubleClickListener) {
		this.unitActorDoubleClickListener = unitActorDoubleClickListener;
		return this;
	}
	
	public OutsideUnitsPanel withUnitFocus(ShapeRenderer shapeRenderer, DragAndDrop goodsDragAndDrop, ChangeColonyStateListener changeColonyStateListener) {
		this.shapeRenderer = shapeRenderer;
		this.withUnitFocus = true;
		this.changeColonyStateListener = changeColonyStateListener;
		
		cargoPanel = new CargoPanel(goodsDragAndDrop, changeColonyStateListener);
		goodsDragAndDrop.addTarget(new QuantityGoodActor.GoodsDragAndDropTarget(cargoPanel, cargoPanel));
		return this;
	}
	
    public void setLabelStr(String labelStr) {
    	if (label == null) {
    		label = new Label(labelStr, labelStyle());
    	} else {
    		label.setText(labelStr);
    	}
    }
    
    private LabelStyle labelStyle() {
        LabelStyle labelStyle = GameResources.instance.getUiSkin().get("black", LabelStyle.class);
        labelStyle.font = FontResource.getUnitBoxFont();
        return labelStyle;
    }	

    @Override
    public void draw(Batch batch, float parentAlpha) {
    	super.draw(batch, parentAlpha);
    	if (label != null) {
	    	label.setX(getX());
	    	label.setY(getY() + getHeight() - label.getHeight());
	    	label.draw(batch, parentAlpha);
    	}
    }
	
	@Override
	public void takePayload(UnitActor unitActor, float x, float y) {
		System.out.println("take unit [" + unitActor.unit + "] from unitLocation " + unitLocation);

		unitActor.dragAndDropSourceContainer = null;
		unitActor.disableUnitChip();
		
		unitLocation.getUnits().removeId(unitActor.unit);
		widgets.removeActor(unitActor);
		
		validate();
		setScrollPercentX(100);
	}
	
	@Override
	public boolean canPutPayload(UnitActor unitActor, float x, float y) {
		return true;
	}
	
	@Override
	public void putPayload(UnitActor unitActor, float x, float y) {
		System.out.println("put unit [" + unitActor.unit + "] to unitLocation " + unitLocation);
		
		unitActor.unit.setState(UnitState.ACTIVE);
		unitActor.unit.changeUnitLocation(unitLocation);
		
		unitActor.dragAndDropSourceContainer = this;
		unitActor.enableUnitChip(shapeRenderer);
		widgets.addActor(unitActor);
		
		validate();
		setScrollPercentX(100);
		
		changeColonyStateListener.changeUnitAllocation();
	}

	public void clearUnits() {
		widgets.clear();
	}
	
	public void initUnits(UnitLocation aUnitLocation, UnitPredicate aUnitPredicate) {
		this.unitLocation = aUnitLocation;
		if (unitDragAndDrop != null) {
			unitDragAndDrop.addTarget(new UnitDragAndDropTarget(this, this));
		}
		widgets.clear();

		boolean first = true;
		for (Unit unit : aUnitLocation.getUnits().entities()) {
			if (aUnitPredicate.obtains(unit)) {
				UnitActor unitActor = addUnit(unit);
				if (withUnitFocus && first) {
					first = false;
					unitActor.enableFocus(shapeRenderer);
					selectedActor = unitActor;
					
					updateCargo(selectedActor);
				}
			}
		}
	}

	public UnitActor addUnit(Unit unit) {
		UnitActor unitActor = new UnitActor(unit, unitActorDoubleClickListener);
		if (unitClickListener != null) {
			unitActor.addListener(unitClickListener);
		}
		if (showUnitChip) {
			unitActor.enableUnitChip(shapeRenderer);
		}
		
		unitActor.dragAndDropSourceContainer = this;
		if (unitDragAndDrop != null) {
			unitDragAndDrop.addSource(new UnitDragAndDropSource(unitActor));
		}

		widgets.addActor(unitActor);
		return unitActor;
	}
	
	private void updateCargo(UnitActor unitActor) {
        cargoPanel.initCargoForUnit(unitActor.unit);
        widgets.addActorAfter(selectedActor, cargoPanel);
	}
	
	@Override
	public float getPrefWidth() {
		return MapRenderer.TILE_WIDTH*3;
	}
	
	@Override
	public float getPrefHeight() {
		return MapRenderer.TILE_HEIGHT*2;
	}
}
