package promitech.colonization.savegame;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter.OutputType;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.SavedGame;
import promitech.colonization.ui.resources.Messages;

class SaveGameListData {
	protected List<SaveGameElement> saves = new ArrayList<SaveGameElement>();
	
	public void add(SaveGameElement ele) {
		saves.add(ele);
	}

	public SaveGameElement last() {
		if (saves.isEmpty()) {
			return null;
		}
		Collections.sort(saves, SaveGameElement.LAST_ON_FIRST);
		return saves.get(0);
	}
	
	public SaveGameElement findSaveByName(String name) {
		for (SaveGameElement save : saves) {
			if (save.name.equals(name)) {
				return save;
			}
		}
		return null;
	}
	
	public SaveGameElement findOrCreateSaveByName(String name) {
		SaveGameElement save = findSaveByName(name);
		if (save == null) {
			save = new SaveGameElement("" + System.currentTimeMillis() + ".xml", name);
			add(save);
		}
		return save;
	}
	
	public List<SaveGameElement> getSaves() {
		Collections.sort(saves, SaveGameElement.LAST_ON_FIRST);
		return saves;
	}
	
	public boolean isNotEmpty() {
		return saves.size() > 0;
	}
	
	@Override
	public String toString() {
		return "SaveGameListData [saves=" + saves + "]";
	}
}

class SaveGameElement {
	
	public static Comparator<SaveGameElement> LAST_ON_FIRST = new Comparator<SaveGameElement>() {
		@Override
		public int compare(SaveGameElement o1, SaveGameElement o2) {
			if (o2.date > o1.date) {
				return 1;
			} else {
				if (o1.date == o2.date) {
					return 0;
				}
				return -1;
			}
		}
	};
	
	protected String fileName;
	protected String name;
	protected long date;
	protected boolean autosave = false;
	protected String uuid;
	
	public SaveGameElement() {
	}
	
	public SaveGameElement(String fileName, String name) {
		this.fileName = fileName;
		this.name = name;
		this.date = System.currentTimeMillis();
	}

	public void setAutosaveId(String uuid) {
		autosave = true;
		this.uuid = uuid;
	}
	
	public SaveGameElement updateDateToNow() {
		this.date = System.currentTimeMillis();
		return this;
	}
	
	public String toString() {
		return "" + fileName + " " + name + " " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(date));
	}
}

public class SaveGameList {

	private static final String QUICK_SAVE_NAME = "Quick save";
	private static final String SAVE_GAME_LIST_FILE_NAME = "saveGameList";

	public String defaultSaveGameName(Game game) {
		return Messages.message(game.playingPlayer.getNationName()) + " " + Messages.message(game.getTurn().getTurnDateLabel());
	}
	
	public List<String> loadGameNames() {
		return loadGameNames(false);
	}
	
	public List<String> loadGameNames(boolean withAutosaves) {
		SaveGameListData saveGameList = loadGameList();
		List<SaveGameElement> saves = saveGameList.getSaves();
		
		List<String> l = new ArrayList<String>(saves.size());
		for (SaveGameElement saveGameElement : saves) {
			if (withAutosaves || !saveGameElement.autosave) {
				l.add(saveGameElement.name);
			}
		}
		return l;
	}
	
	public boolean isSaveNameExists(String name) {
		SaveGameListData saveGameList = loadGameList();
		return saveGameList.findSaveByName(name) != null;
	}
	
	public String lastOneSaveName() {
		SaveGameListData saveGameList = loadGameList();
		SaveGameElement last = saveGameList.last();
		if (last != null) {
			return last.name;
		}
		return null;
	}
	
	public void saveAs(String saveName, Game game) {
		SaveGameListData saveGameList = loadGameList();
		SaveGameElement saveElement = saveGameList.findOrCreateSaveByName(saveName);
		saveGameDataToFile(saveElement, game);
		saveGameList(saveGameList);
	}
	
	public void saveAsQuick(Game game) {
		SaveGameListData saveGameList = loadGameList();
		SaveGameElement saveElement = saveGameList.findOrCreateSaveByName(QUICK_SAVE_NAME);
		saveGameDataToFile(saveElement, game);
		saveGameList(saveGameList);
	}
	
	public void saveAsAutosave(Game game) {
		SaveGameListData saveGameList = loadGameList();
		String saveName = "Autosave " + defaultSaveGameName(game);
		SaveGameElement saveElement = saveGameList.findOrCreateSaveByName(saveName);
		saveElement.setAutosaveId(game.uuid());
		
		saveGameDataToFile(saveElement, game);
		saveGameList(saveGameList);
	}
	
	private void saveGameDataToFile(SaveGameElement quickSaveElement, Game game) {
		quickSaveElement.updateDateToNow();
		
        SavedGame savedGameObj = new SavedGame();
        savedGameObj.game = game;
		
		FileHandle sgfh = Gdx.files.local(quickSaveElement.fileName);
		Writer writer = sgfh.writer(false);
		try {
			new SaveGameCreator(writer).generateXmlFrom(savedGameObj);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
	}
	
	public Game loadGame(String name) {
		SaveGameListData loadGameList = loadGameList();
		SaveGameElement gameElement = loadGameList.findSaveByName(name);
		if (gameElement == null) {
			throw new IllegalStateException("can not find game by name " + name);
		}
		return loadGame(gameElement);
	}
	
	public Game loadLast() {
		SaveGameListData loadGameList = loadGameList();
		SaveGameElement lastElement = loadGameList.last();
		if (lastElement == null) {
			throw new IllegalStateException("can not load last save game because no last save game");
		}
		return loadGame(lastElement);
	}

	private Game loadGame(SaveGameElement saveGameElement) {
		FileHandle sgfh = Gdx.files.local(saveGameElement.fileName);
		try {
			return SaveGameParser.loadGameFromFile(sgfh.file());
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
	private SaveGameListData loadGameList() {
		FileHandle fh = Gdx.files.local(SAVE_GAME_LIST_FILE_NAME);
		System.out.println("load saves list file " + fh.file().getAbsolutePath());
		if (fh.isDirectory()) {
			throw new IllegalStateException("can not write to " + SAVE_GAME_LIST_FILE_NAME);
		}
		if (!fh.exists()) {
			return new SaveGameListData();
		}
		
		Json jsonObj = new Json();
		jsonObj.setOutputType(OutputType.json);
		return jsonObj.fromJson(SaveGameListData.class, fh);
	}
	
	private void saveGameList(SaveGameListData saveGameListData) {
		FileHandle fh = Gdx.files.local(SAVE_GAME_LIST_FILE_NAME);
		System.out.println("save list file " + fh.file().getAbsolutePath());
		if (fh.isDirectory()) {
			throw new IllegalStateException("can not write to " + SAVE_GAME_LIST_FILE_NAME);
		}
		
		Json jsonObj = new Json();
		jsonObj.setOutputType(OutputType.json);
		String jsonStr = jsonObj.prettyPrint(saveGameListData);
		
		OutputStreamWriter outputStream = new OutputStreamWriter(fh.write(false));
		try {
			outputStream.write(jsonStr);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		} finally {
			try {
				outputStream.close();
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
	}
}
