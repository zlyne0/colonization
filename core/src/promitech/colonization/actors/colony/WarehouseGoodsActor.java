package promitech.colonization.actors.colony;

import com.badlogic.gdx.graphics.Color;

import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.actors.QuantityGoodActor;

public class WarehouseGoodsActor extends QuantityGoodActor {

    public WarehouseGoodsActor(GoodsType goodsType, int quantity) {
		super(goodsType, quantity);
	}

	@Override
    public Color getQuantityColor() {
        if (quantity ==  0) {
            return Color.GRAY;
        }
        if (getParent() instanceof WarehousePanel) {
            WarehousePanel warehousePanel = (WarehousePanel)getParent();
            if (quantity > warehousePanel.capacity()) {
                return Color.RED;
            }
        }
    	return super.getQuantityColor();
    }
}
