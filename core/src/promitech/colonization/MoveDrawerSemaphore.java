package promitech.colonization;

import java.util.concurrent.Semaphore;

import promitech.colonization.gamelogic.MoveContext;

public class MoveDrawerSemaphore implements Runnable {
    private final MoveController moveController;
    private final Semaphore animationSemaphore = new Semaphore(0);
    
    private boolean drawing = false;

    public MoveDrawerSemaphore(MoveController moveController) {
        this.moveController = moveController;
    }
    
    @Override
    public void run() {
    	drawing = false;
        animationSemaphore.release(1);
    }
    
    public void waitForUnitDislocationAnimation(MoveContext moveContext) {
        if (moveController.showMoveOnPlayerScreen(moveContext)) {
        	drawing = true;
            moveController.startAnimateMove(moveContext);
            
            try {
                animationSemaphore.acquire(1);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
    }

	public boolean isDrawing() {
		return drawing;
	}
}

