package promitech.measure;

public class MeasurementResult {
    private long min = -1;
    private long max = -1;
    private long avr = -1;
    
    public void add(long startTime, long endTime) {
        long timeout = endTime - startTime;
        
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
    
    public String toString() {
        return "min: " + min + ", max: " + max + ", avr: " + avr;
    }
}
