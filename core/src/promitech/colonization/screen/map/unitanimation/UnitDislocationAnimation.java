package promitech.colonization.screen.map.unitanimation;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import com.badlogic.gdx.utils.Array;

import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import promitech.colonization.screen.map.MapRenderer;
import promitech.colonization.screen.map.ObjectsTileDrawer;

class UnitDislocationAnimation extends TemporalAction implements UnitTileAnimation {
	private static final float UPDATE_DURATION = 0.2f; 
	
	private Unit unit;
	private Tile sourceTile;
	private Tile destTile;
	
	private final Vector2 source = new Vector2();
	private final Vector2 dest = new Vector2();
	private final Vector2 v = new Vector2();

	private final Array<Runnable> animationEndListeners = new Array<>(2);
	
	UnitDislocationAnimation() {
	}

    @Override
	public String toString() {
		return "UnitDislocationAnimation " + unit.getId() + ", source:" + source + ", dest:" + dest;
	}
	
	void init(Unit unit, Tile sourceTile, Tile destTile, Runnable endActionListener) {
		this.unit = unit;
		this.sourceTile = sourceTile;
		this.destTile = destTile;
		this.animationEndListeners.clear();
		this.animationEndListeners.add(endActionListener);

		restart();
		setDuration(UPDATE_DURATION);
	}
	
	@Override
	public void initMapPos(MapRenderer mapRenderer) {
        mapRenderer.mapToScreenCords(sourceTile.x, sourceTile.y, source);
        mapRenderer.mapToScreenCords(destTile.x, destTile.y, dest);
        v.set(source);
	}
	
	@Override
	protected void begin() {
	}
	
	@Override
	protected void end() {
		unit = null;
		sourceTile = null;
		destTile = null;

		for (Runnable animationEndListener : animationEndListeners) {
			animationEndListener.run();
		}
		animationEndListeners.clear();
	}
	
	@Override
	protected void update(float percent) {
		v.set(dest).sub(source).scl(percent);
		v.add(source);
	}
	
	@Override
	public Unit getUnit() {
		return unit;
	}
	
	@Override
	public void drawUnit(ObjectsTileDrawer unitDrawer) {
		if (unit != null) {
			unitDrawer.drawUnit(unit, v);
		}
	}

	@Override
	public void addEndListener(Runnable listener) {
		this.animationEndListeners.add(listener);
	}
}

