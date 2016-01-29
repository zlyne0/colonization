package promitech.colonization.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import promitech.colonization.GameResources;

public class MyDialog {
    private static final long CLOSE_AFTER_CREATE_TIMEOUT = 1000;

    private Dialog dialog;
    private final long createTime;
    private EventListener closeListener;
    
    public MyDialog() {
        this("", GameResources.instance.getUiSkin());
    }
    
    public MyDialog(String title, Skin skin) {
        dialog = new Dialog(title, skin);
        createTime = System.currentTimeMillis();
    }

    public Table getContentTable() { 
        return dialog.getContentTable();
    }
    
    public Table getButtonTable() {
        return dialog.getButtonTable();
    }
    
    public void hide() {
        hideWithFade();
    }
    
    public void hideWithFade() {
        dialog.hide();
        executeCloseListener();
    }
    
    public void hideWithoutFade() {
        dialog.hide(null);
        executeCloseListener();
    }

    private ClickListener stageClickListener = new ClickListener() {
        public void clicked(InputEvent event, float x, float y) {
            if (canCloseBecauseClickOutsideDialog(x, y)) {
                hide();
            }
        }
    };
    
    public void addChildDialog() {
        // TODO: addToParentDialog or child
    }
    
    public void show(Stage stage) {
        stage.addListener(stageClickListener);
        dialog.show(stage);
    }
    
    public void resetPosition() {
        dialog.setPosition(
            Math.round((dialog.getStage().getWidth() - dialog.getWidth()) / 2), 
            Math.round((dialog.getStage().getHeight() - dialog.getHeight()) / 2)
        );        
    }
    
/*    
    public Stage getStage() {
        return dialog.getStage();
    }
    
    public void addListener(EventListener listener) {
        dialog.addListener(listener);
    }
    
    

    public void addCloseListener(EventListener closeListener) {
        this.closeListener = closeListener;
    }
 */       
    
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
    
    private void executeCloseListener() {
        if (closeListener != null) {
            closeListener.handle(null);
        }
    }
}
