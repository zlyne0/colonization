package promitech.map;

public class FloatFloatArray extends AbstractArray2D {

    protected final float[][] array;
    private final float unknownValue;

    public FloatFloatArray(final int width, final int height) {
        super(width, height);
        array = new float[height][width];
        unknownValue = 0;
    }

    public FloatFloatArray(final int width, final int height, final float unknownValue) {
        super(width, height);
        array = new float[height][width];
        this.unknownValue = unknownValue;
        set(unknownValue);
    }

    public void resetToUnknown() {
        set(unknownValue);
    }

    public float get(final int x, final int y) {
        return array[y][x];
    }

    public void set(final int x, final int y, final float value) {
        array[y][x] = value;
    }

    public FloatFloatArray set(final float value) {
        float[] row;
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

    public float getUnknownValue() {
        return unknownValue;
    }

    public void addValue(final int x, final int y, final float value) {
        float v = get(x, y);
        if (v == unknownValue) {
            v = value;
        } else {
            v = v + value;
        }
        set(x, y, v);
    }

}
