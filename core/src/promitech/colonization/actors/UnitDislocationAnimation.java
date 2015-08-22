package promitech.colonization.actors;

import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;

import com.badlogic.gdx.math.Vector2;

public class UnitDislocationAnimation {
	private static final float STEP_DISTANCE = 0.07f; 
	
	public interface EndOfAnimation {
		public void end(Unit unit);
	}
	
	private EndOfAnimation endOfAnimation;
	private Unit unit;
	private final Vector2 source = new Vector2();
	private final Vector2 dest = new Vector2();
	private float step = 0;
	public final Vector2 v = new Vector2();
	
	public UnitDislocationAnimation() {
	}
	
	public void init(MapRenderer mapRenderer, Unit unit, Tile sourceTile, Tile descTile) {
		this.unit = unit;
		this.step = 0;
		v.set(0, 0);
		
		mapRenderer.mapToScreenCords(sourceTile.x, sourceTile.y, source);
		mapRenderer.mapToScreenCords(descTile.x, descTile.y, dest);
	}
	
	public boolean isUnitAnimated(Unit u) {
		return step < 1 && unit != null && this.unit.equalsId(u);
	}
	
	public boolean nextStep() {
		step += STEP_DISTANCE;
		v.set(dest).sub(source).scl(step);
		v.add(source);
		if (step >= 1) {
			if (endOfAnimation != null) {
				endOfAnimation.end(unit);
			}
			endOfAnimation = null;
			unit = null;
		}
		return step < 1;
	}

	public void addEndOfAnimation(EndOfAnimation endOfAnimation) {
		this.endOfAnimation = endOfAnimation;
	}
}

