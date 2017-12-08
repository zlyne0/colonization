package promitech.colonization;

import java.util.concurrent.Semaphore;

import promitech.colonization.move.MoveContext;

class MoveDrawerSemaphore implements Runnable {
    private final MoveController moveController;
    private final Semaphore animationSemaphore = new Semaphore(0);
    
    public MoveDrawerSemaphore(MoveController moveController) {
        this.moveController = moveController;
    }
    
    @Override
    public void run() {
        animationSemaphore.release(1);
    }
    
    public void waitForUnitDislocationAnimation(MoveContext moveContext) {
    	moveController.startAnimateMove(moveContext);
        
        try {
            animationSemaphore.acquire(1);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }
}

