package promitech.colonization.ai;

import java.util.concurrent.Semaphore;

import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;

import promitech.colonization.GUIGameController;
import promitech.colonization.gamelogic.MoveContext;

public class AIMoveDrawer extends RunnableAction {
    private final GUIGameController guiGameController;
    private final Semaphore animationSemaphore = new Semaphore(0);
    
    public AIMoveDrawer(GUIGameController guiGameController) {
        this.guiGameController = guiGameController;
    }
    
    @Override
    public void run() {
        animationSemaphore.release(1);
    }
    
    public void startAIUnitDislocationAnimation(MoveContext moveContext) {
        if (guiGameController.showAIMoveOnPlayerScreen(moveContext)) {
            guiGameController.guiAIMoveInteraction(moveContext);
            
            try {
                animationSemaphore.acquire(1);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
