package promitech.colonization.actors.map;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;

import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;

class UnitDislocationAnimation extends TemporalAction {
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

	public String toString() {
		return "UnitDislocationAnimation " + unit.getId() + ", source:" + source + ", dest:" + dest;
	}
	
	void init(MapRenderer mapRenderer, Unit unit, Tile sourceTile, Tile destTile, Runnable endActionListener) {
		this.unit = unit;
		this.sourceTile = sourceTile;
		this.destTile = destTile;
		this.endActionListener = endActionListener;
		
		mapRenderer.mapToScreenCords(sourceTile.x, sourceTile.y, source);
		mapRenderer.mapToScreenCords(destTile.x, destTile.y, dest);
		
		v.set(source);
		
		restart();
		setDuration(UPDATE_DURATION);
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
	
	boolean isUnitAnimated(Unit u) {
		return this.unit != null && u != null && this.unit.equalsId(u);
	}

	boolean isAnimatedSourceTile(int mapx, int mapy) {
		return sourceTile != null && sourceTile != null && sourceTile.x == mapx && sourceTile.y == mapy;
	}

	boolean isTileAnimated(int mapx, int mapy) {
		return (sourceTile != null && sourceTile.x == mapx && sourceTile.y == mapy) ||
				(destTile != null && destTile.x == mapx && destTile.y == mapy);
	}
	
	void drawUnit(ObjectsTileDrawer unitDrawer) {
		if (unit != null) {
			unitDrawer.drawUnit(unit, v);
		}
	}
}

