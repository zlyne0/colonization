package promitech.colonization.ui;

import java.util.LinkedList;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import promitech.colonization.GameResources;

public class ClosableDialog<T extends ClosableDialog<?>> {
    private static final long CLOSE_AFTER_CREATE_TIMEOUT = 1000;

    protected final Dialog dialog;
    private final long createTime;
    private LinkedList<EventListener> onCloseListeners = new LinkedList<EventListener>();
    
    private ClosableDialog childDialog = null;
    private ClosableDialogSize prefWidth;
    private ClosableDialogSize prefHeight;

    public ClosableDialog() {
    	this("", GameResources.instance.getUiSkin(), 
			ClosableDialogSize.def(), 
			ClosableDialogSize.def()
		);
    }
    
    public ClosableDialog(final ClosableDialogSize prefWidth, final ClosableDialogSize prefHeight) {
    	this("", GameResources.instance.getUiSkin(), prefWidth, prefHeight);
    }
    
    public ClosableDialog(String title, Skin skin, final ClosableDialogSize prefWidth, final ClosableDialogSize prefHeight) {
    	this.prefWidth = prefWidth;
    	this.prefHeight = prefHeight;
    	
    	createTime = System.currentTimeMillis();    	
        dialog = new Dialog(title, skin) {
        	@Override
        	public float getPrefHeight() {
        		if (prefHeight == null) {
        			return super.getPrefHeight();
        		}
        		return prefHeight.get();
        	}
        	
        	@Override
        	public float getPrefWidth() {
        		if (prefWidth == null) {
        			return super.getPrefWidth();
        		}
        		return prefWidth.get();
        	}
        };
        
        buttonTableLayoutExtendX()
	    	.bottom();
		dialog.getButtonTable().defaults().pad(0, 10, 5, 10).fillX().expandX().bottom();
    }
    
    public ClosableDialog(String title, Skin skin) {
    	createTime = System.currentTimeMillis();
    	dialog = new Dialog(title, skin);
    	
        buttonTableLayoutExtendX()
        	.bottom();
    	dialog.getButtonTable().defaults().pad(0, 10, 5, 10).fillX().expandX().bottom();
    }
    
	public void init(ShapeRenderer shapeRenderer) {
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
    	dialog.getStage().removeListener(onStageClickListener);
        dialog.hide();
        executeCloseListener();
    }
    
    public void hideWithoutFade() {
    	dialog.getStage().removeListener(onStageClickListener);
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
    
    private final ClickListener onStageClickListener = new ClickListener() {
    	public void clicked(InputEvent event, float x, float y) {
    		if (childDialog == null && canCloseBecauseClickOutsideDialog(x, y)) {
    			hide();
    		}
    	}
    };
    
    public void show(Stage stage) {
    	if (prefWidth != null) {
    		prefWidth.init(stage);
    	}
    	if (prefHeight != null) {
    		prefHeight.init(stage);
    	}
		stage.addListener(onStageClickListener);
        dialog.show(stage);
    }
    
    public void resetPositionToCenter() {
        dialog.setPosition(
            Math.round((dialog.getStage().getWidth() - dialog.getWidth()) / 2), 
            Math.round((dialog.getStage().getHeight() - dialog.getHeight()) / 2)
        );        
    }
    
	protected void showDialog(SimpleMessageDialog confirmationDialog) {
		confirmationDialog.show(dialog.getStage());
	}

    public void pack() {
    	dialog.pack();
    }
    
    @SuppressWarnings("unchecked")
	public T addOnCloseListener(EventListener onCloseListener) {
    	onCloseListeners.add(onCloseListener);
    	return (T)this;
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
    
	protected Cell<Table> buttonTableLayoutExtendX() {
		return dialog.getCell(dialog.getButtonTable()).expandX().fillX();
	}
    
}
