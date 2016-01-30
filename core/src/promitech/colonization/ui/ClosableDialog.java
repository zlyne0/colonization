package promitech.colonization.ui;

import java.util.LinkedList;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class ClosableDialog {
    private static final long CLOSE_AFTER_CREATE_TIMEOUT = 1000;

    private final Dialog dialog;
    private final long createTime;
    private LinkedList<EventListener> onCloseListeners = new LinkedList<EventListener>();
    
    private ClosableDialog childDialog = null;
    
    public ClosableDialog(String title, Skin skin) {
    	createTime = System.currentTimeMillis();
    	dialog = new Dialog(title, skin);
    }
    
    public ClosableDialog(String title, Skin skin, final float maxHeight) {
    	createTime = System.currentTimeMillis();
    	
        dialog = new Dialog(title, skin) {
        	@Override
        	public float getMaxHeight() {
        		return maxHeight;
        	}
        	@Override
        	public float getPrefHeight() {
        		return maxHeight;
        	}
        };
    }

    public ClosableDialog withHidingOnEsc() {
    	dialog.addListener(new InputListener() {
            public boolean keyDown (InputEvent event, int keycode2) {
                if (Keys.ESCAPE == keycode2) {
                	hideWithFade();
                }
                return false;
            }
        });
    	return this;
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

    public void addChildDialog(ClosableDialog childDialog) {
    	this.childDialog = childDialog;
    	this.childDialog.addOnCloseListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				ClosableDialog.this.childDialog = null;
				return true;
			}
		});
    }
    
    public void show(Stage stage) {
        stage.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (childDialog == null && canCloseBecauseClickOutsideDialog(x, y)) {
                    hide();
                }
            }
        });
        dialog.show(stage);
    }
    
    public void resetPositionToCenter() {
        dialog.setPosition(
            Math.round((dialog.getStage().getWidth() - dialog.getWidth()) / 2), 
            Math.round((dialog.getStage().getHeight() - dialog.getHeight()) / 2)
        );        
    }
    
    public Stage getStage() {
        return dialog.getStage();
    }

    public void pack() {
    	dialog.pack();
    }
    
    public void addOnCloseListener(EventListener onCloseListener) {
    	onCloseListeners.add(onCloseListener);
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
    
    private void executeCloseListener() {
    	for (EventListener event : onCloseListeners) {
    		event.handle(null);
    	}
    }
}
