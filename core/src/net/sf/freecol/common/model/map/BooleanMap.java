package net.sf.freecol.common.model.map;

import net.sf.freecol.common.model.Map;

public class BooleanMap {
	private boolean tab[][];
	private int width;
	private int height;
	
	public BooleanMap(Map map, boolean defaultVal) {
		this.width = map.width;
		this.height = map.height;
		
		tab = new boolean[height][width];
		reset(defaultVal);
	}
	
	public boolean isSet(int x, int y) {
		if (tab == null) {
			return false;
		}
		if (isCordsValid(x, y)) {
			return tab[y][x];
		}
		return false;
	}

	public boolean isCordsValid(int x, int y) {
		return x >= 0 && x < width && y >= 0 && y < height;
	}
	
	/**
	 * @return return true when value change
	 */
	public boolean set(int x, int y, boolean value) {
		if (isCordsValid(x, y)) {
			boolean c = tab[y][x] ^ value;
			tab[y][x] = value;
			return c;
		}
		return false;
	}

	public void reset(boolean defaultVal) {
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				tab[y][x] = defaultVal;
			}
		}
	}
}