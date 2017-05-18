package promitech.colonization.actors.map;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;

import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import promitech.colonization.gamelogic.MoveContext;

class UnitDislocationAnimation extends TemporalAction {
	private static final float UPDATE_DURATION = 0.2f; 
	
	private Unit unit;
	private Tile sourceTile;
	private Tile destTile;
	
	private final Vector2 source = new Vector2();
	private final Vector2 dest = new Vector2();
	private final Vector2 v = new Vector2();
	
	private Runnable endActionListener;
	
	public UnitDislocationAnimation() {
	}

	public String toString() {
		return "UnitDislocationAnimation " + unit.getId() + ", source:" + source + ", dest:" + dest;
	}
	
	public void init(MapRenderer mapRenderer, MoveContext moveContext) {
		this.unit = moveContext.unit;
		this.sourceTile = moveContext.sourceTile;
		this.destTile = moveContext.destTile;
		
		mapRenderer.mapToScreenCords(moveContext.sourceTile.x, moveContext.sourceTile.y, source);
		mapRenderer.mapToScreenCords(moveContext.destTile.x, moveContext.destTile.y, dest);
		
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
	
	public boolean isUnitAnimated(Unit u) {
		return this.unit != null && u != null && this.unit.equalsId(u);
	}

	public boolean isAnimatedSourceTile(int mapx, int mapy) {
		return sourceTile != null && sourceTile != null && sourceTile.x == mapx && sourceTile.y == mapy;
	}

	public boolean isTileAnimated(int mapx, int mapy) {
		return (sourceTile != null && sourceTile.x == mapx && sourceTile.y == mapy) ||
				(destTile != null && destTile.x == mapx && destTile.y == mapy);
	}
	
	public void drawUnit(ObjectsTileDrawer unitDrawer) {
		if (unit != null) {
			unitDrawer.drawUnit(unit, v);
		}
	}

	public void setEndActionListener(Runnable endActionListener) {
		this.endActionListener = endActionListener;
	}
}

