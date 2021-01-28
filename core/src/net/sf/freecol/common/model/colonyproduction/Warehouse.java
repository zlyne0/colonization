package net.sf.freecol.common.model.colonyproduction;


import com.badlogic.gdx.utils.ObjectIntMap.Entry;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.ProductionSummary;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.specification.GoodsType;

class Warehouse {
	private ProductionSummary goods;
	private int capacity;
	
	public Warehouse(Colony colony) {
		goods = new ProductionSummary();
		capacity = colony.warehouseCapacity();
		
		for (Entry<String> entry : colony.getGoodsContainer().entries()) {
			GoodsType goodsType = Specification.instance.goodsTypes.getById(entry.key);
			goods.put(goodsType.getId(), entry.value);
		}
	}

	public int amount(GoodsType goodsType) {
		return goods.getQuantity(goodsType.getId());
	}
	
	public int capacity() {
		return capacity;
	}
}
