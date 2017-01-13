package promitech.map;

import net.sf.freecol.common.model.Map;

public class Boolean2dArray extends AbstractArray2D {
	private final boolean array[];
	
	public Boolean2dArray(Map map, boolean defaultVal) {
	    super(map.width, map.height);
		array = new boolean[width * height];
		reset(defaultVal);
	}
	
	public boolean get(int x, int y) {
	    return array[toIndex(x, y)];
	}

    public boolean get(int cellIndex) {
        return array[cellIndex];
    }
	
	/**
	 * @return return true when value change
	 */
	public boolean setAndReturnDifference(int x, int y, boolean value) {
	    int cellIndex = toIndex(x, y);
        boolean c = array[cellIndex] ^ value;
        array[cellIndex] = value;
        return c;
	}

    public boolean setAndReturnDifference(int cellIndex, boolean value) {
        boolean c = array[cellIndex] ^ value;
        array[cellIndex] = value;
        return c;
    }
	
    public void set(int x, int y, boolean value) {
        int cellIndex = toIndex(x, y);
        array[cellIndex] = value;
    }

    public void set(int cellIndex, boolean value) {
        array[cellIndex] = value;
    }
    
	public void reset(boolean value) {
	    for (int i=0; i<array.length; i++) {
	        array[i] = value;
	    }
	}
}