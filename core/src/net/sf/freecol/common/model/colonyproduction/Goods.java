package net.sf.freecol.common.model.colonyproduction;

import com.badlogic.gdx.utils.ObjectIntMap;
import com.badlogic.gdx.utils.ObjectIntMap.Entries;
import com.badlogic.gdx.utils.ObjectIntMap.Entry;

import net.sf.freecol.common.model.specification.GoodsType;

class Goods {
	private final ObjectIntMap<GoodsType> goods;

	Goods() {
		goods = new ObjectIntMap<GoodsType>();
	}
	
	Goods(int capacity) {
		goods = new ObjectIntMap<GoodsType>(capacity);
	}

	public int amount(GoodsType goodsType) {
		return goods.get(goodsType, 0);
	}
	
	public void add(Goods a) {
        for (Entry<GoodsType> aEntry : a.goods.entries()) {
            goods.getAndIncrement(aEntry.key, 0, aEntry.value);
        }
	}
	
	public void addAmount(GoodsType goodsType, int amount) {
		goods.getAndIncrement(goodsType, 0, amount);
	}
	
	public void set(GoodsType goodsType, int amount) {
		goods.put(goodsType, amount);
	}

	public void set(Goods a) {
		goods.clear();
		goods.putAll(a.goods);
	}
	
	public Entries<GoodsType> entries() {
		return goods.entries();
	}

	public void printByLine() {
		for (Entry<GoodsType> entry : goods) {
			System.out.println(entry.key.getId() + " " + entry.value);
		}
	}

}