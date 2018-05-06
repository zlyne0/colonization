package promitech.colonization;

import java.util.Random;

public class MockedRandomizer extends Random {
    
    private static final long serialVersionUID = 1L;
    
    private int floatIndex = 0;
    private float floats[];
    private int intIndex = 0;
    private int ints[];
    
    public MockedRandomizer withFloatsResults(float ... floats) {
        this.floats = floats;
        return this;
    }
    
    public MockedRandomizer withIntsResults(int ... ints) {
        this.ints = ints;
        return this;
    }
    
    @Override
    public float nextFloat() {
        if (floats == null) {
            return super.nextFloat();
        }
        return floats[floatIndex++];
    }

    @Override
    public int nextInt() {
        if (ints == null) {
            return super.nextInt();
        }
        return ints[intIndex++];
    }
}