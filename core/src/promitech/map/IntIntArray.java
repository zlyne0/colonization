package promitech.map;

public class IntIntArray extends AbstractArray2D {

    protected final int[][] array;

    public IntIntArray(final int width, final int height) {
        super(width, height);
        array = new int[height][width];
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
}
