package promitech.map;

public class IntIntArray extends AbstractArray2D {

    protected final int[][] array;
    private final int unknownValue;

    public IntIntArray(final int width, final int height) {
        super(width, height);
        array = new int[height][width];
        unknownValue = 0;
    }

    public IntIntArray(final int width, final int height, final int unknownValue) {
        super(width, height);
        array = new int[height][width];
        this.unknownValue = unknownValue;
        set(unknownValue);
    }

    public boolean isUnknownValue(int x, int y) {
        return get(x, y) == unknownValue;
    }

    public int get(final int x, final int y) {
        return array[y][x];
    }

    public void set(final int x, final int y, final int value) {
        array[y][x] = value;
    }

    public IntIntArray set(final int value) {
        int[] row;
        for (int y = 0; y < height; y++) {
            row = array[y];
            for (int x = 0; x < width; x++) {
                row[x] = value;
            }
        }
        return this;
    }

    public int size() {
        return width * height;
    }

    public int getUnknownValue() {
        return unknownValue;
    }

    public void addValue(final int x, final int y, final int value) {
        int v = get(x, y);
        if (v == unknownValue) {
            v = value;
        } else {
            v = v + value;
        }
        set(x, y, v);
    }
}
