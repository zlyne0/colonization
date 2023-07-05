package promitech.colonization.screen.map.unitanimation;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import com.badlogic.gdx.utils.Array;

import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import promitech.colonization.screen.map.MapRenderer;
import promitech.colonization.screen.map.ObjectsTileDrawer;

class UnitDisappearAnimation extends TemporalAction implements UnitTileAnimation {
	private static final float UPDATE_DURATION = 0.7f; 
	
	private Unit unit;
	private Tile tile;
	private final Vector2 pos = new Vector2();
	private float alpha = 1.0f;

	private final Array<Runnable> animationEndListeners = new Array<>(2);
	
	UnitDisappearAnimation() {
	}

	@Override
	public String toString() {
		return "UnitDisappearAnimation " + unit.getId() + ", tile:" + tile;
	}
	
	void init(Unit unit, Tile tile, Runnable endActionListener) {
		this.unit = unit;
		this.tile = tile;
		this.animationEndListeners.clear();
		this.animationEndListeners.add(endActionListener);
		this.alpha = 1.0f;
		
		restart();
		setDuration(UPDATE_DURATION);
	}
	
    @Override
    public void initMapPos(MapRenderer mapRenderer) {
        mapRenderer.mapToScreenCords(tile.x, tile.y, pos);
    }
	
	@Override
	protected void begin() {
	}
	
	@Override
	protected void end() {
		unit = null;
		tile = null;
		for (Runnable animationEndListener : animationEndListeners) {
			animationEndListener.run();
		}
		animationEndListeners.clear();
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

	@Override
	public void addEndListener(Runnable listener) {
		this.animationEndListeners.add(listener);
	}
}

