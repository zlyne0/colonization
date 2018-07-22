package promitech.colonization.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import promitech.colonization.GameResources;

public class ClosableDialog<T extends ClosableDialog<?>> extends ModalDialog<T> {
    private static final long CLOSE_AFTER_CREATE_TIMEOUT = 1000;

    private final long createTime;

    private final ClickListener onStageClickListener = new ClickListener() {
    	public void clicked(InputEvent event, float x, float y) {
    		if (childDialog == null && canCloseBecauseClickOutsideDialog(x, y)) {
    			hide();
    		}
    	}
    };
    
    public ClosableDialog() {
		this("", GameResources.instance.getUiSkin(), ModalDialogSize.def(), ModalDialogSize.def());
    }
    
    public ClosableDialog(final String title) {
		this(title, GameResources.instance.getUiSkin(), ModalDialogSize.def(), ModalDialogSize.def());
    }
    
    public ClosableDialog(final ModalDialogSize prefWidth, final ModalDialogSize prefHeight) {
		this("", GameResources.instance.getUiSkin(), prefWidth, prefHeight);
    }
    
    public ClosableDialog(String title, Skin skin, final ModalDialogSize prefWidth, final ModalDialogSize prefHeight) {
    	super(title, skin, prefWidth, prefHeight);
    	
    	createTime = System.currentTimeMillis();    	
    	addOnCloseListener(new EventListener() {
    		@Override
    		public boolean handle(Event event) {
    			dialog.getStage().removeListener(onStageClickListener);
    			return true;
    		}
    	});
    }
    
    public void show(Stage stage) {
		stage.addListener(onStageClickListener);
        super.show(stage);
    }
    
    private boolean canCloseBecauseClickOutsideDialog(float x, float y) {
        if (!containsCords(dialog, x, y)) {
            if (System.currentTimeMillis() - createTime > CLOSE_AFTER_CREATE_TIMEOUT) {
                return true;
            }
        }
        return false;
    }
    
    private boolean containsCords(Actor actor, float x, float y) {
        return 
            actor.getX() <= x && 
            actor.getY() <= y && 
            x <= actor.getX() + actor.getWidth() && 
            y <= actor.getY() + actor.getHeight();
    }
}
