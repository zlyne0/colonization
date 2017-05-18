package promitech.map;

public class Int2dArray extends AbstractArray2D {

    protected final int[] array;
    
    public Int2dArray(final int width, final int height) {
        super(width, height);
        
        array = new int[width * height];
    }

    public int get(final int x, final int y) {
        return array[toIndex(x, y)];
    }
    
    public int get(final int cellIndex) {
        return array[cellIndex];
    }
    
    public void set(final int x, final int y, final int value) {
        array[toIndex(x, y)] = value;
    }

    public void set(final int cellIndex, final int value) {
        array[cellIndex] = value;
    }    
    
    public Int2dArray set(final int value) {
        for (int index = 0, length = array.length; index < length; index++) {
            array[index] = value;
        }
        return this;
    }
    
    public void set(Int2dArray src) {
    	if (src.size() != size()) {
    		throw new IllegalArgumentException("different source array size. Actual " + size() + ", source " + src.size());
    	}
    	for (int i=0; i<array.length; i++) {
    		array[i] = src.array[i];
    	}
    }
    
    public int size() {
    	return array.length;
    }
}
