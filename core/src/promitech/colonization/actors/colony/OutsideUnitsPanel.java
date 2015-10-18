package promitech.colonization.actors.colony;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.Align;

import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.Unit.UnitState;
import promitech.colonization.GameResources;
import promitech.colonization.actors.map.MapRenderer;

class OutsideUnitsPanel extends ScrollPane {

	private final HorizontalGroup widgets = new HorizontalGroup();
	private final ShapeRenderer shapeRenderer;
	private Tile colonyTile;
	
	public OutsideUnitsPanel(ShapeRenderer shapeRenderer) {
		super(null, GameResources.instance.getUiSkin());
		this.shapeRenderer = shapeRenderer;
		setWidget(widgets);
		
        setForceScroll(false, false);
        setFadeScrollBars(false);
        setOverscroll(true, true);
        setScrollBarPositions(false, true);
        
        widgets.align(Align.center);
        widgets.space(15);
	}

	void putUnit(UnitActor unitActor) {
		unitActor.enableUnitChip(shapeRenderer);
		colonyTile.units.add(unitActor.unit);
		widgets.addActor(unitActor);
		
		unitActor.unit.setState(UnitState.ACTIVE);
		
		validate();
		setScrollPercentX(100);
	}

	public void takeUnit(UnitActor unitActor) {
		unitActor.disableUnitChip();
		
		colonyTile.units.removeId(unitActor.unit);
		widgets.removeActor(unitActor);
		
		validate();
		setScrollPercentX(100);
	}

	void initUnits(Tile colonyTile, DragAndDrop dragAndDrop) {
		this.colonyTile = colonyTile;
		dragAndDrop.addTarget(new OutsideUnitsDragAndDropTarget(this));
		
		widgets.clear();
		
		for (Unit unit : colonyTile.units.entities()) {
		    if (unit.isCarrier()) {
		        continue;
		    }
			UnitActor unitActor = new UnitActor(unit, true, shapeRenderer);
			dragAndDrop.addSource(new UnitDragAndDropSource(unitActor));
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
