package promitech.colonization.actors.map;

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;

import net.sf.freecol.common.model.Unit;

abstract class UnitTileAnimation extends TemporalAction {

    abstract void initMapPos(MapRenderer mapRenderer);    
    
    abstract void drawUnit(ObjectsTileDrawer drawer);
    
    abstract Unit getUnit();
}
