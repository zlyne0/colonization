package promitech.colonization.actors;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.Align;

import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.Unit.UnitState;
import net.sf.freecol.common.model.UnitLocation;
import promitech.colonization.GameResources;
import promitech.colonization.actors.colony.DragAndDropSourceContainer;
import promitech.colonization.actors.colony.DragAndDropTargetContainer;
import promitech.colonization.actors.map.MapRenderer;
import promitech.colonization.ui.DoubleClickedListener;

public class OutsideUnitsPanel extends ScrollPane implements DragAndDropSourceContainer<UnitActor>, DragAndDropTargetContainer<UnitActor> {

    private final ChangeColonyStateListener changeColonyStateListener;
    private final DoubleClickedListener unitActorDoubleClickListener;
	private final HorizontalGroup widgets = new HorizontalGroup();
	private final ShapeRenderer shapeRenderer;
	private final DragAndDrop unitDragAndDrop;
	private UnitLocation unitLocation;
	
	public OutsideUnitsPanel(ShapeRenderer shapeRenderer, DragAndDrop unitDragAndDrop, ChangeColonyStateListener changeColonyStateListener, DoubleClickedListener unitActorDoubleClickListener) {
		super(null, GameResources.instance.getUiSkin());
		this.changeColonyStateListener = changeColonyStateListener;
		this.unitActorDoubleClickListener = unitActorDoubleClickListener;
		this.shapeRenderer = shapeRenderer;
		this.unitDragAndDrop = unitDragAndDrop;
		setWidget(widgets);
		
        setForceScroll(false, false);
        setFadeScrollBars(false);
        setOverscroll(true, true);
        setScrollBarPositions(false, true);
        
        widgets.align(Align.center);
        widgets.space(15);
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

	public void initUnits(UnitLocation unitLocation) {
		this.unitLocation = unitLocation;
		unitDragAndDrop.addTarget(new UnitDragAndDropTarget(this, this));
		
		widgets.clear();
		
		for (Unit unit : unitLocation.getUnits().entities()) {
		    if (unit.isCarrier()) {
		        continue;
		    }
			UnitActor unitActor = new UnitActor(unit, unitActorDoubleClickListener);
			unitActor.enableUnitChip(shapeRenderer);
			
			unitActor.dragAndDropSourceContainer = this;
			unitDragAndDrop.addSource(new UnitDragAndDropSource(unitActor));
			
			widgets.addActor(unitActor);
		}
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
