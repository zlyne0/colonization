package promitech.colonization.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import promitech.colonization.ui.resources.Messages;
import promitech.colonization.ui.resources.StringTemplate;

public class SimpleMessageDialog extends Dialog implements CloseableDialog {

	private final ClosableDialogAdapter closeableDialogAdapter = new ClosableDialogAdapter();	
	
	private final Skin skin;
	private ChangeListener hideChangeListener = new ChangeListener() {
		@Override
		public void changed(ChangeEvent event, Actor actor) {
			hide();
		}
	};
	
	public SimpleMessageDialog(String title, Skin skin) {
		super(title, skin);
		this.skin = skin;
	}

	public SimpleMessageDialog withContant(StringTemplate st) {
		String msg = Messages.message(st);
		Label contentLabel = new Label(msg, skin);
		getContentTable().add(contentLabel);
		return this;
	}

	public SimpleMessageDialog withButton(String tileCode) {
		return withButton(tileCode, null);
	}
	
	public SimpleMessageDialog withButton(String tileCode, ChangeListener changeListener) {
		String st = Messages.msg(tileCode);
		TextButton button = new TextButton(st, skin);
		button.align(Align.center);
		
		if (changeListener != null) {
			button.addListener(changeListener);
		} else {
			button.addListener(hideChangeListener);
		}
		getButtonTable().add(button).center();
		return this;
	}

	@Override
	public void hide() {
		closeableDialogAdapter.executeCloseListener();	
		super.hide();
	}
	
	@Override
	public void clickOnDialogStage(InputEvent event, float x, float y) {
		if (closeableDialogAdapter.canCloseBecauseClickOutsideDialog(this, x, y)) {
			hide();
		}
	}

	@Override
	public void addOnCloseEvent(EventListener dialogOnCloseListener) {
		closeableDialogAdapter.addCloseListener(dialogOnCloseListener);
	}
	
}
