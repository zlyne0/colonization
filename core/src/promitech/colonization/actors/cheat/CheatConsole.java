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
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitIterator;
import net.sf.freecol.common.model.map.generator.MapGenerator;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.GUIGameController;
import promitech.colonization.GUIGameModel;
import promitech.colonization.GameCreator;
import promitech.colonization.GameResources;
import promitech.colonization.ui.ClosableDialog;

public class CheatConsole extends ClosableDialog<CheatConsole> {

	private final Table dialogLayout = new Table();
	private final Label label;
	private final TextField textField;
	private final ScrollPane scrollPane;
	private final GUIGameController gameController;
	private final GUIGameModel guiGameModel;
	private Tile selectedTile;
	
	public CheatConsole(GUIGameController gameControler, GUIGameModel guiGameModel) {
		super("", GameResources.instance.getUiSkin());
		this.gameController = gameControler;
		this.guiGameModel = guiGameModel;
		
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
			guiGameModel.game.playingPlayer.explorAllTiles();
			guiGameModel.game.playingPlayer.fogOfWar.removeFogOfWar();
			gameController.resetMapModel();
			hideWithFade();
		}
		if (cmd.equals("map generate")) {
			guiGameModel.game.map = new MapGenerator().generate(guiGameModel.game.players);
			gameController.resetMapModel();
			hideWithFade();
		}
		if (cmd.equals("m")) {
			guiGameModel.game.playingPlayer.explorAllTiles();
			guiGameModel.game.playingPlayer.fogOfWar.removeFogOfWar();
			gameController.resetMapModel();
			
			guiGameModel.game.map = new MapGenerator().generate(guiGameModel.game.players);
			gameController.resetMapModel();
			hideWithFade();
		}
		if (cmd.equals("map show owners") || cmd.equals("mso") ) {
			gameController.showTilesOwners();
			hideWithFade();
		}
		if (cmd.equals("map hide owners") || cmd.equals("mho")) {
			gameController.hideTilesOwners();
			hideWithFade();
		}
		if (cmd.equals("new game")) {
			try {
				new GameCreator(guiGameModel).initNewGame();
				gameController.resetMapModel();
				gameController.nextActiveUnit();
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
			hideWithFade();
		}
		if (cmd.equals("load game")) {
			try {
				new GameCreator(guiGameModel).initGameFromSavegame();
				gameController.resetMapModel();
				gameController.nextActiveUnit();
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
			hideWithFade();
		}
		
		if (cmd.equals("sp")) {
			guiGameModel.game.playingPlayer.setAi(true);
			
			Player newHumanPlayer = guiGameModel.game.players.getById("player:112");
			guiGameModel.game.setCurrentPlayer(newHumanPlayer);
			
			guiGameModel.unitIterator = new UnitIterator(guiGameModel.game.playingPlayer, new Unit.ActivePredicate());
			guiGameModel.game.playingPlayer.setAi(false);
			
			gameController.resetUnexploredBorders();
			gameController.resetMapModel();
			
			gameController.centerOnTile(guiGameModel.game.playingPlayer.getEntryLocationX(), guiGameModel.game.playingPlayer.getEntryLocationY());

			gameController.nextActiveUnit();
			
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
