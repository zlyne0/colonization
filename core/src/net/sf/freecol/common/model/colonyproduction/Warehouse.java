package net.sf.freecol.common.model.colonyproduction;


import com.badlogic.gdx.utils.ObjectIntMap.Entry;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.ProductionSummary;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.specification.GoodsType;

class Warehouse {
	private final ProductionSummary goods = new ProductionSummary();
	private int capacity = 1;
	
	public void reset(Colony colony) {
		capacity = colony.warehouseCapacity();
		goods.clear();
		
		for (Entry<String> entry : colony.getGoodsContainer().entries()) {
			GoodsType goodsType = Specification.instance.goodsTypes.getById(entry.key);
			goods.put(goodsType.getId(), entry.value);
		}
	}

	public void reset(int colonyWarehouseCapacity) {
		capacity = colonyWarehouseCapacity;
		goods.clear();
	}

	public int amount(GoodsType goodsType) {
		return goods.getQuantity(goodsType.getId());
	}
	
	public int capacity() {
		return capacity;
	}

}
