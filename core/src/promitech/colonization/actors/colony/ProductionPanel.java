package promitech.colonization.actors.colony;

import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Tile;

public class ProductionPanel extends HorizontalGroup {

    public void init(Colony colony, Tile colonyTile) {
        colony.production();
    }

}
