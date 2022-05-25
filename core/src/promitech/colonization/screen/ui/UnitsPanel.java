package promitech.colonization.screen.ui;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.Align;

import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.Unit.UnitPredicate;
import net.sf.freecol.common.model.Unit.UnitState;
import net.sf.freecol.common.model.UnitLocation;

import promitech.colonization.GameResources;
import promitech.colonization.infrastructure.FontResource;
import promitech.colonization.screen.colony.DragAndDropSourceContainer;
import promitech.colonization.screen.colony.DragAndDropTargetContainer;
import promitech.colonization.screen.map.MapRenderer;
import promitech.colonization.ui.DoubleClickedListener;

public class UnitsPanel extends ScrollPane implements DragAndDropSourceContainer<UnitActor>, DragAndDropTargetContainer<UnitActor> {

	private static final float CORNER_SKIN_DECORATION_SIZE = 25;

	private final HorizontalGroup widgets = new HorizontalGroup();
	
    private ChangeColonyStateListener changeColonyStateListener;
    private DoubleClickedListener unitActorDoubleClickListener;
	private ShapeRenderer shapeRenderer;
	private DragAndDrop unitDragAndDrop;
	private DragAndDrop goodsDragAndDrop;
	private UnitLocation unitLocation;
	
	private boolean showUnitChip = false;
	private boolean withUnitFocus = false;
	private boolean readonlyCargoSummaryPanel = false;
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

	public UnitsPanel() {
		this(null);
	}
    
	public UnitsPanel(String title) {
		super(null, GameResources.instance.getUiSkin());
		ScrollPaneStyle frameStyle = new ScrollPaneStyle(this.getStyle());
		frameStyle.background = new FrameWithCornersDrawableSkin(title, FontResource.getUnitBoxFont(), GameResources.instance);
		setStyle(frameStyle);
		
		setActor(widgets);
		
        setForceScroll(false, false);
        setFadeScrollBars(false);
        setOverscroll(true, true);
        setScrollBarPositions(false, true);
        
        widgets.padLeft(CORNER_SKIN_DECORATION_SIZE);
        widgets.padRight(CORNER_SKIN_DECORATION_SIZE);
        widgets.align(Align.left);
        widgets.space(15);
	}
	
	public UnitActor getSelectedCarrierUnit() {
		if (cargoPanel == null) {
			return null;
		}
		return selectedActor;
	}
	
	public UnitsPanel withDragAndDrop(DragAndDrop unitDragAndDrop, ChangeColonyStateListener changeColonyStateListener) {
		this.changeColonyStateListener = changeColonyStateListener;
		this.unitDragAndDrop = unitDragAndDrop;
		return this;
	}

	public UnitsPanel withUnitChips(ShapeRenderer shapeRenderer) {
		this.shapeRenderer = shapeRenderer;
		this.showUnitChip = true;
		return this;
	}
	
	public UnitsPanel withUnitDoubleClick(DoubleClickedListener unitActorDoubleClickListener) {
		this.unitActorDoubleClickListener = unitActorDoubleClickListener;
		return this;
	}
	
	public UnitsPanel withUnitFocus(ShapeRenderer shapeRenderer, DragAndDrop goodsDragAndDrop, ChangeColonyStateListener changeColonyStateListener) {
		this.shapeRenderer = shapeRenderer;
		this.withUnitFocus = true;
		this.changeColonyStateListener = changeColonyStateListener;
		this.goodsDragAndDrop = goodsDragAndDrop;
		
		cargoPanel = new CargoPanel(goodsDragAndDrop, changeColonyStateListener);
		return this;
	}
	
	public void changeLabel(String title) {
		((FrameWithCornersDrawableSkin)getStyle().background).changeTitle(title, FontResource.getUnitBoxFont()); 
	}
	
	@Override
	public void takePayload(UnitActor unitActor, float x, float y) {
		System.out.println("take unit [" + unitActor.unit + "] from unitLocation " + unitLocation);

		unitActor.dragAndDropSourceContainer = null;
		unitActor.removeListener(unitClickListener);
		unitActor.disableUnitChip();
		
		unitLocation.removeUnit(unitActor.unit);
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
		if (withUnitFocus) {
			unitActor.addListener(unitClickListener);
		}
		widgets.addActor(unitActor);
		
		validate();
		setScrollPercentX(100);
		
		changeColonyStateListener.changeUnitAllocation();
	}

	@Override
	public void onDragPayload(float x, float y) {
	}

	@Override
	public void onLeaveDragPayload() {
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
		selectedActor = null;

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
		if (showUnitChip) {
			unitActor.enableUnitChip(shapeRenderer);
		}
		
		unitActor.dragAndDropSourceContainer = this;
		if (unitDragAndDrop != null) {
			unitDragAndDrop.addSource(new UnitDragAndDropSource(unitActor));
		}

		if (withUnitFocus) {
			unitActor.addListener(unitClickListener);
			unitActor.withCargoPanel(cargoPanel, changeColonyStateListener);
			goodsDragAndDrop.addTarget(new QuantityGoodActor.GoodsDragAndDropTarget(unitActor, unitActor));
			goodsDragAndDrop.addTarget(new QuantityGoodActor.GoodsDragAndDropTarget(cargoPanel, cargoPanel));
			goodsDragAndDrop.addTarget(new QuantityGoodActor.GoodsDragAndDropTarget(this, cargoPanel));
		}

		widgets.addActor(unitActor);
		if (readonlyCargoSummaryPanel) {
			widgets.addActor(new ReadonlyCargoSummaryPanel(unit, GameResources.instance));
		}
		return unitActor;
	}
	
	private void updateCargo(UnitActor unitActor) {
		if (withUnitFocus) {
	        cargoPanel.initCargoForUnit(unitActor);
	        widgets.removeActor(cargoPanel);
	        widgets.addActorAfter(selectedActor, cargoPanel);
		}
	}
	
	@Override
	public float getPrefHeight() {
		return MapRenderer.TILE_HEIGHT*2;
	}

	public UnitsPanel withReadonlyCargoSummaryPanel() {
		this.readonlyCargoSummaryPanel = true;
		return this;
	}
}

