package promitech.colonization.orders.move;

import java.util.concurrent.Semaphore;

class MoveDrawerSemaphore implements Runnable {
    private final Semaphore animationSemaphore = new Semaphore(0);
    
    @Override
    public void run() {
        animationSemaphore.release(1);
    }
    
    public void waitForUnitAnimation() {
        try {
            animationSemaphore.acquire(1);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }
}

