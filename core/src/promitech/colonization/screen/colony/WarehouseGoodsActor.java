package promitech.colonization.screen.colony;

import com.badlogic.gdx.graphics.Color;

import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.screen.ui.QuantityGoodActor;

public class WarehouseGoodsActor extends QuantityGoodActor {

	private boolean exported = false;
	
    public WarehouseGoodsActor(GoodsType goodsType, int quantity) {
		super(goodsType, quantity);
	}
    
	@Override
    public Color getQuantityColor() {
		if (exported) {
			return Color.GREEN;
		}
        if (quantity == 0) {
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

	public void setExported(boolean exported) {
		this.exported = exported;
	}
}
