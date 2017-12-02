package promitech.colonization.actors.map.unitanimation;

import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;

import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import promitech.colonization.actors.map.MapRenderer;
import promitech.colonization.actors.map.ObjectsTileDrawer;

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
			if (animationEndListener != null) {
				animationEndListener.run();
				animationEndListener = null;
			}
		}
	};
	
	private UnitTileAnimation actualAnimationPart = UnitTileAnimation.NoAnimation;
	private Runnable animationEndListener;
	
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
		this.animationEndListener = endOfAnimation;
		
		addAction(moveForward);
		addAction(retreat);

		moveForward.init(unit, sourceTile, destTile, endMoveForwardActionListener);
		retreat.init(unit, destTile, sourceTile, endRetreatActionListener);
		
		actualAnimationPart = moveForward;
	}
}
