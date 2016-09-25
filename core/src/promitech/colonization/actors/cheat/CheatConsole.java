package promitech.colonization.actors.cheat;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Align;

import promitech.colonization.GameResources;
import promitech.colonization.ui.ClosableDialog;

public class CheatConsole extends ClosableDialog {

	private final Table dialogLayout = new Table();
	private final Label label;
	private final TextField textField;
	private final ScrollPane scrollPane;
	
	public CheatConsole(float width, float height) {
		super("", GameResources.instance.getUiSkin());
		
		dialogLayout.setSize(width, height);
		
		label = new Label("", GameResources.instance.getUiSkin());
        label.setAlignment(Align.top | Align.left);
		
		scrollPane = new ScrollPane(label);
		scrollPane.setForceScroll(false, false);
		scrollPane.setFadeScrollBars(false);
		scrollPane.setOverscroll(true, true);
		scrollPane.setScrollBarPositions(false, true);
		
		textField = new TextField("", GameResources.instance.getUiSkin());
		
		dialogLayout.add(scrollPane).expand().fill().row();
		dialogLayout.add(textField).fillX().expandX();
		
		getContentTable().add(dialogLayout).width(width).height(height);
		
		dialog.addListener(new InputListener() {
			public boolean keyDown (InputEvent event, int keycode2) {
				if (Keys.ENTER == keycode2) {
					executeCommand();
				}
				return false;
			}
		});
		withHidingOnEsc();
	}
	
	protected void executeCommand() {
		String cmd = textField.getText();
		textField.setText("");
		addConsoleLine(cmd);
		
//		public void generateMonarchAction() {
//		    //MonarchLogic.handleMonarchAction(getGame(), game.playingPlayer, MonarchAction.HESSIAN_MERCENARIES);
//		    game.playingPlayer.modifyImmigration(150);
//		}

		
	}

	private void addConsoleLine(String line) {
		if (label.getText().toString().equals("")) {
			label.setText(line);			
		} else {
			label.setText(label.getText() + "\r\n" + line);
		}
		scrollPane.setScrollPercentY(100);
		scrollPane.layout();
	}
	
	@Override
	public void show(Stage stage) {
		super.show(stage);
		stage.setKeyboardFocus(textField);
	}

}
