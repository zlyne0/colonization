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

import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.map.generator.MapGenerator;
import promitech.colonization.GUIGameController;
import promitech.colonization.GameResources;
import promitech.colonization.ui.ClosableDialog;

public class CheatConsole extends ClosableDialog {

	private final Table dialogLayout = new Table();
	private final Label label;
	private final TextField textField;
	private final ScrollPane scrollPane;
	private final GUIGameController gameControler;
	private Tile selectedTile;
	
	public CheatConsole(GUIGameController gameControler) {
		super("", GameResources.instance.getUiSkin());
		this.gameControler = gameControler;
		
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
		getContentTable().add(dialogLayout);		
		
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

		if (cmd.equals("map show")) {
			gameControler.getGame().playingPlayer.getExploredTiles().reset(true);
			gameControler.getGame().playingPlayer.fogOfWar.removeFogOfWar();
			gameControler.resetMapModel();
			hideWithFade();
		}
		if (cmd.equals("map generate")) {
			gameControler.getGame().map = new MapGenerator().generate(gameControler.getGame().players);
			gameControler.resetMapModel();
			hideWithFade();
		}
		if (cmd.equals("m")) {
			gameControler.getGame().playingPlayer.getExploredTiles().reset(true);
			gameControler.getGame().playingPlayer.fogOfWar.removeFogOfWar();
			gameControler.resetMapModel();
			
			gameControler.getGame().map = new MapGenerator().generate(gameControler.getGame().players);
			gameControler.resetMapModel();
			hideWithFade();
		}
		if (cmd.equals("map show owners") || cmd.equals("mso") ) {
			gameControler.showTilesOwners();
			hideWithFade();
		}
		if (cmd.equals("map hide owners") || cmd.equals("mho")) {
			gameControler.hideTilesOwners();
			hideWithFade();
		}
		if (cmd.equals("new game")) {
			try {
				gameControler.initNewGame();
				gameControler.resetMapModel();
				gameControler.nextActiveUnit();
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
			hideWithFade();
		}
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
		getContentTable().getCell(dialogLayout)
			.width(stage.getWidth() * 0.75f)
			.height(stage.getHeight() * 0.75f);
		
		super.show(stage);
		stage.setKeyboardFocus(textField);
	}

	public void setSelectedTile(Tile selectedTile) {
		this.selectedTile = selectedTile;
	}

}
