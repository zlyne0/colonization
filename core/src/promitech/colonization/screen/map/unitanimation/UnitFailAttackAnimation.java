package promitech.colonization.screen.map.unitanimation;

import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.utils.Array;

import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import promitech.colonization.screen.map.MapRenderer;
import promitech.colonization.screen.map.ObjectsTileDrawer;

class UnitFailAttackAnimation extends SequenceAction implements UnitTileAnimation {

	private final UnitDislocationAnimation attack = new UnitDislocationAnimation();
	private final UnitDisappearAnimation disappear = new UnitDisappearAnimation();
	private final Runnable endOfAttackAnimation = new Runnable() {
		@Override
		public void run() {
			actualAnimationPart = disappear;
		}
	};
	private final Runnable endOfDisappearAnimation = new Runnable() {
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
	
	@Override
	public void initMapPos(MapRenderer mapRenderer) {
		attack.initMapPos(mapRenderer);
		disappear.initMapPos(mapRenderer);
	}

	@Override
	public void drawUnit(ObjectsTileDrawer drawer) {
		actualAnimationPart.drawUnit(drawer);
	}

	@Override
	public Unit getUnit() {
		return disappear.getUnit();
	}
	
	public void init(Unit unit, Tile sourceTile, Tile destTile, Runnable animationEndListener) {
		animationEndListeners.clear();
		animationEndListeners.add(animationEndListener);
		
		addAction(attack);
		addAction(disappear);
		
		attack.init(unit, sourceTile, destTile, endOfAttackAnimation);
		disappear.init(unit, destTile, endOfDisappearAnimation);
        actualAnimationPart = attack;
	}

	@Override
	public void addEndListener(Runnable listener) {
		this.animationEndListeners.add(listener);
	}
}
