package promitech.colonization.actors.map.unitanimation;

import net.sf.freecol.common.model.Unit;
import promitech.colonization.actors.map.MapRenderer;
import promitech.colonization.actors.map.ObjectsTileDrawer;

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
		public void drawUnit(ObjectsTileDrawer drawer) {
		}
	};
	
    void initMapPos(MapRenderer mapRenderer);    
    
    void drawUnit(ObjectsTileDrawer drawer);
    
    Unit getUnit();
}
