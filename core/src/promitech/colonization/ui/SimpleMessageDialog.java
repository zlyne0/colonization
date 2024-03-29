package promitech.colonization.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import promitech.colonization.ui.resources.Messages;
import promitech.colonization.ui.resources.StringTemplate;

public class SimpleMessageDialog extends ClosableDialog<SimpleMessageDialog> {

	public abstract static class ButtonActionListener extends ChangeListener {
		private SimpleMessageDialog dialog;
		
		@Override
		public void changed(ChangeEvent event, Actor actor) {
			buttonPressed(dialog);
		}
		public abstract void buttonPressed(SimpleMessageDialog dialog);
	}
	
	private ChangeListener hideChangeListener = new ChangeListener() {
		@Override
		public void changed(ChangeEvent event, Actor actor) {
			hide();
		}
	};

	public SimpleMessageDialog withContent(StringTemplate st) {
		return withContentMsg(Messages.message(st));
	}
	
	public SimpleMessageDialog withContent(String msgKey) {
		return withContentMsg(Messages.msg(msgKey));
	}
	
	public SimpleMessageDialog withContentMsg(String msg) {
		Label contentLabel = new Label(msg, skin);
		contentLabel.setWrap(true);		
		getContentTable().add(contentLabel).fillX().pad(20).space(10).width(500).row();
		return this;
	}

	public SimpleMessageDialog withButton(String titleCode) {
		return withButton(titleCode, null);
	}
	
	public SimpleMessageDialog withButton(String titleCode, ButtonActionListener buttonActionListener) {
		String st = Messages.msg(titleCode);
		TextButton button = new TextButton(st, skin);
		button.align(Align.center);
		
		if (buttonActionListener != null) {
			buttonActionListener.dialog = this;
			button.addListener(buttonActionListener);
		} else {
			button.addListener(hideChangeListener);
		}
		getButtonTable().add(button);
		return this;
	}
}
