package promitech.colonization.actors.map;

import com.badlogic.gdx.math.Vector2;

import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import promitech.colonization.gamelogic.MoveContext;

public class UnitDislocationAnimation {
	private static final float STEP_DISTANCE = 0.07f; 
	
	public static abstract class EndOfAnimationListener {
		public MoveContext moveContext;
		public abstract void end(Unit unit);
	}
	
	private EndOfAnimationListener endOfAnimationListener;
	private Unit unit;
	private final Vector2 source = new Vector2();
	private final Vector2 dest = new Vector2();
	private float step = 0;
	private final Vector2 v = new Vector2();
	private Tile sourceTile;
	
	public UnitDislocationAnimation() {
	}

	public void init(MapRenderer mapRenderer, MoveContext moveContext) {
		this.unit = moveContext.unit;
		this.step = 0;
		this.sourceTile = moveContext.sourceTile;
		v.set(0, 0);
		
		mapRenderer.mapToScreenCords(moveContext.sourceTile.x, moveContext.sourceTile.y, source);
		mapRenderer.mapToScreenCords(moveContext.destTile.x, moveContext.destTile.y, dest);
	}
	
	public boolean isUnitAnimated(Unit u) {
		return this.unit != null && u != null && this.unit.equalsId(u);
	}
	
	private boolean nextStep() {
		step += STEP_DISTANCE;
		v.set(dest).sub(source).scl(step);
		v.add(source);
		if (step >= 1) {
			Unit tmpUnit = unit;
			EndOfAnimationListener tmpListener = endOfAnimationListener;
			endOfAnimationListener = null;
			unit = null;
			sourceTile = null;
			
			if (tmpListener != null) {
				tmpListener.end(tmpUnit);
			}
			return false;
		} else {
			return true;
		}
	}

	public void addEndOfAnimationListener(EndOfAnimationListener endOfAnimationListener) {
		this.endOfAnimationListener = endOfAnimationListener;
	}

	public boolean isAnimatedUnitTile(int mapx, int mapy) {
		return step < 1 && unit != null && sourceTile != null && sourceTile.x == mapx && sourceTile.y == mapy;
	}

	public void drawUnit(ObjectsTileDrawer unitDrawer) {
		if (nextStep()) {
			unitDrawer.screenPoint.set(v);
			unitDrawer.drawUnit(unit);
		}
	}

}

