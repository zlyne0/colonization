package promitech.colonization;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.IdGenerator;
import net.sf.freecol.common.model.Nation;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitIterator;
import net.sf.freecol.common.model.map.generator.MapGenerator;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.savegame.SaveGameList;
import promitech.colonization.savegame.SaveGameParser;
import promitech.colonization.screen.map.hud.GUIGameModel;
import promitech.colonization.ui.resources.Messages;

public class GameCreator {
	private final GUIGameModel guiGameModel;
	private final PathFinder pathFinder;
	
	public GameCreator(PathFinder pathFinder, GUIGameModel guiGameModel) {
		this.guiGameModel = guiGameModel;
		this.pathFinder = pathFinder;
	}

	public void initGameFromSavegame() throws IOException, ParserConfigurationException, SAXException {
		guiGameModel.game = SaveGameParser.loadGameFormClassPath("maps/savegame_1600.xml");
        System.out.println("init game from classpath 1600.xml = " + guiGameModel.game);
		postCreateGame();
	}
	
	public void initNewGame(Nation playerNation, String playerName, String difficultyLevel) throws IOException, ParserConfigurationException, SAXException {
		SaveGameParser.loadDefaultSpecification();
		Specification.instance.updateOptionsFromDifficultyLevel(difficultyLevel);
		
		Game.idGenerator = new IdGenerator(0);
		guiGameModel.game = new Game();
		guiGameModel.game.initCibolaCityNamesForNewGame();
		
		guiGameModel.game.setSpecification(Specification.instance);
		guiGameModel.game.activeUnitId = null;
		
		guiGameModel.game.playingPlayer = Player.newStartingPlayer(Game.idGenerator, playerNation, playerName);
		guiGameModel.game.playingPlayer.setHuman();
		guiGameModel.game.players.add(guiGameModel.game.playingPlayer);
		
		for (Nation nation : Specification.instance.nations.entities()) {
			if (nation.nationType.isEuropean()) {
				if (!nation.nationType.isREF() && guiGameModel.game.playingPlayer.nation().notEqualsId(nation)) {
					System.out.println("create european player: " + nation +  " " + nation.nationType);
					
					guiGameModel.game.players.add(
						Player.newStartingPlayer(
							Game.idGenerator, 
							nation,
							Messages.msg(Messages.msg(nation.getId() + ".ruler"))
						)
					);
				}
			} else {
				System.out.println("create native player: " + nation + " " + nation.nationType);
				guiGameModel.game.players.add(Player.newStartingPlayer(
					Game.idGenerator, nation, Messages.msg(Messages.msg(nation.getId() + ".ruler"))
				));
			}
		}
		guiGameModel.game.map = new MapGenerator().generate(guiGameModel.game.players);

		postCreateGame();
	}
	
	public void loadLastGame() {
		SaveGameList saveGameList = new SaveGameList();
		guiGameModel.game = saveGameList.loadLast();
		postCreateGame();
	}
	
	public void load(String savename) {
		SaveGameList saveGameList = new SaveGameList();
		guiGameModel.game = saveGameList.loadGame(savename);
    	postCreateGame();
	}
	
	public void quickSaveGame() {
		new SaveGameList().saveAsQuick(guiGameModel.game);
	}
	
	private void postCreateGame() {
		pathFinder.reset();
		guiGameModel.game.playingPlayer.eventsNotifications.setAddNotificationListener(guiGameModel);
		guiGameModel.unitIterator = new UnitIterator(guiGameModel.game.playingPlayer, new Unit.ActivePredicate());
		
		for (Player player : guiGameModel.game.players) {
			player.getPlayerExploredTiles().resetFogOfWar(guiGameModel.game, player, guiGameModel.game.getTurn());
		}
	}
}
