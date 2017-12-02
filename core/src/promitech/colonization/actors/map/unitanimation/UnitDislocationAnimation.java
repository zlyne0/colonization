package promitech.colonization.actors.map.unitanimation;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;

import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import promitech.colonization.actors.map.MapRenderer;
import promitech.colonization.actors.map.ObjectsTileDrawer;

class UnitDislocationAnimation extends TemporalAction implements UnitTileAnimation {
	private static final float UPDATE_DURATION = 0.2f; 
	
	private Unit unit;
	private Tile sourceTile;
	private Tile destTile;
	
	private final Vector2 source = new Vector2();
	private final Vector2 dest = new Vector2();
	private final Vector2 v = new Vector2();
	
	private Runnable endActionListener;
	
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
		this.endActionListener = endActionListener;
		
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
		
		if (endActionListener != null) {
			endActionListener.run();
		}
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

}

