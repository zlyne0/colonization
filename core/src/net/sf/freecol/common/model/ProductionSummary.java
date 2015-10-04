package net.sf.freecol.common.model;

import com.badlogic.gdx.utils.ObjectIntMap;
import com.badlogic.gdx.utils.ObjectIntMap.Entries;
import com.badlogic.gdx.utils.ObjectIntMap.Entry;

public class ProductionSummary {
	private ObjectIntMap<String> goods = new ObjectIntMap<String>();

	public boolean isEmpty() {
		return goods.size == 0;
	}

	public boolean isSingleGoods() {
		return goods.size == 1;
	}

	public int size() {
		return goods.size;
	}
	
	public Entry<String> singleEntry() {
		return goods.entries().next();
	}

	public void addGoods(String goodsId, int goodQuantity) {
		goods.getAndIncrement(goodsId, 0, goodQuantity);
	}

	public Entries<String> entries() {
		return goods.entries();
	}
	
	public void applyTileImprovementsModifiers(Tile tile) {
		for (Entry<String> entry : goods.entries()) {
			int quantity = entry.value;
			for (TileImprovement ti : tile.getTileImprovements()) {
				quantity = (int)ti.type.applyModifier(entry.key, quantity);
			}
			for (TileResource tileResource : tile.getTileResources()) {
				quantity = (int)tileResource.getResourceType().applyModifier(entry.key, quantity);
			}
			goods.put(entry.key, quantity);
		}
	}
	
	public String toString() {
		String st = "productionSummary [";
		for (Entry<String> entry : goods.entries()) {
			st += "[" + entry.key + ", " + entry.value + "], ";
		}
		st += "]";
		return st;
	}

}
