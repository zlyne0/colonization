package promitech.colonization.screen.map.unitanimation;

import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.utils.Array;

import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import promitech.colonization.screen.map.MapRenderer;
import promitech.colonization.screen.map.ObjectsTileDrawer;

class UnitAttackRetreatAnimation extends SequenceAction implements UnitTileAnimation {
	private final UnitDislocationAnimation moveForward = new UnitDislocationAnimation();
	private final UnitDislocationAnimation retreat = new UnitDislocationAnimation();
	private final Runnable endMoveForwardActionListener = new Runnable() {
		@Override
		public void run() {
			actualAnimationPart = retreat;
		}
	};
	private final Runnable endRetreatActionListener = new Runnable() {
		@Override
		public void run() {
			actualAnimationPart = UnitTileAnimation.NoAnimation;
			for (Runnable animationEndListener : animationEndListeners) {
				animationEndListener.run();
			}
			animationEndListeners.clear();
		}
	};
	
	private UnitTileAnimation actualAnimationPart = UnitTileAnimation.NoAnimation;
	private final Array<Runnable> animationEndListeners = new Array<>(2);
	
	UnitAttackRetreatAnimation() {
	}
	
	@Override
	public void initMapPos(MapRenderer mapRenderer) {
		moveForward.initMapPos(mapRenderer);
		retreat.initMapPos(mapRenderer);
	}
	
	@Override
	public void drawUnit(ObjectsTileDrawer drawer) {
		actualAnimationPart.drawUnit(drawer);
	}
	
	@Override
	public Unit getUnit() {
		return retreat.getUnit();
	}

	public void init(Unit unit, Tile sourceTile, Tile destTile, Runnable endOfAnimation) {
		animationEndListeners.clear();
		animationEndListeners.add(endOfAnimation);

		addAction(moveForward);
		addAction(retreat);

		moveForward.init(unit, sourceTile, destTile, endMoveForwardActionListener);
		retreat.init(unit, destTile, sourceTile, endRetreatActionListener);
		
		actualAnimationPart = moveForward;
	}

	@Override
	public void addEndListener(Runnable listener) {
		this.animationEndListeners.add(listener);
	}
}
