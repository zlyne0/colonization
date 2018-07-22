package promitech.colonization.ui;

import java.util.LinkedList;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import promitech.colonization.GameResources;

public class ModalDialog<T extends ModalDialog<?>> {

	protected final Skin skin;
    protected final Dialog dialog;
    protected ModalDialog<?> childDialog = null;
    protected final ModalDialogSize prefWidth;
    protected final ModalDialogSize prefHeight;

    private LinkedList<EventListener> onCloseListeners = new LinkedList<EventListener>();

    public ModalDialog() {
        this("", GameResources.instance.getUiSkin(),
            ModalDialogSize.def(),
            ModalDialogSize.def()
        );
    }

    public ModalDialog(final ModalDialogSize prefWidth, final ModalDialogSize prefHeight) {
        this("", GameResources.instance.getUiSkin(), prefWidth, prefHeight);
    }

    public ModalDialog(String title, Skin skin, final ModalDialogSize prefWidth, final ModalDialogSize prefHeight) {
    	this.skin = skin;
        this.prefWidth = prefWidth;
        this.prefHeight = prefHeight;

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

    public void init(ShapeRenderer shapeRenderer) {
    }

    public ModalDialog<?> withHidingOnEsc() {
        dialog.addListener(new InputListener() {
            public boolean keyDown (InputEvent event, int keycode2) {
            if (Input.Keys.ESCAPE == keycode2) {
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
        executeCloseListener();
        dialog.hide();
    }

    public void hideWithoutFade() {
        executeCloseListener();
        dialog.hide(null);
    }

    public void show(Stage stage) {
        if (prefWidth != null) {
            prefWidth.init(stage);
        }
        if (prefHeight != null) {
            prefHeight.init(stage);
        }
        dialog.show(stage);
    }

    public void resetPositionToCenter() {
        dialog.setPosition(
            Math.round((dialog.getStage().getWidth() - dialog.getWidth()) / 2),
            Math.round((dialog.getStage().getHeight() - dialog.getHeight()) / 2)
        );
    }

	protected void showDialog(SimpleMessageDialog dialogToShow) {
    	this.childDialog = dialogToShow;
    	this.childDialog.addOnCloseListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				ModalDialog.this.childDialog = null;
				return true;
			}
		});
    	dialogToShow.show(dialog.getStage());
	}
    
    public void pack() {
        dialog.pack();
    }

    @SuppressWarnings("unchecked")
    public T addOnCloseListener(EventListener onCloseListener) {
        onCloseListeners.add(onCloseListener);
        return (T)this;
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
