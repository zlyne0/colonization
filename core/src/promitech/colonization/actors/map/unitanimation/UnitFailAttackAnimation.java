package promitech.colonization.actors.map.unitanimation;

import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;

import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import promitech.colonization.actors.map.MapRenderer;
import promitech.colonization.actors.map.ObjectsTileDrawer;

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
		this.animationEndListener = animationEndListener;
		
		addAction(attack);
		addAction(disappear);
		
		attack.init(unit, sourceTile, destTile, endOfAttackAnimation);
		disappear.init(unit, destTile, endOfDisappearAnimation);
        actualAnimationPart = attack;
	}

}
