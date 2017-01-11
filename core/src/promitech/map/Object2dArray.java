package promitech.map;

public class Object2dArray<T> extends AbstractArray2D {

    protected final T[] array;

    @SuppressWarnings("unchecked")
    public Object2dArray(final int width, final int height) {
        super(width, height);
        array = (T[]) (new Object[width * height]);
    }
    
    public int getMaxCellIndex() {
        return array.length;
    }

    public T get(final int x, final int y) {
        return array[toIndex(x, y)];
    }

    public T get(final int cellIndex) {
        return array[cellIndex];
    }

    public void set(final int x, final int y, final T value) {
        array[toIndex(x, y)] = value;
    }

    public void set(final int cellIndex, final T value) {
        array[cellIndex] = value;
    }

    public Object2dArray<T> set(final T value) {
        for (int index = 0; index < array.length; index++) {
            array[index] = value;
        }
        return this;
    }
}
