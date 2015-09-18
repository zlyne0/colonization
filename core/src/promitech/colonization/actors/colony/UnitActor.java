package promitech.colonization.actors.colony;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

import net.sf.freecol.common.model.Unit;
import promitech.colonization.GameResources;

class UnitActor extends Image {
    final Unit unit;
    
    private static TextureRegion getTexture(Unit unit) {
        return GameResources.instance.getFrame(unit.resourceImageKey()).texture;
    }
    
    UnitActor(final Unit unit) {
        super(getTexture(unit));
        this.unit = unit;
    }
}
