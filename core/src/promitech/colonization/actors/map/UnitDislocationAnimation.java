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
	
	private final Vector2 source = new Vector2();
	private final Vector2 dest = new Vector2();
	private final Vector2 v = new Vector2();
	
	public UnitDislocationAnimation() {
	}

	public String toString() {
		return "UnitDislocationAnimation " + unit.getId() + ", source:" + source + ", dest:" + dest;
	}
	
	public void init(MapRenderer mapRenderer, MoveContext moveContext) {
		this.unit = moveContext.unit;
		this.sourceTile = moveContext.sourceTile;
		
		mapRenderer.mapToScreenCords(moveContext.sourceTile.x, moveContext.sourceTile.y, source);
		mapRenderer.mapToScreenCords(moveContext.destTile.x, moveContext.destTile.y, dest);
		
		restart();
		setDuration(UPDATE_DURATION);
	}
	
	@Override
	protected void begin() {
		v.set(0, 0);
	}
	
	@Override
	protected void end() {
		unit = null;
		sourceTile = null;
	}
	
	@Override
	protected void update(float percent) {
		v.set(dest).sub(source).scl(percent);
		v.add(source);
	}
	
	public boolean isUnitAnimated(Unit u) {
		return this.unit != null && u != null && this.unit.equalsId(u);
	}

	public boolean isAnimatedUnitTile(int mapx, int mapy) {
		return unit != null && sourceTile != null && sourceTile.x == mapx && sourceTile.y == mapy;
	}

	public void drawUnit(ObjectsTileDrawer unitDrawer) {
		if (unit != null) {
			unitDrawer.drawUnit(unit, v);
		}
	}

}

