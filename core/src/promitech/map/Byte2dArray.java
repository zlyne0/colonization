package promitech.map;

public class Byte2dArray extends AbstractArray2D {

    protected final byte[] array;
    
    public Byte2dArray(final int width, final int height) {
        super(width, height);
        
        array = new byte[width * height];
    }

    public int cellLength() {
    	return array.length;
    }
    
    public byte get(final int x, final int y) {
        return array[toIndex(x, y)];
    }
    
    public byte get(final int cellIndex) {
        return array[cellIndex];
    }
    
    public void set(final int x, final int y, final byte value) {
        array[toIndex(x, y)] = value;
    }

    public void set(final int cellIndex, final byte value) {
        array[cellIndex] = value;
    }    

	public boolean setAndReturnDifference(int x, int y, byte value) {
	    int cellIndex = toIndex(x, y);
        boolean c = array[cellIndex] != value;
        array[cellIndex] = value;
        return c;
	}
    
    public Byte2dArray set(final byte value) {
        for (int index = 0, length = array.length; index < length; index++) {
            array[index] = value;
        }
        return this;
    }
}
