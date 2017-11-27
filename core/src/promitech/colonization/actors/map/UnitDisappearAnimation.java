package promitech.colonization.actors.map;

import com.badlogic.gdx.math.Vector2;

import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;

class UnitDisappearAnimation extends UnitTileAnimation {
	private static final float UPDATE_DURATION = 0.7f; 
	
	private Unit unit;
	private Tile tile;
	private final Vector2 pos = new Vector2();
	private float alpha = 1.0f;
	
	private Runnable endActionListener;
	
	UnitDisappearAnimation() {
	}

	@Override
	public String toString() {
		return "UnitDisappearAnimation " + unit.getId() + ", tile:" + tile;
	}
	
	void init(Unit unit, Tile tile, Runnable endActionListener) {
		this.unit = unit;
		this.tile = tile;
		this.endActionListener = endActionListener;
		this.alpha = 1.0f;
		
		restart();
		setDuration(UPDATE_DURATION);
	}
	
    @Override
    void initMapPos(MapRenderer mapRenderer) {
        mapRenderer.mapToScreenCords(tile.x, tile.y, pos);
    }
	
	@Override
	protected void begin() {
	}
	
	@Override
	protected void end() {
		unit = null;
		tile = null;
		if (endActionListener != null) {
			endActionListener.run();
		}
	}
	
	@Override
	protected void update(float percent) {
		alpha = 1f - percent;
	}
	
	@Override
	public Unit getUnit() {
		return this.unit;
	}
	
	@Override
	public void drawUnit(ObjectsTileDrawer unitDrawer) {
		if (unit != null) {
			unitDrawer.drawUnit(unit, pos, alpha);
		}
	}
}

