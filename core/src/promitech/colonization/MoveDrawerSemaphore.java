package promitech.colonization;

import java.util.concurrent.Semaphore;

import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;

import promitech.colonization.gamelogic.MoveContext;

/**
 * Class extends RunnableAction because its use with libgdx {@link Actions.sequence} to catch moment when 
 * action is finish. 
 * If animation is necessary waitForUnitDislocationAnimation is blocking until animation will finished. 
 *  
 */
public class MoveDrawerSemaphore extends RunnableAction {
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

