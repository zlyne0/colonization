package net.sf.freecol.common.model.colonyproduction;


import com.badlogic.gdx.utils.ObjectIntMap.Entry;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.ProductionSummary;
import net.sf.freecol.common.model.specification.GoodsType;

class Warehouse {
	private final ProductionSummary goods = new ProductionSummary();
	private int capacity = 1;
	
	public void reset(Colony colony, int warehouseCapacity) {
		capacity = warehouseCapacity;
		goods.clear();
		
		for (Entry<String> entry : colony.getGoodsContainer().entries()) {
			goods.put(entry.key, entry.value);
		}
	}

	public void reset(int colonyWarehouseCapacity) {
		capacity = colonyWarehouseCapacity;
		goods.clear();
	}

	public int amount(String goodsTypeId) {
		return goods.getQuantity(goodsTypeId);
	}

	public int amount(GoodsType goodsType) {
		return goods.getQuantity(goodsType.getId());
	}

	public int capacity() {
		return capacity;
	}

	public boolean hasGoodsQuantity(GoodsType type, int amount) {
		return goods.getQuantity(type.getId()) >= amount;
	}

	public int maxGoodsAmountToFillWarehouseCapacity(GoodsType goodsType, int goodsAmount) {
		int freeSpace = capacity - goods.getQuantity(goodsType);
		if (freeSpace < 0) {
			freeSpace = 0;
		}
		if (freeSpace < goodsAmount) {
			return freeSpace;
		}
		return goodsAmount;
	}
}
