package promitech.colonization.actors.map.unitanimation;

import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;

import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import promitech.colonization.actors.map.MapRenderer;
import promitech.colonization.actors.map.ObjectsTileDrawer;

class SuccessAttackAnimation extends SequenceAction implements UnitTileAnimation {

	private final UnitDislocationAnimation moveForward = new UnitDislocationAnimation();
	private final UnitDislocationAnimation moveBack = new UnitDislocationAnimation();
	private final UnitDisappearAnimation enemyDisappear = new UnitDisappearAnimation();
	private final Runnable endOfMoveForwardAnimation = new Runnable() {
		@Override
		public void run() {
			actualAnimationPart = moveBack;
		}
	};
	private final Runnable endOfMoveBackAnimation = new Runnable() {
		@Override
		public void run() {
			actualAnimationPart = enemyDisappear;
		}
	};
	private final Runnable endOfDisappearAnimation = new Runnable() {
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
	
	@Override
	public void initMapPos(MapRenderer mapRenderer) {
		moveForward.initMapPos(mapRenderer);
		moveBack.initMapPos(mapRenderer);
		enemyDisappear.initMapPos(mapRenderer);
	}

	@Override
	public void drawUnit(ObjectsTileDrawer drawer) {
		actualAnimationPart.drawUnit(drawer);
	}

	@Override
	public Unit getUnit() {
		return actualAnimationPart.getUnit();
	}
	
	public void init(Unit unit, Tile sourceTile, Tile destTile, Unit enemy, Runnable animationEndListener) {
		this.animationEndListener = animationEndListener;
		
		addAction(moveForward);
		addAction(moveBack);
		addAction(enemyDisappear);
		
		moveForward.init(unit, sourceTile, destTile, endOfMoveForwardAnimation);
		moveBack.init(unit, destTile, sourceTile, endOfMoveBackAnimation);
		enemyDisappear.init(enemy, destTile, endOfDisappearAnimation);
        actualAnimationPart = moveForward;
	}

}
