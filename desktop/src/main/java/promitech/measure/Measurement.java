package promitech.measure;

public class Measurement {
    private final int count; 
    
    public Measurement(int count) {
        this.count = count;
    }
    
    public MeasurementResult measure(MeasureTask task) {
        MeasurementResult result = new MeasurementResult();
        long startTime;
        long endTime;
        for (int i = 0; i < count; i++) {
            startTime = System.currentTimeMillis();
            task.run();
            endTime = System.currentTimeMillis();
            result.add(startTime, endTime);
        }
        return result;
    }
}
