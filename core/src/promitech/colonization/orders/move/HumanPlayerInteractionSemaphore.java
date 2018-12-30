package promitech.colonization.orders.move;

import java.util.concurrent.Semaphore;

public class HumanPlayerInteractionSemaphore implements Runnable {
    private final Semaphore semaphore = new Semaphore(0);
    
    public void release() {
        run();
    }
    
    @Override
    public void run() {
        semaphore.release(1);
    }
    
    public void waitForInteraction() {
        try {
            semaphore.acquire(1);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }
}

