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
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.savegame.SaveGameList;
import promitech.colonization.savegame.SaveGameParser;

public class GameCreator {
	private final GUIGameModel guiGameModel;
	
	public GameCreator(GUIGameModel guiGameModel) {
		this.guiGameModel = guiGameModel;
	}

	public void initGameFromSavegame() throws IOException, ParserConfigurationException, SAXException {
		guiGameModel.game = SaveGameParser.loadGameFormClassPath("maps/savegame_1600.xml");
		guiGameModel.game.playingPlayer = guiGameModel.game.players.getById("player:1");
		guiGameModel.game.playingPlayer.eventsNotifications.setAddNotificationListener(guiGameModel);
        System.out.println("init game from classpath 1600.xml = " + guiGameModel.game);
		postCreateGame();
	}
	
	public void initNewGame() throws IOException, ParserConfigurationException, SAXException {
		SaveGameParser.loadDefaultSpecification();
		Specification.instance.updateOptionsFromDifficultyLevel("model.difficulty.medium");
		
		Game.idGenerator = new IdGenerator(0);
		guiGameModel.game = new Game();
		guiGameModel.game.initCibolaCityNamesForNewGame();
		
		guiGameModel.game.setSpecification(Specification.instance);
		guiGameModel.game.activeUnitId = null;
		
		guiGameModel.game.playingPlayer = Player.newStartingPlayer(Game.idGenerator, Specification.instance.nations.getById("model.nation.french"));
		guiGameModel.game.playingPlayer.setHuman();
		guiGameModel.game.players.add(guiGameModel.game.playingPlayer);
		
		for (Nation nation : Specification.instance.nations.entities()) {
			if (nation.nationType.isEuropean()) {
				if (!nation.nationType.isREF() && guiGameModel.game.playingPlayer.nation().notEqualsId(nation)) {
					System.out.println("create european player: " + nation +  " " + nation.nationType);
					guiGameModel.game.players.add(Player.newStartingPlayer(Game.idGenerator, nation));
				}
			} else {
				System.out.println("create native player: " + nation + " " + nation.nationType);
				guiGameModel.game.players.add(Player.newStartingPlayer(Game.idGenerator, nation));
			}
		}
		guiGameModel.game.map = new MapGenerator().generate(guiGameModel.game.players);

		postCreateGame();
	}
	
	public void loadLastGame() throws IOException, ParserConfigurationException, SAXException {
		SaveGameList saveGameList = new SaveGameList();
		
		guiGameModel.game = saveGameList.loadLast();
        if (guiGameModel.game != null) {
        	guiGameModel.game.playingPlayer.eventsNotifications.setAddNotificationListener(guiGameModel);
        	postCreateGame();
        } else {
        	initNewGame();
        }
	}
	
	public void quickSaveGame() {
		new SaveGameList().saveAsQuick(guiGameModel.game);
	}
	
	private void postCreateGame() {
		guiGameModel.unitIterator = new UnitIterator(guiGameModel.game.playingPlayer, new Unit.ActivePredicate());
		
		for (Player player : guiGameModel.game.players.entities()) {
			player.fogOfWar.resetFogOfWar(player);
			
			if (player.isIndian()) {
				for (Unit indianUnit : player.units.entities()) {
					indianUnit.addIndianUnitToSettlement();
				}
			}
		}
	}
	
	
}
