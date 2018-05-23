package promitech.colonization.screen.map.hud;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import net.sf.freecol.common.model.player.MessageNotification;
import net.sf.freecol.common.model.player.Notification;
import promitech.colonization.GameResources;
import promitech.colonization.ui.ClosableDialog;
import promitech.colonization.ui.resources.Messages;

public class NotificationDialog extends ClosableDialog {

	private final Table dialogLayout = new Table();
	
	public NotificationDialog(Notification notification) {
		super(Messages.msg("message"), GameResources.instance.getUiSkin());

		String text;
		if (notification instanceof MessageNotification) {
			MessageNotification msgNotify = (MessageNotification)notification;
			text = msgNotify.getBody();
		} else {
			text = "can not recognize Notification: " + notification;
		}
		Label label = new Label(text, GameResources.instance.getUiSkin());
		dialogLayout.add(label).pad(20);
		
		getContentTable().add(dialogLayout).top().expand();
		buttonTableLayoutExtendX();
		dialog.getButtonTable().add(buttonsPanel()).expandX().fillX().padTop(20);
		
		withHidingOnEsc();
	}

	private Actor buttonsPanel() {
		TextButton okButton = new TextButton(Messages.msg("ok"), GameResources.instance.getUiSkin());
		okButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				NotificationDialog.this.hide();
			}
		});
		return okButton;
	}

	
	
}
