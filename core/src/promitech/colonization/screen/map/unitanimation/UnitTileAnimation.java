package promitech.colonization.screen.map.unitanimation;

import net.sf.freecol.common.model.Unit;
import promitech.colonization.screen.map.MapRenderer;
import promitech.colonization.screen.map.ObjectsTileDrawer;

public interface UnitTileAnimation {

	public static final UnitTileAnimation NoAnimation = new UnitTileAnimation() {
		@Override
		public void initMapPos(MapRenderer mapRenderer) {
		}
		@Override
		public Unit getUnit() {
			return null;
		}

		@Override
		public void addEndListener(Runnable listener) {
		}

		@Override
		public void drawUnit(ObjectsTileDrawer drawer) {
		}
	};
	
    void initMapPos(MapRenderer mapRenderer);    
    
    void drawUnit(ObjectsTileDrawer drawer);
    
    Unit getUnit();

	void addEndListener(Runnable listener);
}
