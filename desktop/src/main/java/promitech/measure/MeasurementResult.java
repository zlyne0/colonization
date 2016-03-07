package promitech.measure;

public class MeasurementResult {
    private static final int NUMBER_OF_RANGES = 5;
    private long min = -1;
    private long max = -1;
    private long avr = -1;
    
    private final long timeouts[];
    private int timeoutsSize;
    
    public MeasurementResult(final int count) {
        this.timeouts = new long[count];
        this.timeoutsSize = 0;
    }

    public void add(long startTime, long endTime) {
        long timeout = endTime - startTime;
        
        timeouts[timeoutsSize] = timeout;
        timeoutsSize++;
        
        if (avr == -1) {
            avr = timeout;
            min = timeout;
            max = timeout;
        }
        
        if (timeout < min) {
            min = timeout;
        }
        if (timeout > max) {
            max = timeout;
        }
        avr = (avr + timeout) / 2;
    }
    
    public void xxx() {
        float range = ((max+1) - min) / (float)NUMBER_OF_RANGES;
        System.out.println("min max " + min + ", " + max + ", range: " + range);
        
        for (int i = 0; i < NUMBER_OF_RANGES; i++) {
            float start = min + i * range;
            float end = min + (i+1)*range;
            
            System.out.println("range [" + start + ", " + end + "> " + countElements(start, end));
        }
    }
    
    private int countElements(float start, float end) {
        int count = 0;
        for (int i=0; i<timeouts.length; i++) {
            if (start <= timeouts[i] && timeouts[i] < end) {
                count++;
            }
        }
        return count;
    }

    public String toString() {
        return "min: " + min + ", max: " + max + ", avr: " + avr;
    }
}
