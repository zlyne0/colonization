package promitech.colonization.screen.map;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import promitech.colonization.GameResources;
import promitech.colonization.screen.map.hud.GUIGameController;
import promitech.colonization.ui.ClosableDialog;
import promitech.colonization.ui.resources.Messages;

public class ColonyNameDialog extends ClosableDialog {

	private final Table dialogLayout = new Table();
	
	private TextField colonyNameTextField;
	private final Skin skin;
	private final GUIGameController guiGameController;
	
	public ColonyNameDialog(final GUIGameController guiGameController, float maxWidth, String initialColonyName) {
		super("", GameResources.instance.getUiSkin());
		this.skin = GameResources.instance.getUiSkin();
		this.guiGameController = guiGameController;

		dialogLayout.setWidth(maxWidth);
		getContentTable().add(dialogLayout).width(maxWidth).pad(20);
		
		colonyNameTextField = new TextField(initialColonyName, skin);
		
		dialogLayout.add(new Label(Messages.msg("nameColony.text"), skin)).spaceBottom(20).row();
		dialogLayout.add(colonyNameTextField).fillX().expandX();
		
		TextButton okButton = new TextButton(Messages.msg("nameColony.yes"), skin);
		TextButton cancelButton = new TextButton(Messages.msg("nameColony.no"), skin);

		getButtonTable().add(okButton);
		getButtonTable().add(cancelButton);
		
		okButton.addListener(new ChangeListener(){
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				approveColonyName();
			}
		});
		cancelButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				hideWithFade();
			}
		});
	}

	protected void approveColonyName() {
		if (colonyNameTextField.getText().trim().length() == 0) {
			return;
		}
		hideWithFade();
		guiGameController.buildColony(colonyNameTextField.getText().trim());
	}

}
